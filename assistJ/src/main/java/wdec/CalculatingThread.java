package wdec;

import javafx.application.Platform;
import javafx.concurrent.Task;
import opt.Constraints;
import opt.Decision;
import opt.Solver;

public class CalculatingThread
{
	int debt, period, cash;
	Thread thread;
	AfterCalcInterface listener;
	Decision decision;

	public CalculatingThread(int debt, int period, int cash, AfterCalcInterface listener)
	{
		this.debt = debt;
		this.period = period;
		this.cash = cash;
		this.listener = listener;
				
	     Task<Void> task = new Task<Void>() {
	         @Override protected Void call() throws Exception {
	        	 
	        	Solver solver = new Solver();
	     		Constraints constraints = new Constraints(debt, period, cash);
	     		decision = solver.solveBestInc(constraints);
	     		fireAfterCalc();
	             return null;
	         }
	     };
	     
	     new Thread(task).start();
	}

	private void fireAfterCalc()
	{
		Platform.runLater(new Runnable() {
			@Override
			public void run()
			{
				listener.fillTextFields(decision); 
				
			}
		});
		
	}	  
}