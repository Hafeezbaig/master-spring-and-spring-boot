package com.in28minutes.mockito.mockitodemo.business;

public class SomeBusinessImpl {
	
	private final DataService dataService;
	
	public SomeBusinessImpl(DataService dataService) {
		super();
		this.dataService = dataService;
	}
	
	public int findTheGreatestFromAllData() {
		int[] data = dataService.retrieveAllData();
		int greatestValue = Integer.MIN_VALUE;
		for(int value:data) {
			if(value > greatestValue)
				greatestValue = value;
		}
		return greatestValue;
	}

	//Added in Step 06 - a void call we can verify and an argument we can capture
	public void storeTheGreatestFromAllData() {
		dataService.storeGreatest(findTheGreatestFromAllData());
	}

}

interface DataService {
	int[] retrieveAllData();

	void storeGreatest(int greatestValue);

}