package use.math.square;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

import use.math.AbstractMathFitnessFunction;
import use.math.FitnessResult;

public class SquareFitnessFunction extends AbstractMathFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static final double[] targetCoeff = new double[] { 0.01 }; // y=0.01x^2

	@Override
	protected FitnessResult calculateFitnessResult(ArrayList<double[]> tests, double[] actualOutputs) {


		final WeightedObservedPoints obs = new WeightedObservedPoints();
		for (int i = 0; i < tests.size(); i++) {
			obs.add(tests.get(i)[0], actualOutputs[i]);
		}

		final SquareCurveFitter fitter = SquareCurveFitter.create(2);

		final double[] coeff = fitter.fit(obs.toList());

		FitnessResult result = new FitnessResult(false);

		result.tests = tests;
		result.actualOutputs = actualOutputs;

		double[] targetOutputs = new double[tests.size()];
		for (int i = 0; i < tests.size(); i++) {
			obs.add(tests.get(i)[0], actualOutputs[i]);
			targetOutputs[i] = coeff[2] * tests.get(i)[0] * tests.get(i)[0];
		}
		result.targetOutputs = targetOutputs;
		
		result.targetFittingParams = targetCoeff;
		result.actualFittingParams = new double[] { coeff[2] };
		return result;

	}

	public static void main(String[] args) {
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		for (int i = 0; i < 100; i++) {
			obs.add(i, i * i / 50);
		}

		final SquareCurveFitter fitter = SquareCurveFitter.create(2);

		final double[] coeff = fitter.fit(obs.toList());
		System.out.println(Arrays.toString(coeff));
	}

}

class SquareCurveFitter extends AbstractCurveFitter {
	/** Parametric function to be fitted. */
	private static final PolynomialFunction.Parametric FUNCTION = new PolynomialFunction.Parametric() {
		@Override
		public double[] gradient(double arg0, double... arg1) {
			return new double[] { 0, 0, 1 };
		}
	};
	private final double[] initialGuess;
	private final int maxIter;

	private SquareCurveFitter(double[] initialGuess, int maxIter) {
		this.initialGuess = initialGuess;
		this.maxIter = maxIter;
	}

	public static SquareCurveFitter create(int degree) {
		return new SquareCurveFitter(new double[degree + 1], Integer.MAX_VALUE);
	}

	public SquareCurveFitter withStartPoint(double[] newStart) {
		return new SquareCurveFitter(newStart.clone(), maxIter);
	}

	public SquareCurveFitter withMaxIterations(int newMaxIter) {
		return new SquareCurveFitter(initialGuess, newMaxIter);
	}

	@Override
	protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> observations) {
		// Prepare least-squares problem.
		final int len = observations.size();
		final double[] target = new double[len];
		final double[] weights = new double[len];

		int i = 0;
		for (WeightedObservedPoint obs : observations) {
			target[i] = obs.getY();
			weights[i] = obs.getWeight();
			++i;
		}

		final AbstractCurveFitter.TheoreticalValuesFunction model = new AbstractCurveFitter.TheoreticalValuesFunction(FUNCTION,
				observations);

		if (initialGuess == null) {
			throw new MathInternalError();
		}

		return new LeastSquaresBuilder().maxEvaluations(Integer.MAX_VALUE).maxIterations(maxIter).start(initialGuess).target(target)
				.weight(new DiagonalMatrix(weights)).model(model.getModelFunction(), model.getModelFunctionJacobian()).build();

	}

}
