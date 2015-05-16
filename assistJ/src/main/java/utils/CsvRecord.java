package utils;

import java.util.Map;

public class CsvRecord {
	private final Map<String,Integer>valMap;
	private final String[] values;

	public String[] getValues() {
		return values;
	}
	
	public String getValue(String key){
		return values[valMap.get(key)];
	}

	public CsvRecord(Map<String, Integer> valMap,String [] values) {
		super();
		this.valMap = valMap;
		this.values = values;
	}

}
