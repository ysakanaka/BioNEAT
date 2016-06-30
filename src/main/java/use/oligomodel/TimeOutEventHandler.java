package use.oligomodel;

import org.apache.commons.math3.ode.events.EventHandler;

public class TimeOutEventHandler implements EventHandler {

	private int fireTime = -1;
	private int unit = 1000; //fireTime is in seconds
	private long startTime;

	
	@Override
	public void init(double t0, double[] y0, double t) {
		startTime = System.currentTimeMillis();
	}

	@Override
	public double g(double t, double[] y) {
		return this.fireTime == -1 ? 1 : (fireTime*unit - startTime + 1);
	}

	@Override
	public Action eventOccurred(double t, double[] y, boolean increasing) {
		return EventHandler.Action.STOP;
	}

	@Override
	public void resetState(double t, double[] y) {
		startTime = System.currentTimeMillis();
	}

	public void setFire(int time) {
		this.fireTime = time;
	}

}