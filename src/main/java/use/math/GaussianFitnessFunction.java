package use.math;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class GaussianFitnessFunction extends AbstractMathFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static final double[] targetCoeff = new double[] { 50, 2, 0.5 };

	@Override
	protected FitnessResult calculateFitnessResult(ArrayList<double[]> tests, double[] actualOutputs) {

		double[] targetOutputs = new double[tests.size()];

		final WeightedObservedPoints obs = new WeightedObservedPoints();
		for (int i = 0; i < tests.size(); i++) {
			obs.add(tests.get(i)[0], actualOutputs[i]);
			targetOutputs[i] = 0.02 * tests.get(i)[0] * tests.get(i)[0];
		}

		final GaussianCurveFitter fitter = GaussianCurveFitter.create();

		final double[] coeff = fitter.fit(obs.toList());

		FitnessResult result = new FitnessResult(false);

		result.tests = tests;
		result.actualOutputs = actualOutputs;
		result.targetOutputs = targetOutputs;
		result.targetFittingParams = targetCoeff;
		result.actualFittingParams = coeff;
		return result;

	}

	public static void main(String[] args) {
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		for (int i = 0; i < 100; i++) {
			obs.add(i, 50 * Math.exp((-Math.pow(Math.log(i) - Math.log(100) / 2, 2)) / 1));
		}

		final GaussianCurveFitter fitter = GaussianCurveFitter.create();

		final double[] coeff = fitter.fit(obs.toList());
		System.out.println(Arrays.toString(coeff));
	}

}
