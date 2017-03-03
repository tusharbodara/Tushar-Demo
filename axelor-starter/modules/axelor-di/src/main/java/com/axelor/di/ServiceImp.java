package com.axelor.di;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServiceImp implements Service{
	
	
	public void doSomething1() {
		 System.out.println("I'm doing something - 1");
	}
	@LicenceRequired
	public void doSomething2() {
		 System.out.println("I'm doing something - 2");
	}
}
