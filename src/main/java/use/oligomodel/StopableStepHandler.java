package use.oligomodel;

import java.util.ArrayList;

import model.Constants;

import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import utils.MyStepHandler;
import utils.PluggableWorker;

public class StopableStepHandler implements StepHandler {
	private ArrayList<double[]> timeSerie;
	private int time = 0;
	private int minTimeStable = 100;
	private int timeToCheckStability = 50;
	private double changeThresholdStable = 5E-3;
	private int startedStable;
	private StopableEventHandler myEventHandler;

	public StopableStepHandler(StopableEventHandler myEventHandler) {
		this.myEventHandler = myEventHandler;
	}

	@Override
	public void handleStep(StepInterpolator step, boolean isLastStep) {
		StepInterpolator localCopy = step.copy();
		if (step.getCurrentTime() >= time + 1 && time < Constants.numberOfPoints - 1) {
			// while(time<step.getCurrentTime()&&
			// time<Constants.numberOfPoints-1){
			time++;
			// worker.firePropertyChange("progress", time-1, time);

			// Thread.yield();
			// prog.setValue(time);
			// prog.repaint();
			// monitor.repaint();
			localCopy.setInterpolatedTime(time);
			// System.out.println("Time "+time);
			double[] y0 = localCopy.getInterpolatedState();
			double[] expression = new double[y0.length];
			for (int i = 0; i < y0.length; i++) {
				expression[i] = y0[i];
			}
			timeSerie.add(expression);

			if (time > minTimeStable - timeToCheckStability) {
				boolean stable = true;
				// System.out.println(t+" "+timeSeries[0][t]);
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
				}
			}
			// }
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
		for (int i = 0; i < timeSerie.size(); i++) {
			double[] expression = timeSerie.get(i);
			for (int j = 0; j < expression.length; j++) {
				timeSeries[j][i] = expression[j];
			}
		}
		return timeSeries;
	}
}
