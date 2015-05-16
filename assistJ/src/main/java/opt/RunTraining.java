package opt;

import org.encog.Encog;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.neural.networks.BasicNetwork;

public class RunTraining {

	public static void main(final String args[]) {
		NeuralTraining training = new NeuralTraining();
		BasicNetwork trainUnitPriceApprox = training.trainUnitPriceApprox();
		ObjLoader ol = new ObjLoader();
		NormalizationHelper hUp = ol.loadNormHelperUprice();
		BasicNetwork uPNN = ol.loadUPriceNN();
		UnitPriceFun uPriceF = new UnitPriceFun(hUp,uPNN);
		System.out.println(uPriceF.compute(100000, 68));
		Encog.getInstance().shutdown();

	}
}