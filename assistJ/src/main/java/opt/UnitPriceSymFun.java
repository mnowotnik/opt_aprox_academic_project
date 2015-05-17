package opt;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Program;
import org.moeaframework.util.tree.Environment;

public class UnitPriceSymFun {

	private final Solution solution;
	public UnitPriceSymFun(Solution solution) {
		super();
		this.solution = solution;
	}
	public double compute(double volume,double quality){
		Environment env = new Environment();
		env.set("0", volume);
		env.set("1", quality);
		Program program = (Program) solution.getVariable(0);
		Number res=(Number) program.evaluate(env);
		return res.doubleValue();
	}
}
