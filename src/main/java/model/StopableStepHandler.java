package model;

import java.util.ArrayList;
import model.Constants;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

public class StopableStepHandler implements StepHandler {
	private ArrayList<double[]> timeSerie;
	private int time = 0;
	private int minTimeStable = 100;
	private int timeToCheckStability = 50;
	private double changeThresholdStable = 5E-3;
	private int startedStable;
	private StopableEventHandler myEventHandler;
	private boolean timedOut = false;

	public StopableStepHandler(StopableEventHandler myEventHandler) {
		this.myEventHandler = myEventHandler;
	}

	@Override
	public void handleStep(StepInterpolator step, boolean isLastStep) {
		StepInterpolator localCopy = step.copy();
		//First of all, did we time out?
		if(isLastStep && step.getCurrentTime() < timeToCheckStability){
			timedOut = true;
		}
		if (step.getCurrentTime() >= time + 1 && time < Constants.numberOfPoints - 1) {
			time++;
			localCopy.setInterpolatedTime(time);
			double[] y0 = localCopy.getInterpolatedState();
			double[] expression = new double[y0.length];
			for (int i = 0; i < y0.length; i++) {
				expression[i] = y0[i];
			}
			timeSerie.add(expression);

			if (erne.Constants.stabilityCheck && time > minTimeStable - timeToCheckStability) {
				boolean stable = true;
				for (int i = 0; i < expression.length; i++) {
					if (Math.abs(expression[i] - timeSerie.get(time - 1)[i]) > changeThresholdStable) {
						stable = false;
						break;
					}
				}
				if (stable) {
					if (startedStable == 0) {
						startedStable = time;
					} else if (time > startedStable + timeToCheckStability) {
						myEventHandler.setFire(time);
					}
				} else {
					startedStable = 0;
					if(isLastStep && step.getCurrentTime() < erne.Constants.maxEvalTime){
						//We aren't stable and we aren't at the end
						timedOut = true;
						System.err.println("Evaluation cancelled");
					}
				}
			}
		}


	}

	@Override
	public void init(double t0, double[] y0, double t) {
		timeSerie = new ArrayList<double[]>();
		double[] expression = new double[y0.length];
		for (int i = 0; i < y0.length; i++) {
			expression[i] = y0[i];
		}
		timeSerie.add(expression);
	}

	public double[][] getTimeSerie() {
		double[][] timeSeries = new double[timeSerie.get(0).length][timeSerie.size()];
		if(!timedOut){
			for (int i = 0; i < timeSerie.size(); i++) {
				double[] expression = timeSerie.get(i);
				for (int j = 0; j < expression.length; j++) {
					timeSeries[j][i] = expression[j];
				}
			}
		} else {
			System.err.println("ERROR: timed out");
		}
		return timeSeries;
	}
}
