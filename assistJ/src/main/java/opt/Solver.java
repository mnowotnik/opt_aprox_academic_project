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

	public Solver(UnitPriceFun upriceFun, PercSoldFun percSoldFun) {
		this.upriceFun = upriceFun;
		this.percSoldFun = percSoldFun;
	}

	public Solver() {
		ObjLoader ol = new ObjLoader();
		NormalizationHelper upriceNormHelper = ol.loadNormHelperUprice();
		BasicNetwork upriceNN = ol.loadUPriceNN();
		UnitPriceFun upriceFun = new UnitPriceFun(upriceNormHelper, upriceNN);

		BasicNetwork percSoldNN = ol.loadPercSoldNN();
		NormalizationHelper percSoldNormHelper = ol.loadNormHelperPercSold();
		PercSoldFun percSoldFun = new PercSoldFun(percSoldNormHelper,
				percSoldNN);
		this.upriceFun = upriceFun;
		this.percSoldFun = percSoldFun;
//		double p= percSoldFun.compute(62, 22, new Advertisments(0, 60000, 38000));
//		System.out.println(p);
//		p= percSoldFun.compute(62, 22, new Advertisments(38000, 60000, 0));
//		System.out.println(p);
//		InvestProblem investProblem = new InvestProblem(upriceFun, percSoldFun, new Constraints(0, 1, 300000));
//		int i  = investProblem.netIncomeFunc(new int[]{73000,62,0,58000,0,22,80000});
//		int e  = investProblem.totalExpensesFunc(new int[]{73000,62,0,58000,0,22,80000});
//		System.out.println(i+" "+e);
//0.8969652983087147
//0.9405122256098115
	}

	public Decision solve(Constraints constraints) {
		Object[] args = new Object[] { upriceFun, percSoldFun, constraints };

		NondominatedPopulation res = new Executor()
				.withProblemClass(InvestProblem.class, args)
				.withAlgorithm(ALGORITHM).withMaxEvaluations(MAX_EVALS)
				.distributeOnAllCores()
				.run();

		for (Solution solution : res) {
			System.out.println(solution.getObjective(0) + " "
					+ (1-solution.getObjective(1)));
			int [] d  = (EncodingUtils.getInt(solution));
			for(int i=0;i<d.length;i++){
				System.out.print(d[i]+" ");
			}
			System.out.println();
		}

		return null;

	}

}
