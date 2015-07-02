package use.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class GaussianFitnessFunction extends AbstractMathFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// y= 50 * Math.exp((-Math.pow(i - 25, 2)) / (2 * Math.pow(3.5, 2)))
	static final double[] targetCoeff = new double[] { 50, 25, 3.5 };

	@Override
	protected FitnessResult calculateFitnessResult(ArrayList<double[]> tests, double[] actualOutputs) {

		double[] targetOutputs = new double[tests.size()];

		final WeightedObservedPoints obs = new WeightedObservedPoints();
		for (int i = 0; i < tests.size(); i++) {
			obs.add(tests.get(i)[0], actualOutputs[i]);
			targetOutputs[i] = 50 * Math.exp((-Math.pow(tests.get(i)[0] - 25, 2)) / (2 * Math.pow(3.5, 2)));
		}

		final GaussianCurveFitter fitter = GaussianCurveFitter.create().withMaxIterations(1000);
		double[] coeff = targetCoeff;
		try {
			coeff = fitter.fit(obs.toList());
		} catch (TooManyIterationsException e) {

		}
		FitnessResult result = new FitnessResult(false);

		result.tests = tests;
		result.actualOutputs = actualOutputs;
		result.targetOutputs = targetOutputs;
		result.targetFittingParams = targetCoeff;
		result.actualFittingParams = new double[] { coeff[0], coeff[1], coeff[2] };
		return result;

	}

	public static void main(String[] args) {
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		Random rand = new Random();
		for (int i = 0; i < 100; i++) {
			obs.add(i, 0.0000000000001 * Math.exp((-Math.pow(i - 25, 2)) / (2 * Math.pow(3.5, 2))));
		}

		final GaussianCurveFitter fitter = GaussianCurveFitter.create().withMaxIterations(1000);

		final double[] coeff = fitter.fit(obs.toList());
		System.out.println(Arrays.toString(coeff));
	}
}
