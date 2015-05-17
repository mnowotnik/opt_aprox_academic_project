package opt;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Program;
import org.moeaframework.util.tree.Environment;

import utils.CsvReader;
import utils.CsvRecord;

public class SymbolicTraining {

	private final static String UPRICE_CSV = "/opt/unit_price.csv";
	private final DataSet uPriceDataSet;
	private final static int MAX_EVALS = 90000;
	private final static String ALGORITHM = "NSGAII";

	public SymbolicTraining() {
		InputStream upriceIs = ClassLoader.class
				.getResourceAsStream(UPRICE_CSV);
		CsvReader csvReader = new CsvReader();
		List<CsvRecord> upriceRecords = csvReader.readCsv(upriceIs, "\t", true);

		this.uPriceDataSet = new DataSet(upriceRecords, "unit_price",
				Arrays.asList(new String[] { "volume", "quality" }));
		for (int i = 0; i < this.uPriceDataSet.getInput().length; i++) {
			this.uPriceDataSet.getInput()[i][0] /= 1000;
		}

	}

	UnitPriceSymFun trainUPriceFun() {
		for (int i = 0; i < 10; i++) {

			System.out.println(this.uPriceDataSet.getInput()[i][0] + " "
					+ this.uPriceDataSet.getInput()[i][1] + " "
					+ this.uPriceDataSet.getOutput()[i][0]);

		}
		NondominatedPopulation res = new Executor()
				.withProblemClass(SymbolicRegression.class, this.uPriceDataSet)
				.withAlgorithm(ALGORITHM).withMaxEvaluations(MAX_EVALS)
				.withProperty("populationSize", "2000")
				.withProperty("pm.rate", "0.5").withProperty("sbx.rate", "0.5")
				.run();

		System.out.println(res.size() + " " + res.get(0).getObjective(0));
		Solution sol = res.get(0);
		Environment env = new Environment();
		env.set("0", 400);
		env.set("1", 77);
		Program program = (Program) sol.getVariable(0);
		Number res1 = (Number) program.evaluate(env);
		System.out.println(res1.doubleValue());
		UnitPriceSymFun upriceFun = new UnitPriceSymFun(sol);
		System.out.println(upriceFun.compute(400, 77));
		System.out.println(upriceFun.compute(400, 50));
		System.out.println(upriceFun.compute(100, 68));
		System.out.println(upriceFun.compute(100, 75));
		return upriceFun;

	}
}
