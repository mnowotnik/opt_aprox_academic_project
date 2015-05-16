package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CsvReader {
	
	public List<CsvRecord>readCsv(InputStream is, String delim,boolean withHeaders){
		
		List<CsvRecord> rList = new ArrayList<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			String headers = br.readLine();
			String [] headersTab = headers.split(delim);
			Map<String,Integer>valMap = createValMap(headersTab);
			while((line = br.readLine()) != null){
				CsvRecord csvR = new CsvRecord(valMap,line.split(delim));
				rList.add(csvR);
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return rList;
		
	}
	
	private Map<String,Integer> createValMap(String [] headers){
		Map<String,Integer> valMap = new HashMap<>();
		for(int i=0;i<headers.length;i++){
			valMap.put(headers[i], i);
		}
		return valMap;


		
		
	}

}
