package opt;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.neural.networks.BasicNetwork;

public class ObjLoader {

	private final static String UPRICE_MODEL_PATH = "/opt/uprice.ser";
	private final static String UPRICE_HELPER_PATH = "/opt/uprice_helper.ser";
	private final static String PERCSOLD_MODEL_PATH = "/opt/persold.ser";
	private final static String PERCSOLD_HELPER_PATH = "/opt/percsold_helper.ser";

	public BasicNetwork loadUPriceNN() {
		try {
			InputStream nnIs = ClassLoader.class
					.getResourceAsStream(UPRICE_MODEL_PATH);
			ObjectInputStream ois;
			ois = new ObjectInputStream(nnIs);
			BasicNetwork h = (BasicNetwork) ois.readObject();
			ois.close();
			nnIs.close();
			return h;
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new BasicNetwork();

	}

	public NormalizationHelper loadNormHelperUprice() {
		InputStream helperIs = ClassLoader.class
				.getResourceAsStream(UPRICE_HELPER_PATH);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(helperIs);
			NormalizationHelper h = (NormalizationHelper) ois.readObject();
			ois.close();
			helperIs.close();
			return h;
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new NormalizationHelper();

	}

	public BasicNetwork loadPercSoldNN() {
		try {
			InputStream nnIs = ClassLoader.class
					.getResourceAsStream(PERCSOLD_MODEL_PATH);
			ObjectInputStream ois;
			ois = new ObjectInputStream(nnIs);
			BasicNetwork h = (BasicNetwork) ois.readObject();
			ois.close();
			nnIs.close();
			return h;
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new BasicNetwork();
	}

	public NormalizationHelper loadNormHelperPercSold() {
		InputStream helperIs = ClassLoader.class
				.getResourceAsStream(PERCSOLD_HELPER_PATH);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(helperIs);
			NormalizationHelper h = (NormalizationHelper) ois.readObject();
			ois.close();
			helperIs.close();
			return h;
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new NormalizationHelper();
	}
}
