package opt;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.normalizers.strategies.BasicNormalizationStrategy;
import org.encog.ml.data.versatile.normalizers.strategies.NormalizationStrategy;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.EncogUtility;

public class NeuralTraining {

	private final static String NN_ARCH = "?:B->TANH->12:B->?";
	private final static double ERR = 8e-5;
	private final static String UPRICE_MODEL_PATH = "src/main/resources/opt/uprice.ser";
	private final static String UPRICE_HELPER_PATH = "src/main/resources/opt/uprice_helper.ser";
	private final static String PERCSOLD_MODEL_PATH = "src/main/resources/opt/persold.ser";
	private final static String PERCSOLD_HELPER_PATH = "src/main/resources/opt/percsold_helper.ser";

	private final static String UPRICE_CSV = "/opt/unit_price.csv";
	private final static String PERCSOLD_CSV = "/opt/percsold.csv";
	private final static CSVFormat DEF_CSV_FMT = new CSVFormat('.', '\t');

	private NormalizationStrategy getTanhNormalization() {
		return new BasicNormalizationStrategy(-1, 1, -1, 1);
	}

	private NormalizationStrategy getSigmoidNormalization() {
		return new BasicNormalizationStrategy(0, 1, 0, 1);
	}

	public BasicNetwork trainUnitPriceApprox() {
		final int IN_N = 2, OUT_N = 1;

		VersatileMLDataSet ds = initDataSet(UPRICE_CSV, DEF_CSV_FMT);
		ds.defineSourceColumn("volume", ColumnType.continuous);
		ds.defineSourceColumn("quality", ColumnType.continuous);
		ColumnDefinition uPriceCol = ds.defineSourceColumn("unit_price",
				ColumnType.continuous);
		ds.analyze();
		ds.defineSingleOutputOthersInput(uPriceCol);
		ds.getNormHelper().setStrategy(getTanhNormalization());
		// EncogModel model = new EncogModel(ds);
		// model.selectMethod(ds,
		// MLMethodFactory.TYPE_FEEDFORWARD,NN_ARCH,"rprop","");
		ds.normalize();

		// model.holdBackValidation(0.3, true,1001);
		// model.selectTrainingType(ds);
		BasicNetwork network = (BasicNetwork) (new MLMethodFactory().create(
				MLMethodFactory.TYPE_FEEDFORWARD, NN_ARCH, IN_N, OUT_N));
		final ResilientPropagation train = new ResilientPropagation(network, ds);

		NormalizationHelper helper = ds.getNormHelper();

		int epoch = 1;

		do {
			train.iteration();
			System.out
					.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while (train.getError() > ERR);
		train.finishTraining();
		// BasicNetwork network = (BasicNetwork) model.crossvalidate(5,true);

		persist(helper, Paths.get(UPRICE_HELPER_PATH), network,
				Paths.get(UPRICE_MODEL_PATH));
		// EncogUtility.evaluate(network, ds);

		return network;

	}

	public BasicNetwork trainPercSoldApprox() {
		final int IN_N = 5, OUT_N = 1;

		VersatileMLDataSet ds = initDataSet(PERCSOLD_CSV, DEF_CSV_FMT);
		ds.defineSourceColumn("quality", ColumnType.continuous);
		ds.defineSourceColumn("tv", ColumnType.continuous);
		ds.defineSourceColumn("internet", ColumnType.continuous);
		ds.defineSourceColumn("warehouse", ColumnType.continuous);
		ds.defineSourceColumn("price", ColumnType.continuous);
		ColumnDefinition percSoldCol = ds.defineSourceColumn("sold_ratio",
				ColumnType.continuous);
		ds.analyze();
		ds.defineSingleOutputOthersInput(percSoldCol);
		ds.getNormHelper().setStrategy(getTanhNormalization());
		// EncogModel model = new EncogModel(ds);
		// model.selectMethod(ds, MLMethodFactory.TYPE_FEEDFORWARD, NN_ARCH,
		// "rprop", "");
		ds.normalize();
		//
		// model.holdBackValidation(0.3, true, 1001);
		// model.selectTrainingType(ds);
		BasicNetwork network = (BasicNetwork) (new MLMethodFactory().create(
				MLMethodFactory.TYPE_FEEDFORWARD, NN_ARCH, IN_N, OUT_N));
		final ResilientPropagation train = new ResilientPropagation(network, ds);

		NormalizationHelper helper = ds.getNormHelper();

		int epoch = 1;

		do {
			train.iteration();
			System.out
					.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while (train.getError() > ERR);
		train.finishTraining();
		// BasicNetwork network = (BasicNetwork) model.crossvalidate(5, true);

		persist(helper, Paths.get(PERCSOLD_HELPER_PATH), network,
				Paths.get(PERCSOLD_MODEL_PATH));
		EncogUtility.evaluate(network, ds);
		System.out.println("Training error: "+
		EncogUtility.calculateRegressionError(network, ds));
		// System.out
		// .println("Validation error: "
		// + model.calculateError(network,
		// model.getValidationDataset()));

		// Display our normalization parameters.
		System.out.println(helper.toString());

		 EncogUtility.evaluate(network, ds);
		// Display the final model.
		System.out.println("Final model: " + network);
		System.out.println(compute(new double[]{62,38000,60000,0,22}, network, helper));
		System.out.println(compute(new double[]{62,0,60000,38000,22}, network, helper));
		PercSoldFun percSoldFun = new PercSoldFun(helper,
				network);
		double p= percSoldFun.compute(62, 22, new Advertisments(0, 60000, 38000));
		System.out.println(p);
		p= percSoldFun.compute(62, 22, new Advertisments(38000, 60000, 0));
		System.out.println(p);
		return network;

	}

	private VersatileMLDataSet initDataSet(String csvPath, CSVFormat csvFmt) {
		URL csvURL = ClassLoader.class.getResource(csvPath);
		VersatileDataSource dsource = new CSVDataSource(new File(
				csvURL.getPath()), true, csvFmt);
		VersatileMLDataSet ds = new VersatileMLDataSet(dsource);
		ds.getNormHelper().setFormat(csvFmt);
		return ds;

	}

	private void persist(NormalizationHelper helper, Path helperPath,
			BasicNetwork network, Path networkPath) {
		try {
			OutputStream modelOS = Files.newOutputStream(networkPath);
			ObjectOutputStream nnOs = new ObjectOutputStream(modelOS);
			nnOs.writeObject(network);
			nnOs.close();
			modelOS.close();

			OutputStream helperOS = Files.newOutputStream(helperPath);
			ObjectOutputStream objStream = new ObjectOutputStream(helperOS);
			objStream.writeObject(helper);
			objStream.close();
			helperOS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private double compute(double[] input, BasicNetwork network,
			NormalizationHelper helper) {

		MLData data = helper.allocateInputVector();
		String[] dataS = new String[input.length];
		for (int i = 0; i < input.length; i++) {
			dataS[i] = Double.toString(input[i]);
		}
		helper.normalizeInputVector(dataS, data.getData(), false);
//		System.out.println(data.getData(0) + " " + data.getData(1));
		MLData output = network.compute(data);
//		System.out.println(output.getData(0));
		String[] out = helper.denormalizeOutputVectorToString(output);
		return Double.parseDouble(out[0]);

	}
}
