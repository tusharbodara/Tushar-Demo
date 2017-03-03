package com.axelor.di;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;

public class LicenceModule extends AbstractModule {

	@Override
	protected void configure() {
		
		LicenceChecker licenceChecker=new LicenceChecker();
		requestInjection(licenceChecker);
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(LicenceRequired.class), licenceChecker);
		bind(Service.class).to(ServiceImp.class);
	}
 
}
