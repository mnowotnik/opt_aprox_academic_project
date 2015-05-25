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
	CalcInterface listener;
	Decision decision;
	Thread glowThread;

	public CalculatingThread(int debt, int period, int cash, CalcInterface listener)
	{
		this.debt = debt;
		this.period = period;
		this.cash = cash;
		this.listener = listener;
				
	     Task<Void> calculations = new Task<Void>() {
	         @Override protected Void call() throws Exception {
	        	 
	        	Solver solver = new Solver();
	     		Constraints constraints = new Constraints(debt, period, cash);
	     		decision = solver.solveBestInc(constraints);
	     		fireAfterCalc();
	             return null;
	         }
	     };
	     
	     new Thread(calculations).start();
	     
	     Task<Void> glow = new Task<Void>() {
	         @Override protected Void call() throws Exception {
	        	 while(true)
	        	 {
	        		 calcGlow();
	        		 Thread.sleep(20);
	        	 }
	         }
	     };
	     
	     glowThread = new Thread(glow);
	     glowThread.start();
	     
	}

	private void fireAfterCalc()
	{
		glowThread.stop();
		Platform.runLater(new Runnable() {
			@Override
			public void run()
			{
				listener.fillTextFields(decision); 
				
			}
		});
	}	  
	
	private void calcGlow()
	{
		Platform.runLater(new Runnable() {
			@Override
			public void run()
			{
				listener.calcGlow();
			}
		});
	}
	
}