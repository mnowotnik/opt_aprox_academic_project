package opt;

/* Copyright 2009-2013 David Hadka
 *
 * This file is part of the MOEA Framework.
 *
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The MOEA Framework is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
//modified 2015 17-05

import java.util.List;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Program;
import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.util.tree.Add;
import org.moeaframework.util.tree.Cos;
import org.moeaframework.util.tree.Divide;
import org.moeaframework.util.tree.Environment;
import org.moeaframework.util.tree.Exp;
import org.moeaframework.util.tree.Get;
import org.moeaframework.util.tree.Log;
import org.moeaframework.util.tree.Multiply;
import org.moeaframework.util.tree.Node;
import org.moeaframework.util.tree.Power;
import org.moeaframework.util.tree.Rules;
import org.moeaframework.util.tree.Sin;
import org.moeaframework.util.tree.Square;
import org.moeaframework.util.tree.Subtract;

/**
 * The symbolic regression problem for genetic programming. Given a function,
 * the symbolic regression problem attempts to find an expression for closely
 * approximating the output of the function.
 */
public class SymbolicRegression extends AbstractProblem {
	private final DataSet ds;
	private final Rules rules;

	public SymbolicRegression(DataSet ds) {
		super(ds.inputNum, 1);
		this.ds = ds;
		this.rules = newRules();
	}

	public SymbolicRegression(DataSet ds, List<Node> expressions) {
		super(ds.inputNum, 1);
		this.ds = ds;
		this.rules = newRules();
		for (Node n : expressions) {
//			rules.add(n);
		}
	}

	public double[] getApproximatedY(Solution solution) {
		Program program = (Program) solution.getVariable(0);
		double[] approximatedY = new double[ds.rowsNum];

		for (int i = 0; i < ds.rowsNum; i++) {
			Environment environment = new Environment();
			for (int j = 0; j < ds.inputNum; j++) {
				environment.set(Integer.toString(j), ds.getInput()[i][j]);
			}
			approximatedY[i] = ((Number) program.evaluate(environment))
					.doubleValue();
		}

		return approximatedY;
	}

	@Override
	public void evaluate(Solution solution) {
		double difference = 0.0;
		double[] approximatedY = getApproximatedY(solution);

		for (int i = 0; i < ds.rowsNum; i++) {
			difference += Math.pow(
					Math.abs(ds.getOutput()[i][0] - approximatedY[i]), 2.0);
		}

		difference = Math.sqrt(difference);

		// protect against NaN
		if (Double.isNaN(difference)) {
			difference = Double.POSITIVE_INFINITY;
		} else {
			System.out.println(difference);
		}

		solution.setObjective(0, difference);
	}

	public Rules newRules() {
		Rules rules = new Rules();
		rules.add(new Add());
		rules.add(new Multiply());
		rules.add(new Subtract());
		rules.add(new Divide());
		rules.add(new Sin());
		rules.add(new Cos());
		rules.add(new Square());
		rules.add(new Power());
		rules.add(new Exp());
		rules.add(new Log());
		for (int i = 0; i < this.ds.inputNum; i++) {
			rules.add(new Get(Number.class, Integer.toString(i)));
		}
		rules.setReturnType(Number.class);
		rules.setMaxVariationDepth(50);
		rules.setMaxInitializationDepth(10);
		return rules;
	}

	@Override
	public Solution newSolution() {
		Solution solution = new Solution(1, 1);
		Program p = new Program(rules);
		solution.setVariable(0, p);
		return solution;
	}

}
