package com.axelor.sale.web;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.stream.FileImageInputStream;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.osgi.service.upnp.UPnPLocalStateVariable;

import com.axelor.db.JpaSupport;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.sale.db.Invoice;
import com.axelor.sale.db.InvoiceItem;
import com.axelor.sale.db.Order;
import com.axelor.sale.db.Tax;
import com.axelor.sale.db.repo.InvoiceItemRepository;
import com.axelor.sale.db.repo.InvoiceRepository;
import com.axelor.sale.db.repo.OrderRepository;
import com.axelor.sale.service.SaleOrderService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


import com.axelor.sale.db.OrderItem;

public class SaleOrderController extends JpaSupport {
	
	
	@Inject
	OrderRepository OrderRepo;
	
	@Inject
	InvoiceItemRepository invoiceItemRepo;
	
	@Inject
	InvoiceRepository invoiceRepo;
	
	@Inject
	SaleOrderService orderService;
	
	@Inject
	MetaFiles metaFile;

	public void calculate(ActionRequest request, ActionResponse response) {
		Order order = request.getContext().asType(Order.class);
		order = orderService.calculate(order);
		response.setValue("amount", order.getAmount());
		response.setValue("taxAmount", order.getTaxAmount());
		response.setValue("totalAmount", order.getTotalAmount());
	}

	public void calculateInvoice(ActionRequest request, ActionResponse response) {
		Invoice invoice = request.getContext().asType(Invoice.class);
		BigDecimal quantity;
		BigDecimal price;
		Set<Tax> tax;
		BigDecimal taxTotal = BigDecimal.ZERO;
		BigDecimal amt = BigDecimal.ZERO;
		BigDecimal totalAmt = BigDecimal.ZERO;
		for (int i = 0; i < invoice.getInvoiceItem().size(); i++) {
			quantity = BigDecimal.valueOf(invoice.getInvoiceItem().get(i).getQuantity());
			price = invoice.getInvoiceItem().get(i).getPrice();
			amt = amt.add((quantity).multiply(price));
			tax = invoice.getInvoiceItem().get(i).getTaxes();
			if (tax == null) {
				taxTotal = BigDecimal.ZERO;
			} else {
				for (Tax tax1 : tax) {

					taxTotal = taxTotal.add(amt.multiply(tax1.getRate()));
				}
			}
		}
		totalAmt = totalAmt.add(amt).add(taxTotal);
		invoice.setAmount(amt);
		invoice.setTaxAmount(taxTotal);
		invoice.setTotalAmount(totalAmt);
		response.setValue("amount", invoice.getAmount());
		response.setValue("taxAmount", invoice.getTaxAmount());
		response.setValue("totalAmount", invoice.getTotalAmount());
	}

	public void cancelDraft(ActionRequest request, ActionResponse response) {
		Order order = request.getContext().asType(Order.class);
		OrderItem items = new OrderItem();
		String query = "delete from sale_order_item o where o.sale_order=" + order.getId();
		response.setValue("items", query);
	}

	@Transactional
	public void generateInvoice(ActionRequest request, ActionResponse response) throws IOException {
		Order order = request.getContext().asType(Order.class);
		Invoice invoice = new Invoice();
		InvoiceItem invoiceItem = new InvoiceItem();
		List<InvoiceItem> invoiceItemList = new ArrayList<InvoiceItem>();
		List<Order> listorder = OrderRepo.all().filter("self.id=?", order.getId()).fetch();
		InvoiceItem[] invoiceItemsArray = new InvoiceItem[order.getItems().size()];
		
		
		for (Order o : listorder) {
			invoice.setOrderSale(o);
			invoice.setState("draft");
			invoice.setInvoiceDate(order.getConfirmDate());
			invoice.setTaxAmount(o.getTaxAmount());
			invoice.setAmount(o.getAmount());
			invoice.setTotalAmount(o.getTotalAmount());
			
			Set<Tax> tax = null;
			for (int i = 0; i < o.getItems().size(); i++) {
				invoiceItemsArray[i] = new InvoiceItem();
				invoiceItemsArray[i].setProduct(o.getItems().get(i).getProduct());
				invoiceItemsArray[i].setPrice(o.getItems().get(i).getPrice());
				invoiceItemsArray[i].setQuantity(o.getItems().get(i).getQuantity());
				invoiceItemsArray[i].setTaxes(new HashSet<>(order.getItems().get(i).getTaxes()));
				invoiceItemList.add(invoiceItemsArray[i]);
				for (InvoiceItem it : invoiceItemList) {
					it.setInvoice(invoice);
					invoiceItemRepo.save(it);
				}
			}
		}
		invoiceRepo.save(invoice);
		
		EntityManager em=getEntityManager();
		Query query1 = em
				.createNativeQuery("update sale_invoice set image=34 where name='" + invoice.getName() + "'");
		query1.executeUpdate();
		
	}

	public void deleteInvoice(ActionRequest request, ActionResponse response) {
		Order order = request.getContext().asType(Order.class);
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		Query query2 = em.createNativeQuery("delete from sale_invoice_item_taxes where sale_invoice_item IN (select id from sale_invoice_item where invoice IN(select id from sale_invoice where name='"+ order.getInvoice().getName() + "' ))");
		query2.executeUpdate();
		Query query = em.createNativeQuery("delete from sale_invoice_item where invoice IN (select id from sale_invoice where name='"+ order.getInvoice().getName() + "')");
		query.executeUpdate();
		Query query1 = em.createNativeQuery("delete from sale_invoice where name='" + order.getInvoice().getName() + "'");
		query1.executeUpdate();
		em.getTransaction().commit();
	}
	
	public void reportToday(ActionRequest request, ActionResponse response) {
		BigDecimal lastDayAmount = BigDecimal.ZERO;
		BigDecimal todayAmount = BigDecimal.ZERO;
		BigDecimal percentage = BigDecimal.ZERO;
		boolean down;
		/*List<Order> lastDayOrders = OrderRepo.all()
				.filter("YEAR(orderDate) = YEAR(current_date) and MONTH(orderDate) = MONTH(current_date) and DAY(orderDate) = DAY(current_date)-1")
				.fetch();*/
		
		List<Order> lastDayOrders = OrderRepo.all()
				.filter("orderDate=DATE(current_date)-1")
				.fetch();
		/*
		List<Order> todayOrders = OrderRepo.all()
				.filter("YEAR(orderDate) = YEAR(current_date) and MONTH(orderDate) = MONTH(current_date) and DAY(orderDate) = DAY(current_date)")
				.fetch();*/
		

		List<Order> todayOrders = OrderRepo.all()
				.filter("orderDate=DATE(current_date)")
				.fetch();
		for (Order order : lastDayOrders) {
			lastDayAmount = lastDayAmount.add(order.getTotalAmount());
		}
		for (Order orderToday : todayOrders) {
			todayAmount = todayAmount.add(orderToday.getTotalAmount());
		}
		if (todayAmount.floatValue() > lastDayAmount.floatValue()) {
			down = true;
		} else {
			down = false;
		}
		percentage = ((todayAmount.subtract(lastDayAmount)).multiply(new BigDecimal(100))).divide(todayAmount, 2,
				RoundingMode.HALF_UP);
		System.out.println(lastDayAmount);
		System.out.println(todayAmount);
		Map<String, Object> mapData = new HashMap<String, Object>();
		mapData.put("total", todayAmount);
		mapData.put("percentage", percentage);
		mapData.put("down", down);
		response.setData(Arrays.asList(mapData));
	}

	public void reportMonthly(ActionRequest request, ActionResponse response) {
		BigDecimal lastMonthAmount = BigDecimal.ZERO;
		BigDecimal currentMonthAmount = BigDecimal.ZERO;
		BigDecimal percentage = BigDecimal.ZERO;
		boolean down;
		List<Order> lastMonthOrders = OrderRepo.all()
				.filter("YEAR(orderDate) = YEAR(current_date) and MONTH(orderDate) = MONTH(current_date) - 1").fetch();
		List<Order> currentMonthOrders = OrderRepo.all()
				.filter("YEAR(orderDate) = YEAR(current_date) and MONTH(orderDate) = MONTH(current_date)").fetch();
		for (Order order : lastMonthOrders) {
			lastMonthAmount = lastMonthAmount.add(order.getTotalAmount());
		}
		for (Order orderToday : currentMonthOrders) {
			currentMonthAmount = currentMonthAmount.add(orderToday.getTotalAmount());
		}
		if (currentMonthAmount.floatValue() > lastMonthAmount.floatValue()) {
			down = true;
		} else {
			down = false;
		}
		percentage = ((currentMonthAmount.subtract(lastMonthAmount)).multiply(new BigDecimal(100)))
				.divide(currentMonthAmount, 2, RoundingMode.HALF_UP);
		System.out.println(lastMonthAmount);
		System.out.println(currentMonthAmount);
		Map<String, Object> Data = new HashMap<String, Object>();
		Data.put("total", currentMonthAmount);
		Data.put("percentage", percentage);
		Data.put("down", down);
		response.setData(Arrays.asList(Data));
	}
	
	public void getInvoiceImage(ActionRequest request, ActionResponse response) {
		Invoice invoice = request.getContext().asType(Invoice.class);
		MetaFile file = invoice.getImage();
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		if (file != null) {

			Query query1 = em.createNativeQuery("update meta_file set file_path= '" + metaFile.getPath(file).toString()
					+ "' where id =(select image from sale_invoice where name= '" + invoice.getName() + "')");
			query1.executeUpdate();

		} else {

			Query query1 = em
					.createNativeQuery("update sale_invoice set image=34 where name='" + invoice.getName() + "'");
			query1.executeUpdate();

		}
		em.getTransaction().commit();

	}
}