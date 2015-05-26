package wdec;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.concurrent.Task;
import opt.Constraints;
import opt.Decision;
import opt.Solver;

public class CalculatingThread {
	int debt, period, cash;
	Thread thread;
	CalcInterface listener;
	List<Decision> decisions;
	Thread glowThread;
	AtomicBoolean glowFlag = new AtomicBoolean(false);

	public CalculatingThread(int debt, int period, int cash,
			CalcInterface listener) {
		this.debt = debt;
		this.period = period;
		this.cash = cash;
		this.listener = listener;

		Task<Void> calculations = new Task<Void>() {
			@Override
			protected Void call() throws Exception {

				Solver solver = new Solver();
				Constraints constraints = new Constraints(debt, period, cash);
				decisions = solver.solve(constraints);
				fireAfterCalc();
				return null;
			}
		};

		new Thread(calculations).start();

		glowThread = new Thread(() -> {
			while (glowFlag.get()) {
				calcGlow();
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
		glowFlag.set(true);
		glowThread.start();

	}

	private void fireAfterCalc() {
		glowFlag.set(false);
		Platform.runLater(() -> listener.addDecisions(decisions));
	}

	private void calcGlow() {
		Platform.runLater(listener::calcGlow);
	}

}