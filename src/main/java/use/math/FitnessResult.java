package use.math;

import java.util.HashMap;

import common.Static;

import erne.AbstractFitnessResult;

public class FitnessResult extends AbstractFitnessResult {
	public HashMap<String, Double> errors;
	public double target_max;

	public HashMap<String, Double> targets = new HashMap<String, Double>();
	public HashMap<String, Double> actuals = new HashMap<String, Double>();
	public double scale = 1;

	public double[] targetFittedParams;
	public double[] actualFittedParams;

	@Override
	public double getFitness() {
		double result = 0;
		for (double error : errors.values()) {
			result += Math.pow(error, 2);// modified to square by YR
		}
		result = 10000 / Math.max(result, 0.01);
		if (targetFittedParams != null) {
			for (int i = 0; i < targetFittedParams.length; i++) {
				result = result
						* Math.exp(-(actualFittedParams[i] - targetFittedParams[i])
								* (actualFittedParams[i] - targetFittedParams[i])
								/ (targetFittedParams[i]
										* targetFittedParams[i] / 100));
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(Static.df4.format(getFitness()));
		builder.append("(scale=" + Static.df4.format(scale) + ")");

		builder.append("=10000/(");
		int i = 0;
		for (double error : errors.values()) {
			builder.append("("
					+ (error == Double.MAX_VALUE ? "MAX_VALUE" : Static.df4
							.format(error)) + ")^2 ");// mod YR
			i++;
			if (i < errors.size() - 1) {
				builder.append(" + ");
			}
		}
		builder.append(") ");
		if (targetFittedParams != null) {
			for (int j = 0; j < targetFittedParams.length; j++) {
				builder.append(actualFittedParams[j] + " ");
			}
		}
		return builder.toString();
	}

	@Override
	public boolean isSatisfied() {
		for (double error : errors.values()) {
			if (Math.abs(error / target_max) > FitnessFunction.stoppingError) {
				return false;
			}
		}
		for (int i = 0; i < targetFittedParams.length; i++) {
			if (Math.abs(targetFittedParams[i] - actualFittedParams[i])
					/ targetFittedParams[i] > FitnessFunction.stoppingError) {
				return false;
			}
		}
		return true;
	}
}
