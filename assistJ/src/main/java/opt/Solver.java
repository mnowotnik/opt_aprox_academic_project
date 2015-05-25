package opt;

import java.util.ArrayList;
import java.util.List;

import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.neural.networks.BasicNetwork;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

public class Solver {

	private final UnitPriceFun upriceFun;
	private final PercSoldFun percSoldFun;

	private final static String ALGORITHM = "eNSGAII";
	private final static int MAX_EVALS = 400000;
	private final static int AD_MULTI = 1000;
	private final static int MAX_DEBT = 800000;

	public static void main(String[] args) {
		if (args.length != 3) {
			return;
		}
		Solver solver = new Solver();
		int debt = Integer.parseInt(args[0]);
		int period = Integer.parseInt(args[1]);
		int cash = Integer.parseInt(args[2]);
		Constraints constraints = new Constraints(debt, period, cash);
		Decision decision = solver.solveBestInc(constraints);
		System.out.println("Decision");
		System.out.println("- volume:" + decision.inputArgs.volume);
		System.out.println("- quality:" + decision.inputArgs.quality);
		System.out.println("- tv:" + decision.inputArgs.ads.tv);
		System.out.println("- internet:" + decision.inputArgs.ads.internet);
		System.out.println("- warehouse:" + decision.inputArgs.ads.warehouse);
		System.out.println("- price:" + decision.inputArgs.price);
		System.out.println("- loan:" + decision.inputArgs.loan);
		System.out.println("- instalment:" + decision.inputArgs.instalment);

		System.out.println("Parameters");
		System.out.println("- unitPrice:" + decision.parameters.unitPrice);
		System.out.println("Financial report");
		System.out.println("- grossSalesIncome:"
				+ decision.report.grossSalesIncome);
		System.out.println("- primeCosts:" + decision.report.primeCosts);
		System.out.println("- salesIncome:" + decision.report.salesIncome);

		double risk = 1.0 - decision.objectives.percSold;
		System.out.println("Objectives");
		System.out.println("- realNetIncome:" + decision.objectives.netIncome);
		double wdecIncome = convertToWdecIncome(decision.objectives.netIncome,
				decision.inputArgs.instalment);
		System.out.println("- wdecNetIncome:" + Math.round(wdecIncome));
		System.out.println("- risk:" + risk);

	}

	/**
	 * @param upriceFun sieć neuronowa - funkcja kosztu jednostkowego
	 * @param percSoldFun sieć neuronowa - funkcja procentu sprzedanego wolumenu
	 */
	public Solver(UnitPriceFun upriceFun, PercSoldFun percSoldFun) {
		this.upriceFun = upriceFun;
		this.percSoldFun = percSoldFun;
	}

	public Solver() {
		//do deserializacji obiektów (sieci neuronowych)
		ObjLoader ol = new ObjLoader();

		// wczytywanie sieci, która aproksymuje procent sprzedanego wolumenu
		BasicNetwork percSoldNN = ol.loadPercSoldNN();
		// wczytywanie klasy do normalizacji danych wejściowych
		NormalizationHelper percSoldNormHelper = ol.loadNormHelperPercSold();
		// tworzenie obiektu - wrappera wokół sieci
		PercSoldFun percSoldFun = new PercSoldFun(percSoldNormHelper,
				percSoldNN);

		// wczytywanie sieci, która aproksymuje funkcję kosztu jednostkowego
		BasicNetwork upriceNN = ol.loadUPriceNN();
		// wczytywanie klasy do normalizacji danych wejściowych
		NormalizationHelper upriceNormHelper = ol.loadNormHelperUprice();
		// tworzenie obiektu - wrappera , wokół sieci
		UnitPriceFun upriceFun = new UnitPriceFun(upriceNormHelper, upriceNN);
		this.upriceFun = upriceFun;
		this.percSoldFun = percSoldFun;
	}

	public List<Decision> solve(Constraints constraints) {
		System.out.println("Solving...");
		Object[] args = new Object[] { upriceFun, percSoldFun, constraints };

		// uruchamianie biblioteki MOAE i otrzymywanie niezdominowaje populacji
		NondominatedPopulation res = new Executor()
				.withProblemClass(InvestProblem.class, args)
				.withAlgorithm(ALGORITHM).withMaxEvaluations(MAX_EVALS)
				.withProperty("populationSize", "500")
				// .withProperty("pm.rate", "0.3")
				.distributeOnAllCores().run();

		//tworzenie listy decyzji z otrzymanej niezdominowaje populacji rozwiązań
		List<Decision> decisions = new ArrayList<>();
		for (Solution solution : res) {

			int[] vars = EncodingUtils.getInt(solution);
			int volume = vars[0];
			int quality = vars[1];
			int tv = vars[2] * AD_MULTI;
			int internet = vars[3] * AD_MULTI;
			int warehouse = vars[4] * AD_MULTI;
			int price = vars[5];
			int loan = (int) ((double) vars[6] / 100 * (MAX_DEBT - constraints.debt));
			int instalment = vars[7]
					+ InvestProblem.calcMinInstalment(constraints.period, loan
							+ constraints.debt);
			InputArgs iArgs = new InputArgs(volume, quality, price, loan,
					instalment, new Advertisments(tv, internet, warehouse));
			Objectives objectives = new Objectives(-1
					* solution.getObjective(0), 1 - solution.getObjective(1));

			int grossSalesIncome = (int) solution
					.getAttribute("grossSalesIncome");
			int primeCosts = (int) solution.getAttribute("primeCosts");
			int salesIncome = (int) solution.getAttribute("salesIncome");
			Report report = new Report(grossSalesIncome, primeCosts,
					salesIncome);

			double unitPrice = (double) solution.getAttribute("unitPrice");
			Parameters parameters = new Parameters(unitPrice);
			Decision decision = new Decision(iArgs, objectives, report,
					parameters);
			decisions.add(decision);

		}
		return decisions;

	}

	public Decision solveBestInc(Constraints constraints) {
		List<Decision> decisions = solve(constraints);

		Decision bestDec = null;
		for (Decision decision : decisions) {
			if (bestDec == null
					|| decision.objectives.netIncome > bestDec.objectives.netIncome) {
				bestDec = decision;
			}
		}
		return bestDec;
	}

	// symulator na WDEC nalicza ratę dwa razy i podaje niepoprawny zysk
	public static double convertToWdecIncome(double netInc, double inst) {
		netInc /= (1 - 0.19);
		netInc -= inst;
		if (netInc < 0) {
			return netInc;
		}
		return (1 - 0.19) * netInc;

	}
}
