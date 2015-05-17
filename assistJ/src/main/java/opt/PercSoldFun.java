package opt;

import java.util.HashMap;
import java.util.Map;

import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.neural.networks.BasicNetwork;

public class PercSoldFun {
	
	private final NormalizationHelper nHelper;
	private final BasicNetwork network;
	private final Map<String,ColumnDefinition> colMap;
	public PercSoldFun(NormalizationHelper nHelper, BasicNetwork network) {
		super();
		this.nHelper = nHelper;
		this.network = network;
		colMap = new HashMap<>();
		for(ColumnDefinition col : nHelper.getInputColumns()){
			colMap.put(col.getName(), col);
		}
		for(ColumnDefinition col : nHelper.getOutputColumns()){
			colMap.put(col.getName(), col);
		}


	}
	synchronized public double compute(double quality,double price,Advertisments ads){
		MLData input =  normalizeInput(quality,price,ads);
		MLData output = network.compute(input);
		return denormalizeOutput(output);
		
	}
	
	public double denormalizeOutput(MLData val){
		ColumnDefinition perc = colMap.get("sold_ratio");
		String d = nHelper.getNormStrategy().denormalizeColumn(perc, false, val, 0);
		return Double.parseDouble(d);
		
	}
	public MLData normalizeInput(double quality,double price,Advertisments ads){
		ColumnDefinition priceCol= colMap.get("price");
		ColumnDefinition qualityCol = colMap.get("quality");
		ColumnDefinition tvCol = colMap.get("tv");
		ColumnDefinition internetCol = colMap.get("internet");
		ColumnDefinition wareCol = colMap.get("warehouse");
		MLData values = nHelper.allocateInputVector();
		nHelper.getNormStrategy().normalizeColumn(qualityCol, true, quality, values.getData(), 0);
		nHelper.getNormStrategy().normalizeColumn(tvCol, true, ads.tv, values.getData(), 1);
		nHelper.getNormStrategy().normalizeColumn(internetCol, true, ads.internet, values.getData(), 2);
		nHelper.getNormStrategy().normalizeColumn(wareCol, true, ads.warehouse, values.getData(), 3);
		nHelper.getNormStrategy().normalizeColumn(priceCol, true, price, values.getData(), 4);
		return values;
	}
}
