package use.oligomodel;

import org.apache.commons.math3.ode.events.EventHandler;

public class StopableEventHandler implements EventHandler {

	private int fireTime = -1;

	@Override
	public void init(double t0, double[] y0, double t) {
	}

	@Override
	public double g(double t, double[] y) {
		return this.fireTime == -1 ? 1 : (fireTime - t + 1);
	}

	@Override
	public Action eventOccurred(double t, double[] y, boolean increasing) {
		return EventHandler.Action.STOP;
	}

	@Override
	public void resetState(double t, double[] y) {

	}

	public void setFire(int time) {
		this.fireTime = time;
	}

}
