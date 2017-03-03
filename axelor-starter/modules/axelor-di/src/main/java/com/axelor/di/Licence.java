package com.axelor.di;

public class Licence {
	
	public boolean checkLicence(String licenceString) {
		
		String licenceStringFromDB = getLicenceStringFromDB();
		return (licenceString != null && licenceStringFromDB != null && licenceString.equals(licenceStringFromDB) ? true : false);
	}

	private String getLicenceStringFromDB() {
		
		return "TEST";
	}

}
