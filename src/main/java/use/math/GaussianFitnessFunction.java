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
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		for (int i = 0; i < tests.size(); i++) {
			obs.add(tests.get(i)[0], actualOutputs[i]);
			System.out.println(tests.get(i)[0] + " " + actualOutputs[i]);

		}
		double[] coeff = new double[] { 0, 25, 3.5 };
		final GaussianCurveFitter fitter = GaussianCurveFitter.create().withMaxIterations(1000);
		try {
			coeff = fitter.fit(obs.toList());
		} catch (TooManyIterationsException e) {

		}
		FitnessResult result = new FitnessResult(false);

		result.tests = tests;
		result.actualOutputs = actualOutputs;

		double[] targetOutputs = new double[tests.size()];
		for (int i = 0; i < tests.size(); i++) {
			targetOutputs[i] = coeff[0] * Math.exp((-Math.pow(tests.get(i)[0] - coeff[1], 2)) / (2 * Math.pow(coeff[2], 2)));
		}
		result.targetOutputs = targetOutputs;

		result.targetFittingParams = targetCoeff;
		result.actualFittingParams = new double[] { coeff[0], coeff[1], coeff[2] };
		return result;

	}

	public static void main(String[] args) {
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		obs.add(1.0, 0.041084924203786885);
		obs.add(6.444444444444445, 0.00822427625858405);
		obs.add(11.88888888888889, 0.004128547441396287);
		obs.add(17.333333333333336, 0.0026128182678188086);
		obs.add(22.77777777777778, 0.001850039379161948);
		obs.add(28.22222222222222, 0.0014016113563683381);
		obs.add(33.66666666666667, 0.0011114454869482894);
		obs.add(39.111111111111114, 9.109175255016486E-4);
		obs.add(44.55555555555556, 7.654880728002641E-4);
		obs.add(50.0, 6.560391667093474E-4);

		final GaussianCurveFitter fitter = GaussianCurveFitter.create();

		final double[] coeff = fitter.fit(obs.toList());
		System.out.println(Arrays.toString(coeff));
	}
}
