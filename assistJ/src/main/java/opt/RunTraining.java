package opt;

import org.encog.Encog;

public class RunTraining {

	public static void main(final String args[]) {
		NeuralTraining training = new NeuralTraining();
		training.trainPercSoldApprox(1e-4,"?:B->TANH->9:B->?");
		training.trainUnitPriceApprox(1e-3,"?:B->TANH->9:B->?");
		Encog.getInstance().shutdown();

	}
}