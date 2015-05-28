package opt;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

public class InvestProblem extends AbstractProblem {

	/**
	 * maksymalne zadłużenie
	 */
	private final static double MAX_DEBT = 800000;
	/**
	 * amortyzacja
	 */
	private final static double AMORT = 20000;
	/**
	 * koszty stałe produkcji
	 */
	private final static double CONST_COST = 10000;

	/**
	 * ilość zmiennych
	 */
	private final static int VAR_NUM = 8;
	/**
	 * ilość optymalizowanych zmiennych celu
	 */
	private final static int OBJ_NUM = 2;
	/**
	 * ilość ograniczeń
	 */
	private final static int CONS_NUM = 4;

	/**
	 * maksymalny wolumen
	 */
	private final static int MAX_VOL = 400000;
	private final static int MIN_VOL = 0;

	/**
	 * mininmalna/maksymalna jakość
	 */
	private final static int MIN_QUAL = 20, MAX_QUAL = 88;

	/**
	 * minimalna maksymalna reklama ( z mnożnikiem 1000)
	 */
	private final static int MIN_AD = 0, MAX_AD = 1000;// 0 - 10^6
	private final static int AD_MULTI = 1000;

	/**
	 * minimalna maksymalna cena
	 */
	private final static int MIN_PRICE = 1, MAX_PRICE = 24;

	// year
	/**
	 * oprocentowanie roczne banku i kredytu
	 */
	private final static double BANK_RATE = 0.04;
	private final static double DEBT_RATE = 0.12;

	// period
	/**
	 * oprocentowanie kwartalne banku i kredytu
	 */
	private final static double BANK_PERIOD_RATE = 0.01;
	private final static double DEBT_PERIOD_RATE = 0.03;
	/**
	 * podatek
	 */
	private final static double TAX_RATE = 0.19;
	/**
	 * ułamek kosztu jednostkowego dla odsprzedaży
	 */
	private final static double RESELL_RATE = 0.5;

	/**
	 * maksymalna rata
	 */
	private final static int MAX_INST = (int) (800000 * (DEBT_RATE + 1));

	/**
	 * ilość kwartałów
	 */
	private final static int TOTAL_PERIODS = 5;

	/**
	 * @param period
	 * @param debt
	 * @return minimalna wysokość raty
	 */
	public static int calcMinInstalment(int period, int debt) {
		int periodsLeft = TOTAL_PERIODS - period + 1;
		double minInstalment = debt / periodsLeft;
		minInstalment += (debt * DEBT_PERIOD_RATE);
		return (int) Math.ceil(minInstalment);
	}
	/**
	 * aproksymowana funkcja kosztu jednostkowego
	 */
	private final UnitPriceFun upriceFun;

	/**
	 * aproksymowana funkcja procentu sprzenego wolumenu
	 */
	private final PercSoldFun percSoldFun;

	/**
	 * dane wejściowe/ograniczenia - kwartał , dług , gotówka
	 */
	private final Constraints periodConstraints;

	/**
	 * @param upriceFun
	 *            aproksymowana funkcja kosztu sprzedaży
	 * @param percSoldFun
	 *            aproksymowana funkcja procentu sprzedanego wolumenu
	 * @param periodConstraints
	 *            dane wejściowe - dług, pieniądze, kwartał
	 */
	public InvestProblem(UnitPriceFun upriceFun, PercSoldFun percSoldFun,
			Constraints periodConstraints) {
		super(VAR_NUM, OBJ_NUM, CONS_NUM);
		this.upriceFun = upriceFun;
		this.percSoldFun = percSoldFun;
		this.periodConstraints = periodConstraints;
	}

	/**
	 * @param period
	 * @param debt
	 * @return maksymalna wysokość raty
	 */
	public int calcMaxInstalment(int period, int debt) {
		int periodsLeft = TOTAL_PERIODS - period + 1;
		double interests = debt / periodsLeft;
		interests *= DEBT_PERIOD_RATE;
		return (int) Math.ceil(interests + debt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.moeaframework.core.Problem#evaluate(org.moeaframework.core.Solution)
	 * ewaluacja pojedynczego rozwiązania. ustawia ograniczenia i wartości
	 * funkcji celu
	 */
	@Override
	public void evaluate(Solution solution) {
		int[] vars = EncodingUtils.getInt(solution);
		int vol = vars[0];
		int qual = vars[1];
		int tv = AD_MULTI * vars[2];
		vars[2] = tv;
		int internet = AD_MULTI * vars[3];
		vars[3] = internet;
		int warehouse = AD_MULTI * vars[4];
		vars[4] = warehouse;
		int price = vars[5];
		int loan = (int) Math.floor((MAX_DEBT - periodConstraints.debt)
				* vars[6] / 100);

		vars[6] = loan;

		int debt = periodConstraints.debt;
		int period = periodConstraints.period;
		int totalDebt = debt + loan;
		int inst = vars[7] + calcMinInstalment(period, totalDebt);

		// debt instalment
		vars[7] = inst;

		double soldRate = percSoldFun.compute(qual, price, new Advertisments(
				tv, internet, warehouse));

		// Constraints
		/*
		 * 0 - cash , 1 - debt , 2 - max instalment , 3 - positive netincome
		 */

		double[] constraints = new double[CONS_NUM];

		int cash = periodConstraints.cash;

		int totalCash = cash + loan - inst;
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
		if (inst > calcMaxInstalment(period, totalDebt)) {
			constraints[2] = inst - calcMaxInstalment(period, totalDebt);
		} else {
			constraints[2] = 0;
		}

		int report[] = new int[3];
		int netIncome = netIncomeFunc(vars, report);
		if (netIncome <= 0) {
			constraints[3] = netIncome;
		} else {
			constraints[3] = 0;
		}

		solution.setAttribute("grossSalesIncome", report[0]);
		solution.setAttribute("primeCosts", report[1]);
		solution.setAttribute("salesIncome", report[2]);

		double unitPrice = upriceFun.compute(vol, qual);
		solution.setAttribute("unitPrice", unitPrice);

		solution.setConstraints(constraints);

		// Objectives
		double risk = 1 - soldRate;

		solution.setObjective(0, -1 * netIncome);
		solution.setObjective(1, risk);

	}

	/**
	 * @param vars
	 *            : 0 - wolumen, 1 - jakość, 2 - tv, 3 - internet, 4 - magazyny,
	 *            5 - cena, 6 - kredyt, 7 - rata
	 * @return przyszłe wydatki pod koniec kwartału = obecne wydatki +
	 *         amortyzacja
	 */
	public int futureExpensesFunc(int[] vars) {
		return totalExpensesFunc(vars) + (int) AMORT;
	}

	/**
	 * @param vars
	 *            : 0 - wolumen, 1 - jakość, 2 - tv, 3 - internet, 4 - magazyny,
	 *            5 - cena, 6 - kredyt, 7 - rata
	 * @return realny zysk netto po opodatkowaniu i odjęciu należności
	 */
	public int netIncomeFunc(int[] vars) {
		return netIncomeFunc(vars, new int[0]);
	}

	/**
	 * @param vars
	 *            : 0 - wolumen, 1 - jakość, 2 - tv, 3 - internet, 4 - magazyny,
	 *            5 - cena, 6 - kredyt, 7 - rata
	 * @param report
	 *            0 - dochód brutto, 1 - koszty własne , 2 - netto po odjęciu
	 *            kosztów własnych
	 * @return realny zysk netto po opodatkowaniu i odjęciu należności
	 */
	public int netIncomeFunc(int[] vars, int[] report) {
		int totalExpenses = futureExpensesFunc(vars);
		int vol = vars[0];
		int qual = vars[1];
		int tv = vars[2];
		int internet = vars[3];
		int warehouse = vars[4];
		int price = vars[5];
		int loan = vars[6];
		int inst = vars[7];

		double soldRatio = percSoldFun.compute(qual, price, new Advertisments(
				tv, internet, warehouse));

		int soldNum = (int) Math.floor(soldRatio * vol);

		double unitPrice = upriceFun.compute(vol, qual);

		int cashLeft = periodConstraints.cash + loan - totalExpenses - inst;

		int grossIncome = 0;

		// INCOME
		grossIncome += (soldNum * price); // gross sale income
		// resold units
		grossIncome += (Math.floor((vol - soldNum) * unitPrice * RESELL_RATE));

		if (report.length == 3) {
			int primeCosts = totalExpenses;
			report[0] = grossIncome;
			report[1] = primeCosts;
			report[2] = grossIncome - primeCosts;
		}
		// bank interests
		grossIncome += (Math.floor(cashLeft * BANK_PERIOD_RATE));

		// EXPENDITURE
		grossIncome -= totalExpenses;

		int netIncome = grossIncome;
		if (netIncome > 0) {
			netIncome = (int) Math.floor(netIncome * (1 - TAX_RATE)); // taxation
		}

		return netIncome;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moeaframework.core.Problem#newSolution() tworzy nowe rozwiązanie
	 * dla algorytmu z biblioteki MOEA
	 */
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
			solution.setVariable(6, EncodingUtils.newInt(0, 100)); // loan
		}

		solution.setVariable(7, EncodingUtils.newInt(0, MAX_INST)); // debt
		// payoff

		return solution;
	}

	/**
	 * @param vars
	 *            : 0 - wolumen, 1 - jakość, 2 - tv, 3 - internet, 4 - magazyny,
	 *            5 - cena, 6 - kredyt, 7 - rata
	 * @return całkowite wydatki : reklamy + koszty produkcji
	 */
	public int totalExpensesFunc(int[] vars) {
		int vol = vars[0];
		int qual = vars[1];
		int tv = vars[2];
		int internet = vars[3];
		int warehouse = vars[4];

		double unitPrice = upriceFun.compute(vol, qual);

		int ads = tv + internet + warehouse;
		int productionCost = (int) Math.ceil(vol * unitPrice);
		productionCost += CONST_COST;

		int totalExpenses = ads + productionCost;
		return totalExpenses;
	}
}
