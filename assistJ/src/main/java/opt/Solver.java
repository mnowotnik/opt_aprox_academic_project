package opt;

import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.neural.networks.BasicNetwork;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

public class Solver {

	private final UnitPriceFun upriceFun;
	private final PercSoldFun percSoldFun;

	private final static String ALGORITHM = "NSGAIII";
	private final static int MAX_EVALS = 50000;
	private final static int AD_MULTI = 1000;

	public static void main(String[] args) {
		if (args.length != 3) {
			return;
		}
		Solver solver = new Solver();
		int debt = Integer.parseInt(args[0]);
		int period = Integer.parseInt(args[1]);
		int cash = Integer.parseInt(args[2]);
		Constraints constraints = new Constraints(debt, period, cash);
		Decision decision = solver.solve(constraints);
		System.out.println("volume:" + decision.inputArgs.volume);
		System.out.println("quality:" + decision.inputArgs.quality);
		System.out.println("tv:" + decision.inputArgs.ads.tv);
		System.out.println("internet:" + decision.inputArgs.ads.internet);
		System.out.println("warehouse:" + decision.inputArgs.ads.warehouse);
		System.out.println("price:" + decision.inputArgs.price);
		System.out.println("loan:" + decision.inputArgs.loan);
		System.out.println("instalment:" + decision.inputArgs.instalment);

		double risk = 1.0 - decision.objectives.percSold;
		System.out.println("netIncome:" + decision.objectives.netIncome
				+ " risk:" + risk);

	}
	

	public Solver(UnitPriceFun upriceFun, PercSoldFun percSoldFun) {
		this.upriceFun = upriceFun;
		this.percSoldFun = percSoldFun;
	}

	public Solver() {
		ObjLoader ol = new ObjLoader();

		BasicNetwork percSoldNN = ol.loadPercSoldNN();
		NormalizationHelper percSoldNormHelper = ol.loadNormHelperPercSold();
		PercSoldFun percSoldFun = new PercSoldFun(percSoldNormHelper,
				percSoldNN);

		BasicNetwork upriceNN = ol.loadUPriceNN();
		NormalizationHelper upriceNormHelper = ol.loadNormHelperUprice();
		UnitPriceFun upriceFun = new UnitPriceFun(upriceNormHelper, upriceNN);
		this.upriceFun = upriceFun;
		this.percSoldFun = percSoldFun;
		// double p= percSoldFun.compute(60, 24, new Advertisments(9000,75000,
		// 0));
		// System.out.println(p);
		// p= percSoldFun.compute(62, 22, new Advertisments(38000, 60000, 0));
		// System.out.println(p);
		// InvestProblem investProblem = new InvestProblem(upriceFun,
		// percSoldFun, new Constraints(0, 1, 300000));
		// int i = investProblem.netIncomeFunc(new
		// int[]{71000,60,9,75,0,24,800000,184000});
		// System.out.println(i);
		// 0.8969652983087147
		// 0.9405122256098115
	}

	public Decision solve(Constraints constraints) {
		Object[] args = new Object[] { upriceFun, percSoldFun, constraints };

		NondominatedPopulation res = new Executor()
				.withProblemClass(InvestProblem.class, args)
				.withAlgorithm(ALGORITHM).withMaxEvaluations(MAX_EVALS)
				.distributeOnAllCores().run();

		// for (Solution solution : res) {
		// System.out.println(solution.getObjective(0) + " "
		// + (1-solution.getObjective(1)));
		// int [] d = (EncodingUtils.getInt(solution));
		// for(int i=0;i<d.length;i++){
		// System.out.print(d[i]+" ");
		// }
		// System.out.println();
		// }
		Solution solution = res.get(0);

		int[] vars = EncodingUtils.getInt(solution);
		int volume = vars[0];
		int quality = vars[1];
		int tv = vars[2] * AD_MULTI;
		int internet = vars[3] * AD_MULTI;
		int warehouse = vars[4] * AD_MULTI;
		int price = vars[5];
		int loan = vars[6];
		int instalment = vars[7];
		InputArgs iArgs = new InputArgs(volume, quality, price, loan,
				instalment, new Advertisments(tv, internet, warehouse));
		Objectives objectives = new Objectives(-1 * solution.getObjective(0),
				1 - solution.getObjective(1));
		Decision decision = new Decision(iArgs, objectives);

		return decision;

	}

}
