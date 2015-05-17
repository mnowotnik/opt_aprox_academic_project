package opt;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

public class InvestProblem extends AbstractProblem {

	private final static double MAX_DEBT = 800000;
	private final static double AMORT = 20000;
	private final static double CONST_COST = 10000;

	private final static int VAR_NUM = 8;
	private final static int OBJ_NUM = 2;
	private final static int CONS_NUM = 4;

	private final static int MAX_VOL = 400000;
	private final static int MIN_VOL = 1000;

	private final static int MIN_QUAL = 60, MAX_QUAL = 85;
	private final static int MIN_AD = 0, MAX_AD = 100;
	private final static int AD_MULTI = 1000;
	private final static int MIN_PRICE = 1, MAX_PRICE = 24;

	// year
	private final static double BANK_RATE = 0.04;
	private final static double DEBT_RATE = 0.12;

	// period
	private final static double BANK_PERIOD_RATE = 0.01;
	private final static double DEBT_PERIOD_RATE = 0.03;
	private final static double TAX_RATE = 0.19;
	private final static double RESELL_RATE = 0.5;

	private final static int MIN_LOAN = 800000, MAX_LOAN = 800000;
	private final static int MAX_INST = (int) (800000 * (DEBT_RATE + 1));

	private final static int TOTAL_PERIODS = 5;

	private final UnitPriceFun upriceFun;
	private final PercSoldFun percSoldFun;

	private final Constraints periodConstraints;

	public InvestProblem(UnitPriceFun upriceFun, PercSoldFun percSoldFun,
			Constraints periodConstraints) {
		super(VAR_NUM, OBJ_NUM, CONS_NUM);
		this.upriceFun = upriceFun;
		this.percSoldFun = percSoldFun;
		this.periodConstraints = periodConstraints;
	}

	@Override
	public void evaluate(Solution solution) {
		int[] vars = EncodingUtils.getInt(solution);
		int vol = vars[0];
		int qual = AD_MULTI * vars[1];
		int tv = AD_MULTI * vars[2];
		int internet = AD_MULTI * vars[3];
		int warehouse = vars[4];
		int price = vars[5];
		int loan = vars[6];
		int inst = vars[7]; // debt instalment

		double soldRate = percSoldFun.compute(qual, price, new Advertisments(
				tv, internet, warehouse));

		// Constraints
		/*
		 * 0 - cash , 1 - debt , 2 - max instalment , 3 - positive netincome
		 */

		double[] constraints = new double[4];

		int period = periodConstraints.period;
		int cash = periodConstraints.cash;
		int debt = periodConstraints.debt;
		int totalDebt = debt + loan;

		int totalCash = cash + loan;
		int totalExpenses = totalExpensesFunc(vars);

		// you cannot spend more than you have
		if (totalCash < totalExpenses) {
			constraints[0] = totalCash - totalExpenses;
		} else {
			constraints[0] = 0;
		}

		// total debt cannot go higher than max
		if (totalDebt > MAX_DEBT) {
			constraints[1] = totalDebt - MAX_DEBT;
		} else {
			constraints[1] = 0;
		}

		// the instalment has to be in specific range
		int minInst = calcMinInstalment(period, totalDebt);
		if (inst < minInst || inst > calcMaxInstalment(period, totalDebt)) {
			constraints[2] = minInst - inst;
		} else {
			constraints[2] = 0;
		}

		// netIncome has to be positive
		int netIncome = netIncomeFunc(vars, totalExpenses);
		if (netIncome <= 0) {
			constraints[3] = netIncome;
		} else {
			constraints[3] = 0;
		}

		solution.setConstraints(constraints);

		// Objectives
		double risk = 1 - soldRate;

		solution.setObjective(0, -1 * netIncome);
		solution.setObjective(1, risk);

	}

	public int netIncomeFunc(int[] vars) {
		return netIncomeFunc(vars, totalExpensesFunc(vars));
	}

	@Override
	public Solution newSolution() {
		Solution solution = new Solution(VAR_NUM, OBJ_NUM, CONS_NUM);
		solution.setVariable(0, EncodingUtils.newInt(MIN_VOL, MAX_VOL)); // volume
		solution.setVariable(1, EncodingUtils.newInt(MIN_QUAL, MAX_QUAL)); // quality
		for (int i = 0; i < 3; i++) {
			solution.setVariable(i + 2, EncodingUtils.newInt(MIN_AD, MAX_AD)); // tv/internet/warehouse
		}
		solution.setVariable(5, EncodingUtils.newInt(MIN_PRICE, MAX_PRICE)); // price

		int period = periodConstraints.period;
		if (period == TOTAL_PERIODS) {
			solution.setVariable(6, EncodingUtils.newInt(0, 0)); // loan
		} else {
			solution.setVariable(6, EncodingUtils.newInt(MIN_LOAN, MAX_LOAN)); // loan
		}

		int minInstalment = calcMinInstalment(periodConstraints.period,
				periodConstraints.debt);

		solution.setVariable(7, EncodingUtils.newInt(minInstalment, MAX_INST)); // debt
		// payoff

		return solution;
	}

	private int calcMaxInstalment(int period, int debt) {
		int periodsLeft = TOTAL_PERIODS - period + 1;
		double interests = debt / periodsLeft;
		interests *= DEBT_PERIOD_RATE;
		return (int) Math.ceil(interests + debt);
	}

	private int calcMinInstalment(int period, int debt) {
		int periodsLeft = TOTAL_PERIODS - period + 1;
		double minInstalment = debt / periodsLeft;
		minInstalment += (debt * DEBT_PERIOD_RATE);
		return (int) Math.ceil(minInstalment);
	}

	private int netIncomeFunc(int[] vars, int totalExpenses) {
		int vol = vars[0];
		int qual = vars[1];
		int tv = AD_MULTI * vars[2];
		int internet = AD_MULTI * vars[3];
		int warehouse = AD_MULTI * vars[4];
		int price = vars[5];
		int loan = vars[6];
		int inst = vars[7];

		double soldRatio = percSoldFun.compute(qual, price, new Advertisments(
				tv, internet, warehouse));

		int soldNum = (int) Math.floor(soldRatio * vol);

		double unitPrice = upriceFun.compute(vol, qual);

		int cashLeft = periodConstraints.cash + loan - totalExpenses;

		int grossIncome = 0;

		// INCOME
		grossIncome += (soldNum * price); // gross sale income
		// resold units
		grossIncome += (Math.floor((vol - soldNum) * unitPrice * RESELL_RATE));
		// bank interests
		grossIncome += (Math.floor(cashLeft * BANK_PERIOD_RATE));

		// EXPENDITURE
		grossIncome -= totalExpenses;
		grossIncome -= AMORT;

		int netIncome = (int) Math.floor(grossIncome * (1 - TAX_RATE)); // taxation

		if (netIncome > 350000) {
			System.out.println(qual + " " + price + " " + soldRatio);
			System.out.println(tv + " " + internet + " " + warehouse);
		}
		return netIncome;
	}

	public int totalExpensesFunc(int[] vars) {
		int vol = vars[0];
		int qual = vars[1];
		int tv = AD_MULTI * vars[2];
		int internet = AD_MULTI * vars[3];
		int warehouse = AD_MULTI * vars[4];
		int loan = vars[6];
		int inst = vars[7]; // debt instalment

		double unitPrice = upriceFun.compute(vol, qual);

		int ads = tv + internet + warehouse;
		int productionCost = (int) Math.floor(vol * unitPrice);
		productionCost += CONST_COST;

		int totalExpenses = inst + ads + productionCost;
		return totalExpenses;
	}
}
