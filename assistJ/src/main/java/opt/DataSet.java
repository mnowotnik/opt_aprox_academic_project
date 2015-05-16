package opt;


public class DataSet {
	private final double inputs[][];
	private final double outputs[][];
	public DataSet(double[][] input, double[][] output) {
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
