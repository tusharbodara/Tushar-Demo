package com.axelor.di;

import java.awt.geom.IllegalPathStateException;
import java.util.Scanner;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Inject;


public class LicenceChecker implements MethodInterceptor{

	@Inject
	private Licence licence;
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		System.out.println("You need a valid Licence to use this functionality...Please enter your Licence string: ");
		Scanner input = new Scanner(System.in);
        String licenceString = input.nextLine();
        if(!licence.checkLicence(licenceString)) {
        	System.out.println("invalid Licence !");
        	throw new IllegalPathStateException("Invalid Exception " + invocation.getMethod().getName());
        }
		
		return invocation.proceed();
	}

	
}
