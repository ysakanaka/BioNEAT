package model.util;

import java.util.HashMap;

import org.apache.commons.math3.ode.events.EventHandler;

import model.Input;
import model.Sequence;

public class MyEventHandler implements EventHandler {

	public Input[] inputs; // the integer is the index in the y state array
	protected int fire = -1;
	protected double value = 1;
	protected int whoseTurn = 0;
	protected boolean stopAtStable = false;

	public MyEventHandler(Input[] inputs) {
		this.inputs = inputs;
		if (inputs != null && inputs.length < 3) {
			whoseTurn = inputs.length - 1; // TODO that is a pretty bad hack
		}
	}

	public MyEventHandler(Input[] inputs, boolean stopAtStable) {
		this(inputs);
		this.stopAtStable = stopAtStable;
	}

	protected boolean additionalStopCondition() {
		return true;
	}

	// @Override
	public Action eventOccurred(double arg0, double[] arg1, boolean arg2) {
		if (stopAtStable && additionalStopCondition()) {
			return EventHandler.Action.STOP;
		}
		whoseTurn++;
		inputs[whoseTurn].timeStep = fire;
		return EventHandler.Action.RESET_STATE;
	}

	// @Override
	public double g(double t, double[] y) {
		if (stopAtStable && additionalStopCondition()) {
			return this.fire == -1 ? this.value : value * (fire - t + 1);
		}
		return (this.fire == -1 || whoseTurn == (inputs.length - 1) ? this.value
				: value * (fire - t + 1));
	}

	// @Override
	public void init(double arg0, double[] arg1, double arg2) {

	}

	// @Override
	public void resetState(double t, double[] y) {
		if (t == fire) {
			y[inputs[whoseTurn].receivingSeq] = y[inputs[whoseTurn].receivingSeq]
					+ inputs[whoseTurn].ammount;
		}

		value = -value;
		fire = -1;
	}

	public void setFire(int time) {
		this.fire = time;

	}

}
