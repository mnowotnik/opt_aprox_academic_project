package opt;

import java.util.List;

import utils.CsvRecord;

public class DataSet {
	private final double inputs[][];
	private final double outputs[][];
	public final int inputNum, outputNum, rowsNum;

	public DataSet(List<CsvRecord> csvData, String outputHeader,
			List<String> inputHeaders) {

		CsvRecord f = csvData.get(0);
		int allVal = f.getValues().length;
		inputs = new double[csvData.size()][allVal - 1];
		outputs = new double[csvData.size()][1];
		for (int i = 0; i < csvData.size(); i++) {
			CsvRecord cs = csvData.get(i);
			outputs[i][0] = Double.parseDouble(cs.getValue(outputHeader));
			for (int j = 0; j < inputs[0].length; j++) {
				inputs[i][j] = Double.parseDouble(cs.getValue(inputHeaders
						.get(j)));
			}
		}
		inputNum = inputs[0].length;
		outputNum = outputs[0].length;
		rowsNum = csvData.size();

	}

	public DataSet(double[][] input, double[][] output) {
		inputNum = input[0].length;
		outputNum = output[0].length;
		this.rowsNum = input.length;
		this.inputs = input;
		this.outputs = output;
	}

	public double[][] getInput() {
		return inputs;
	}

	public double[][] getOutput() {
		return outputs;
	}

}
