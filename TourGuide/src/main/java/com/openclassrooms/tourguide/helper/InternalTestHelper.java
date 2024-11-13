package com.openclassrooms.tourguide.helper;

/**
 * InternalTestHelper class provideshelper methods and properties for configuring 
 * the number of internal users used for testing purposes in the TourGuide app
 * This class allows for customization of the internal user count to facalitate
 * large-scale performance tests. 
 */
public class InternalTestHelper {

	// Set this default up to 100,000 for testing
	private static int internalUserNumber = 100;
	
	public static void setInternalUserNumber(int internalUserNumber) {
		InternalTestHelper.internalUserNumber = internalUserNumber;
	}
	
	public static int getInternalUserNumber() {
		return internalUserNumber;
	}
}
