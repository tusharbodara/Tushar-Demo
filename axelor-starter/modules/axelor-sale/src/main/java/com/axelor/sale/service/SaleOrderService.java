package com.axelor.sale.service;

import java.math.BigDecimal;
import java.util.Set;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.sale.db.Order;
import com.axelor.sale.db.Tax;

public class SaleOrderService {
	
	public Order calculate(Order order) {
		BigDecimal quantity;
		BigDecimal price;
		Set<Tax> tax;
		BigDecimal taxTotal = BigDecimal.ZERO;
		BigDecimal amt = BigDecimal.ZERO;
		BigDecimal totalAmt = BigDecimal.ZERO;
		for (int i = 0; i < order.getItems().size(); i++) {
			quantity = BigDecimal.valueOf(order.getItems().get(i).getQuantity());
			price = order.getItems().get(i).getPrice();
			amt = amt.add((quantity).multiply(price));
			tax = order.getItems().get(i).getTaxes();
			if (tax == null) {
				taxTotal = BigDecimal.ZERO;
			} else {
				for (Tax tax1 : tax) {
					taxTotal = taxTotal.add(amt.multiply(tax1.getRate()));
				}
			}
		}
		totalAmt = totalAmt.add(amt).add(taxTotal);
		order.setAmount(amt);
		order.setTaxAmount(taxTotal);
		order.setTotalAmount(totalAmt);
		return order;
	}
	

	

	
}


