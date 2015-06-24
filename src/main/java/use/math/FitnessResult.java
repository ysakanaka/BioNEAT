package use.math;

import java.util.ArrayList;

import common.Static;
import erne.AbstractFitnessResult;

public class FitnessResult extends AbstractFitnessResult {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
		result = 10000 / Math.max(result, 0.01);

		if (targetFittingParams != null) {
			for (int i = 0; i < targetFittingParams.length; i++) {
				result = result
						* Math.exp(-(actualFittingParams[i] - targetFittingParams[i]) * (actualFittingParams[i] - targetFittingParams[i])
								/ (actualFittingParams[i] * targetFittingParams[i] / 100));
			}
		}
		return result;
	}

	@Override
	public String toString() {
		if (minFitness)
			return "MIN_FITNESS";
		StringBuilder builder = new StringBuilder();
		builder.append(Static.df4.format(getFitness()));

		builder.append("=10000/(");
		for (int i = 0; i < actualOutputs.length; i++) {
			double error = Math.abs(actualOutputs[i] - targetOutputs[i]);
			builder.append("(" + (error == Double.MAX_VALUE ? "MAX_VALUE" : Static.df4.format(error)) + ")^2 ");
			if (i < actualOutputs.length - 1) {
				builder.append(" + ");
			}
		}
		builder.append(") ");
		if (targetFittingParams != null) {
			builder.append("Fitting params [target]: ");
			for (int j = 0; j < targetFittingParams.length; j++) {
				builder.append(Static.df4.format(actualFittingParams[j]) + "[" + Static.df4.format(targetFittingParams[j]) + "] ");
			}
		}
		return builder.toString();
	}
}
