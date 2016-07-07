package model;

import org.apache.commons.math3.ode.events.EventHandler;

public class TimeOutEventHandler implements EventHandler {

	private int fireTime = -1;
	private int unit = 1000; //fireTime is in seconds
	private long startTime;
	private double lastT = -1;

	
	@Override
	public void init(double t0, double[] y0, double t) {
		startTime = System.currentTimeMillis();
	}

	@Override
	public double g(double t, double[] y) {
		double spentTime = System.currentTimeMillis() - startTime;
		if(fireTime > -1 && lastT == -1 && spentTime > fireTime*unit){
			lastT = t; // trick to have continuity
			System.err.println("Evaluation timed out: spentTime "+spentTime+" lastT "+lastT+" fireTime "+fireTime);
		}
		
		return fireTime == -1 || lastT<0.0? 1 : (lastT - t + 1);
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