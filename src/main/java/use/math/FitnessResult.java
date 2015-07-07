package use.math;

import java.util.ArrayList;

import common.Static;
import erne.AbstractFitnessResult;

public class FitnessResult extends AbstractFitnessResult {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double[] inputs;
	public double[] targetOutputs;
	public double[] actualOutputs;
	public double[] targetFittingParams;
	public double[] actualFittingParams;
	public ArrayList<double[]> tests;

	public boolean minFitness = false;

	public FitnessResult(boolean minFitness) {
		this.minFitness = minFitness;
	}

	@Override
	public double getFitness() {
		if (minFitness)
			return 0;
		double result = 0;
		for (int i = 0; i < actualOutputs.length; i++) {
			double error = Math.abs(actualOutputs[i] - targetOutputs[i]);
			result += Math.pow(error, 2);
		}
		result = 100 / Math.max(result, 1);

		double result1 = 1000000;
		if (targetFittingParams != null) {
			for (int i = 0; i < targetFittingParams.length; i++) {
				result1 /= Math.max(1, Math.abs(10 * (actualFittingParams[i] - targetFittingParams[i]) / targetFittingParams[i]));
			}
		}
		result += result1;

		result *= getSDV(actualOutputs);

		return result;
	}

	private double getSDV(double[] actualOutputs) {
		double mean = 0;
		for (int i = 0; i < actualOutputs.length; i++) {
			mean += actualOutputs[i];
		}
		mean /= actualOutputs.length;
		double sdv = 0;
		for (int i = 0; i < actualOutputs.length; i++) {
			sdv += Math.pow(actualOutputs[i] - mean, 2);
		}
		return Math.sqrt(sdv / actualOutputs.length);
	}

	@Override
	public String toString() {
		if (minFitness)
			return "MIN_FITNESS";
		StringBuilder builder = new StringBuilder();
		builder.append(getFitness());

		builder.append(" Outputs[target]:");
		for (int i = 0; i < actualOutputs.length; i++) {
			builder.append(Static.df2.format(actualOutputs[i]) + "[" + Static.df2.format(targetOutputs[i]) + "] ");
		}
		if (targetFittingParams != null) {
			builder.append("Fitting params[target]: ");
			for (int j = 0; j < targetFittingParams.length; j++) {
				builder.append(Static.df4.format(actualFittingParams[j]) + "[" + Static.df4.format(targetFittingParams[j]) + "] ");
			}
		}
		builder.append("SDV: " + Static.df4.format(getSDV(actualOutputs)));
		return builder.toString();
	}
}
