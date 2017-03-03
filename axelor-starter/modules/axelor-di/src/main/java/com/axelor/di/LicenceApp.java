package com.axelor.di;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class LicenceApp {
	
	public static void main(String[] args) {
		 Injector injector = Guice.createInjector(new LicenceModule());
		 ServiceAdapter serviceInterface=injector.getInstance(ServiceAdapter.class);
		 System.out.println(injector);
		serviceInterface.doSomething();
		 
	}
}
