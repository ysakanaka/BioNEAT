package model.util;

import java.util.ArrayList;

import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

public class MyStepHandler implements StepHandler {

	protected double[][] timeSerie;
	protected int time = 0;
	protected int startedStable = 0;
	protected int sizeSimpleSeq;
	protected MyEventHandler myEventHandler;
	protected boolean simple = false;
	protected int repetitions = 1;
	protected long stepCount = 0;
	public ArrayList<Integer> reporters = new ArrayList<Integer>();

	private long maxIntegrationSteps = Long.MAX_VALUE;

	public MyStepHandler(int sizeSimpleSeq, MyEventHandler myEventHandler) {
		this.sizeSimpleSeq = sizeSimpleSeq;
		this.myEventHandler = myEventHandler;
	}

	public MyStepHandler(int sizeSimpleSeq, MyEventHandler myEventHandler,
			boolean simple) {
		this.sizeSimpleSeq = sizeSimpleSeq;
		this.myEventHandler = myEventHandler;
		this.simple = simple;
	}

	public MyStepHandler(int sizeSimpleSeq, MyEventHandler myEventHandler,
			int numberOfSystems) {
		// TODO Auto-generated constructor stub
		this.sizeSimpleSeq = sizeSimpleSeq;
		this.myEventHandler = myEventHandler;
		this.repetitions = numberOfSystems;
	}

	public void setMaxIntegrationSteps(long value) {
		this.maxIntegrationSteps = value;
	}

	public void handleStep(StepInterpolator step, boolean isLastStep) {
		if (stepCount++ > maxIntegrationSteps) {
			throw new MaxCountExceededException(stepCount);
		}
		StepInterpolator localCopy = step.copy();
		if (step.getCurrentTime() >= time + 1
				&& time < model.Constants.numberOfPoints - 1) {
			time++;
			localCopy.setInterpolatedTime(time);
			double[] y0 = localCopy.getInterpolatedState();
			int offset = 0;
			int dim = Math.min(sizeSimpleSeq, y0.length);
			int sizeIndiv = y0.length / this.repetitions;
			for (int repet = 0; repet < this.repetitions; repet++) {
				for (int i = 0; i < dim; i++) {
					this.timeSerie[i + offset][time] = y0[i + repet * sizeIndiv];
				}
				for (int i = 0; i < reporters.size(); i++) {
					this.timeSerie[dim + offset + i][time] = y0[reporters
							.get(i) + repet * sizeIndiv];
				}
				offset += dim + reporters.size();
			}
		}

	}

	public void init(double t0, double[] y0, double t) {
		// Be careful that this is only valid if the init is done for time t=0
		this.timeSerie = new double[this.repetitions
				* Math.min(sizeSimpleSeq + reporters.size(), y0.length)][(this.simple ? model.Constants.maxTimeSimple
				: model.Constants.numberOfPoints)];
		// System.out.println(Math.min(sizeSimpleSeq,y0.length));

		int offset = 0;
		int dim = Math.min(sizeSimpleSeq, y0.length);
		int sizeIndiv = y0.length / this.repetitions;
		for (int repet = 0; repet < this.repetitions; repet++) {
			for (int i = 0; i < dim; i++) {
				this.timeSerie[i + offset][0] = y0[i + repet * sizeIndiv];
			}
			for (int i = 0; i < reporters.size(); i++) {
				this.timeSerie[dim + offset + i][0] = y0[reporters.get(i)
						+ repet * sizeIndiv];
			}
			offset += dim + reporters.size();
		}
	}

	public double[][] getTimeSerie() {
		return this.timeSerie.clone();
	}
}
