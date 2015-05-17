package opt;

import org.encog.Encog;

public class RunTraining {

	public static void main(final String args[]) {
		NeuralTraining training = new NeuralTraining();
		training.trainPercSoldApprox(8e-5,"?:B->TANH->12:B->?");
		training.trainUnitPriceApprox(5e-5,"?:B->TANH->20:B->?");
		Encog.getInstance().shutdown();

	}
}