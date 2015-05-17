package opt;

import java.util.HashMap;
import java.util.Map;

import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.neural.networks.BasicNetwork;

public class UnitPriceFun {
	
	private final NormalizationHelper nHelper;
	private final BasicNetwork network;
	private final Map<String,ColumnDefinition> colMap;
	public UnitPriceFun(NormalizationHelper nHelper, BasicNetwork network) {
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

	synchronized public double compute(double volume,double quality){
		MLData input = normalizeInput(volume,quality);
		MLData output = network.compute(input);
		return denormalizeOutput(output);
	}

	public double denormalizeOutput(MLData val){
		ColumnDefinition perc = colMap.get("unit_price");
		String d = nHelper.getNormStrategy().denormalizeColumn(perc, false, val, 0);
		return Double.parseDouble(d);
		
	}
	public MLData normalizeInput(double volume,double quality){
		ColumnDefinition vol = colMap.get("volume");
		ColumnDefinition qual = colMap.get("quality");
		MLData input = nHelper.allocateInputVector();
		nHelper.getNormStrategy().normalizeColumn(vol, true, volume, input.getData(), 0);
		nHelper.getNormStrategy().normalizeColumn(qual, true, quality, input.getData(), 1);
		return input;
	}

}
