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
	static final double[] targetCoeff = new double[] { 50, (Math.log(100) + Math.log(0.1)) / 2, 0.7 };

	@Override
	protected FitnessResult calculateFitnessResult(ArrayList<double[]> tests, double[] actualOutputs) {
		FitnessResult result = new FitnessResult(false);

		result.tests = tests;
		result.actualOutputs = actualOutputs;

		double[] targetOutputs = new double[tests.size()];
		for (int i = 0; i < tests.size(); i++) {
			targetOutputs[i] = targetCoeff[0]
					* Math.exp((-Math.pow(Math.log(tests.get(i)[0]) - targetCoeff[1], 2)) / (2 * Math.pow(targetCoeff[2], 2)));
		}
		result.targetOutputs = targetOutputs;

		result.targetFittingParams = targetCoeff;
		result.actualFittingParams = new double[] { targetCoeff[0], targetCoeff[1], targetCoeff[2] };
		return result;

	}

	public static void main(String[] args) {
		System.out.println((Math.log(100) - Math.log(0.1)) / 2);
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		obs.add(1.0, 0.0000001);
		obs.add(6.444444444444445, 0.0000001);
		obs.add(11.88888888888889, 0.0000001);
		obs.add(17.333333333333336, 0.0000001);
		obs.add(22.77777777777778, 0.0000001);
		obs.add(28.22222222222222, 0.0000001);
		obs.add(33.66666666666667, 0.0000001);
		obs.add(39.111111111111114, 0.0000001);
		obs.add(44.55555555555556, 0.0000001);
		obs.add(50.0, 0.0000001);

		final GaussianCurveFitter fitter = GaussianCurveFitter.create().withMaxIterations(1000);

		final double[] coeff = fitter.fit(obs.toList());
		System.out.println(Arrays.toString(coeff));
	}
}
