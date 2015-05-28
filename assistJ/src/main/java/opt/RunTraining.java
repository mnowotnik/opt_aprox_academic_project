package opt;

import org.encog.Encog;

public class RunTraining {

	public static void main(final String args[]) {
		NeuralTraining training = new NeuralTraining();
		training.trainPercSoldApprox(0.05,"?:B->TANH->4:B->?");
//		training.trainUnitPriceApprox(1e-4,"?:B->TANH->8:B->?");
		Encog.getInstance().shutdown();

	}
}