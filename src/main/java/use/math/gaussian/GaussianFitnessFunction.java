package use.math.gaussian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem.Evaluation;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Incrementor;
import org.apache.commons.math3.util.Precision;

import use.math.AbstractMathFitnessFunction;
import use.math.FitnessResult;

public class GaussianFitnessFunction extends AbstractMathFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// y= 50 * Math.exp((-Math.pow(i - 25, 2)) / (2 * Math.pow(3.5, 2)))

	// logscale Gaussian
	public static final boolean logScale = true;
	public static final double[] targetCoeff = new double[] { 100, (Math.log(100) + Math.log(0.1)) / 2, 0.7 };

	// linear Gaussian
	// public static final boolean logScale = false;
	// public static final double[] targetCoeff = new double[] { 50, (100 - 0.1)
	// / 2, 3.5 };

	@Override
	protected ArrayList<double[]> getInputs(double minInputValue, double maxInputValue, int nTests, int nInputs) {
		if (logScale) {
			return getLogScaleInputValues(minInputValue, maxInputValue, nTests, nInputs);
		} else {
			return getInputValues(minInputValue, maxInputValue, nTests, nInputs);
		}
	}

	@Override
	protected FitnessResult calculateFitnessResult(ArrayList<double[]> tests, double[] actualOutputs) {
		FitnessResult result = new FitnessResult(false);

		final WeightedObservedPoints obs = new WeightedObservedPoints();
		for (int i = 0; i < tests.size(); i++) {
			obs.add(Math.log(tests.get(i)[0]), actualOutputs[i]);
		}

		GaussianCurveFitter fitter = GaussianCurveFitter.create().withStartPoint(targetCoeff).withMaxIterations(10000);
		try {
			final double[] fittingParams = fitter.fit(obs.toList());
			if (fittingParams[0] < 0.01) {
				fittingParams[2] = 50;
			}
			result.tests = tests;
			result.actualOutputs = actualOutputs;

			double[] targetOutputs = new double[tests.size()];
			for (int i = 0; i < tests.size(); i++) {
				if (logScale) {
					targetOutputs[i] = fittingParams[0]
							* Math.exp((-Math.pow(Math.log(tests.get(i)[0]) - fittingParams[1], 2)) / (2 * Math.pow(fittingParams[2], 2)));
				} else {
					targetOutputs[i] = fittingParams[0]
							* Math.exp((-Math.pow(tests.get(i)[0] - fittingParams[1], 2)) / (2 * Math.pow(fittingParams[2], 2)));
				}
			}
			result.targetOutputs = targetOutputs;

			result.targetFittingParams = targetCoeff;
			result.actualFittingParams = fittingParams;
			return result;
		} catch (Exception ex) {
			return new FitnessResult(true);
		}

	}

	public static void main(String[] args) {
		System.out.println(Math.log(0.1));
		System.out.println(Math.log(100));
		System.out.println((Math.log(100) + Math.log(0.1)) / 2);
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		// for (int i = 0; i < 10; i++) {
		// obs.add(i, 50 * Math.exp((-Math.pow(i - 25, 2)) / (2 * Math.pow(3.5,
		// 2))));
		// }

		obs.add(1.0, 0.000000000000001);
		obs.add(6.444444444444445, 0.000000000000001);
		obs.add(11.88888888888889, 0.000000000000001);
		obs.add(17.333333333333336, 0.000000000000001);
		obs.add(22.77777777777778, 0.000000000000001);
		obs.add(28.22222222222222, 0.000000000000001);
		obs.add(33.66666666666667, 0.000000000000001);
		obs.add(39.111111111111114, 0.000000000000001);
		obs.add(44.55555555555556, 0.000000000000001);
		obs.add(50.0, 0.000000000000001);

		final GaussianCurveFitter fitter = GaussianCurveFitter.create().withMaxIterations(1000);

		final double[] coeff = fitter.fit(obs.toList());
		System.out.println(Arrays.toString(coeff));
	}
}

class ConstrainedLevenbergMarquardtOptimizer implements LeastSquaresOptimizer {

	private static double[][] constrains = new double[][] { { 0, Double.MAX_VALUE }, { Math.log(0.1), Math.log(100) },
			{ 0, Double.MAX_VALUE } };

	/** Twice the "epsilon machine". */
	private static final double TWO_EPS = 2 * Precision.EPSILON;

	/* configuration parameters */
	/** Positive input variable used in determining the initial step bound. */
	private double initialStepBoundFactor;
	/** Desired relative error in the sum of squares. */
	private double costRelativeTolerance;
	/** Desired relative error in the approximate solution parameters. */
	private double parRelativeTolerance;
	/**
	 * Desired max cosine on the orthogonality between the function vector and
	 * the columns of the jacobian.
	 */
	private double orthoTolerance;
	/** Threshold for QR ranking. */
	private double qrRankingThreshold;

	/**
	 * Default constructor.
	 * <p>
	 * The default values for the algorithm settings are:
	 * <ul>
	 * <li>Initial step bound factor: 100</li>
	 * <li>Cost relative tolerance: 1e-10</li>
	 * <li>Parameters relative tolerance: 1e-10</li>
	 * <li>Orthogonality tolerance: 1e-10</li>
	 * <li>QR ranking threshold: {@link Precision#SAFE_MIN}</li>
	 * </ul>
	 **/
	public ConstrainedLevenbergMarquardtOptimizer() {
		this(100, 1e-10, 1e-10, 1e-10, Precision.SAFE_MIN);
	}

	/**
	 * Construct an instance with all parameters specified.
	 *
	 * @param initialStepBoundFactor
	 *            initial step bound factor
	 * @param costRelativeTolerance
	 *            cost relative tolerance
	 * @param parRelativeTolerance
	 *            parameters relative tolerance
	 * @param orthoTolerance
	 *            orthogonality tolerance
	 * @param qrRankingThreshold
	 *            threshold in the QR decomposition. Columns with a 2 norm less
	 *            than this threshold are considered to be all 0s.
	 */
	public ConstrainedLevenbergMarquardtOptimizer(final double initialStepBoundFactor, final double costRelativeTolerance,
			final double parRelativeTolerance, final double orthoTolerance, final double qrRankingThreshold) {
		this.initialStepBoundFactor = initialStepBoundFactor;
		this.costRelativeTolerance = costRelativeTolerance;
		this.parRelativeTolerance = parRelativeTolerance;
		this.orthoTolerance = orthoTolerance;
		this.qrRankingThreshold = qrRankingThreshold;
	}

	/**
	 * @param newInitialStepBoundFactor
	 *            Positive input variable used in determining the initial step
	 *            bound. This bound is set to the product of
	 *            initialStepBoundFactor and the euclidean norm of
	 *            {@code diag * x} if non-zero, or else to
	 *            {@code newInitialStepBoundFactor} itself. In most cases factor
	 *            should lie in the interval {@code (0.1, 100.0)}. {@code 100}
	 *            is a generally recommended value. of the matrix is reduced.
	 * @return a new instance.
	 */
	public ConstrainedLevenbergMarquardtOptimizer withInitialStepBoundFactor(double newInitialStepBoundFactor) {
		return new ConstrainedLevenbergMarquardtOptimizer(newInitialStepBoundFactor, costRelativeTolerance, parRelativeTolerance,
				orthoTolerance, qrRankingThreshold);
	}

	/**
	 * @param newCostRelativeTolerance
	 *            Desired relative error in the sum of squares.
	 * @return a new instance.
	 */
	public ConstrainedLevenbergMarquardtOptimizer withCostRelativeTolerance(double newCostRelativeTolerance) {
		return new ConstrainedLevenbergMarquardtOptimizer(initialStepBoundFactor, newCostRelativeTolerance, parRelativeTolerance,
				orthoTolerance, qrRankingThreshold);
	}

	/**
	 * @param newParRelativeTolerance
	 *            Desired relative error in the approximate solution parameters.
	 * @return a new instance.
	 */
	public ConstrainedLevenbergMarquardtOptimizer withParameterRelativeTolerance(double newParRelativeTolerance) {
		return new ConstrainedLevenbergMarquardtOptimizer(initialStepBoundFactor, costRelativeTolerance, newParRelativeTolerance,
				orthoTolerance, qrRankingThreshold);
	}

	/**
	 * Modifies the given parameter.
	 *
	 * @param newOrthoTolerance
	 *            Desired max cosine on the orthogonality between the function
	 *            vector and the columns of the Jacobian.
	 * @return a new instance.
	 */
	public ConstrainedLevenbergMarquardtOptimizer withOrthoTolerance(double newOrthoTolerance) {
		return new ConstrainedLevenbergMarquardtOptimizer(initialStepBoundFactor, costRelativeTolerance, parRelativeTolerance,
				newOrthoTolerance, qrRankingThreshold);
	}

	/**
	 * @param newQRRankingThreshold
	 *            Desired threshold for QR ranking. If the squared norm of a
	 *            column vector is smaller or equal to this threshold during QR
	 *            decomposition, it is considered to be a zero vector and hence
	 *            the rank of the matrix is reduced.
	 * @return a new instance.
	 */
	public ConstrainedLevenbergMarquardtOptimizer withRankingThreshold(double newQRRankingThreshold) {
		return new ConstrainedLevenbergMarquardtOptimizer(initialStepBoundFactor, costRelativeTolerance, parRelativeTolerance,
				orthoTolerance, newQRRankingThreshold);
	}

	/**
	 * Gets the value of a tuning parameter.
	 * 
	 * @see #withInitialStepBoundFactor(double)
	 *
	 * @return the parameter's value.
	 */
	public double getInitialStepBoundFactor() {
		return initialStepBoundFactor;
	}

	/**
	 * Gets the value of a tuning parameter.
	 * 
	 * @see #withCostRelativeTolerance(double)
	 *
	 * @return the parameter's value.
	 */
	public double getCostRelativeTolerance() {
		return costRelativeTolerance;
	}

	/**
	 * Gets the value of a tuning parameter.
	 * 
	 * @see #withParameterRelativeTolerance(double)
	 *
	 * @return the parameter's value.
	 */
	public double getParameterRelativeTolerance() {
		return parRelativeTolerance;
	}

	/**
	 * Gets the value of a tuning parameter.
	 * 
	 * @see #withOrthoTolerance(double)
	 *
	 * @return the parameter's value.
	 */
	public double getOrthoTolerance() {
		return orthoTolerance;
	}

	/**
	 * Gets the value of a tuning parameter.
	 * 
	 * @see #withRankingThreshold(double)
	 *
	 * @return the parameter's value.
	 */
	public double getRankingThreshold() {
		return qrRankingThreshold;
	}

	/** {@inheritDoc} */
	public Optimum optimize(final LeastSquaresProblem problem) {
		// Pull in relevant data from the problem as locals.
		final int nR = problem.getObservationSize(); // Number of observed data.
		final int nC = problem.getParameterSize(); // Number of parameters.
		// Counters.
		final Incrementor iterationCounter = problem.getIterationCounter();
		final Incrementor evaluationCounter = problem.getEvaluationCounter();
		// Convergence criterion.
		final ConvergenceChecker<Evaluation> checker = problem.getConvergenceChecker();

		// arrays shared with the other private methods
		final int solvedCols = FastMath.min(nR, nC);
		/* Parameters evolution direction associated with lmPar. */
		double[] lmDir = new double[nC];
		/* Levenberg-Marquardt parameter. */
		double lmPar = 0;

		// local point
		double delta = 0;
		double xNorm = 0;
		double[] diag = new double[nC];
		double[] oldX = new double[nC];
		double[] oldRes = new double[nR];
		double[] qtf = new double[nR];
		double[] work1 = new double[nC];
		double[] work2 = new double[nC];
		double[] work3 = new double[nC];

		// Evaluate the function at the starting point and calculate its norm.
		evaluationCounter.incrementCount();
		// value will be reassigned in the loop
		Evaluation current = problem.evaluate(problem.getStart());
		double[] currentResiduals = current.getResiduals().toArray();
		double currentCost = current.getCost();
		double[] currentPoint = current.getPoint().toArray();
		for (int i = 0; i < currentPoint.length; i++) {
			if (currentPoint[i] < constrains[i][0]) {
				currentPoint[i] = constrains[i][0];
			}
			if (currentPoint[i] > constrains[i][1]) {
				currentPoint[i] = constrains[i][1];
			}
		}
		// Outer loop.
		boolean firstIteration = true;
		while (true) {
			iterationCounter.incrementCount();

			final Evaluation previous = current;

			// QR decomposition of the jacobian matrix
			final InternalData internalData = qrDecomposition(current.getJacobian(), solvedCols);
			final double[][] weightedJacobian = internalData.weightedJacobian;
			final int[] permutation = internalData.permutation;
			final double[] diagR = internalData.diagR;
			final double[] jacNorm = internalData.jacNorm;

			// residuals already have weights applied
			double[] weightedResidual = currentResiduals;
			for (int i = 0; i < nR; i++) {
				qtf[i] = weightedResidual[i];
			}

			// compute Qt.res
			qTy(qtf, internalData);

			// now we don't need Q anymore,
			// so let jacobian contain the R matrix with its diagonal elements
			for (int k = 0; k < solvedCols; ++k) {
				int pk = permutation[k];
				weightedJacobian[k][pk] = diagR[pk];
			}

			if (firstIteration) {
				// scale the point according to the norms of the columns
				// of the initial jacobian
				xNorm = 0;
				for (int k = 0; k < nC; ++k) {
					double dk = jacNorm[k];
					if (dk == 0) {
						dk = 1.0;
					}
					double xk = dk * currentPoint[k];
					xNorm += xk * xk;
					diag[k] = dk;
				}
				xNorm = FastMath.sqrt(xNorm);

				// initialize the step bound delta
				delta = (xNorm == 0) ? initialStepBoundFactor : (initialStepBoundFactor * xNorm);
			}

			// check orthogonality between function vector and jacobian columns
			double maxCosine = 0;
			if (currentCost != 0) {
				for (int j = 0; j < solvedCols; ++j) {
					int pj = permutation[j];
					double s = jacNorm[pj];
					if (s != 0) {
						double sum = 0;
						for (int i = 0; i <= j; ++i) {
							sum += weightedJacobian[i][pj] * qtf[i];
						}
						maxCosine = FastMath.max(maxCosine, FastMath.abs(sum) / (s * currentCost));
					}
				}
			}
			if (maxCosine <= orthoTolerance) {
				// Convergence has been reached.
				return new OptimumImpl(current, evaluationCounter.getCount(), iterationCounter.getCount());
			}

			// rescale if necessary
			for (int j = 0; j < nC; ++j) {
				diag[j] = FastMath.max(diag[j], jacNorm[j]);
			}

			// Inner loop.
			for (double ratio = 0; ratio < 1.0e-4;) {

				// save the state
				for (int j = 0; j < solvedCols; ++j) {
					int pj = permutation[j];
					oldX[pj] = currentPoint[pj];
				}
				final double previousCost = currentCost;
				double[] tmpVec = weightedResidual;
				weightedResidual = oldRes;
				oldRes = tmpVec;

				// determine the Levenberg-Marquardt parameter
				lmPar = determineLMParameter(qtf, delta, diag, internalData, solvedCols, work1, work2, work3, lmDir, lmPar);

				// compute the new point and the norm of the evolution direction
				double lmNorm = 0;
				for (int j = 0; j < solvedCols; ++j) {
					int pj = permutation[j];
					lmDir[pj] = -lmDir[pj];
					currentPoint[pj] = oldX[pj] + lmDir[pj];
					double s = diag[pj] * lmDir[pj];
					lmNorm += s * s;
				}
				lmNorm = FastMath.sqrt(lmNorm);
				// on the first iteration, adjust the initial step bound.
				if (firstIteration) {
					delta = FastMath.min(delta, lmNorm);
				}

				// Evaluate the function at x + p and calculate its norm.
				evaluationCounter.incrementCount();
				current = problem.evaluate(new ArrayRealVector(currentPoint));
				currentResiduals = current.getResiduals().toArray();
				currentCost = current.getCost();
				currentPoint = current.getPoint().toArray();
				for (int i = 0; i < currentPoint.length; i++) {
					if (currentPoint[i] < constrains[i][0]) {
						currentPoint[i] = constrains[i][0];
					}
					if (currentPoint[i] > constrains[i][1]) {
						currentPoint[i] = constrains[i][1];
					}
				}

				// compute the scaled actual reduction
				double actRed = -1.0;
				if (0.1 * currentCost < previousCost) {
					double r = currentCost / previousCost;
					actRed = 1.0 - r * r;
				}

				// compute the scaled predicted reduction
				// and the scaled directional derivative
				for (int j = 0; j < solvedCols; ++j) {
					int pj = permutation[j];
					double dirJ = lmDir[pj];
					work1[j] = 0;
					for (int i = 0; i <= j; ++i) {
						work1[i] += weightedJacobian[i][pj] * dirJ;
					}
				}
				double coeff1 = 0;
				for (int j = 0; j < solvedCols; ++j) {
					coeff1 += work1[j] * work1[j];
				}
				double pc2 = previousCost * previousCost;
				coeff1 /= pc2;
				double coeff2 = lmPar * lmNorm * lmNorm / pc2;
				double preRed = coeff1 + 2 * coeff2;
				double dirDer = -(coeff1 + coeff2);

				// ratio of the actual to the predicted reduction
				ratio = (preRed == 0) ? 0 : (actRed / preRed);

				// update the step bound
				if (ratio <= 0.25) {
					double tmp = (actRed < 0) ? (0.5 * dirDer / (dirDer + 0.5 * actRed)) : 0.5;
					if ((0.1 * currentCost >= previousCost) || (tmp < 0.1)) {
						tmp = 0.1;
					}
					delta = tmp * FastMath.min(delta, 10.0 * lmNorm);
					lmPar /= tmp;
				} else if ((lmPar == 0) || (ratio >= 0.75)) {
					delta = 2 * lmNorm;
					lmPar *= 0.5;
				}

				// test for successful iteration.
				if (ratio >= 1.0e-4) {
					// successful iteration, update the norm
					firstIteration = false;
					xNorm = 0;
					for (int k = 0; k < nC; ++k) {
						double xK = diag[k] * currentPoint[k];
						xNorm += xK * xK;
					}
					xNorm = FastMath.sqrt(xNorm);

					// tests for convergence.
					if (checker != null && checker.converged(iterationCounter.getCount(), previous, current)) {
						return new OptimumImpl(current, evaluationCounter.getCount(), iterationCounter.getCount());
					}
				} else {
					// failed iteration, reset the previous values
					currentCost = previousCost;
					for (int j = 0; j < solvedCols; ++j) {
						int pj = permutation[j];
						currentPoint[pj] = oldX[pj];
					}
					tmpVec = weightedResidual;
					weightedResidual = oldRes;
					oldRes = tmpVec;
					// Reset "current" to previous values.
					current = previous;
				}

				// Default convergence criteria.
				if ((FastMath.abs(actRed) <= costRelativeTolerance && preRed <= costRelativeTolerance && ratio <= 2.0)
						|| delta <= parRelativeTolerance * xNorm) {
					return new OptimumImpl(current, evaluationCounter.getCount(), iterationCounter.getCount());
				}

				// tests for termination and stringent tolerances
				if (FastMath.abs(actRed) <= TWO_EPS && preRed <= TWO_EPS && ratio <= 2.0) {
					throw new ConvergenceException(LocalizedFormats.TOO_SMALL_COST_RELATIVE_TOLERANCE, costRelativeTolerance);
				} else if (delta <= TWO_EPS * xNorm) {
					throw new ConvergenceException(LocalizedFormats.TOO_SMALL_PARAMETERS_RELATIVE_TOLERANCE, parRelativeTolerance);
				} else if (maxCosine <= TWO_EPS) {
					throw new ConvergenceException(LocalizedFormats.TOO_SMALL_ORTHOGONALITY_TOLERANCE, orthoTolerance);
				}
			}
		}
	}

	/**
	 * Holds internal data. This structure was created so that all optimizer
	 * fields can be "final". Code should be further refactored in order to not
	 * pass around arguments that will modified in-place (cf. "work" arrays).
	 */
	private static class InternalData {
		/** Weighted Jacobian. */
		private final double[][] weightedJacobian;
		/** Columns permutation array. */
		private final int[] permutation;
		/** Rank of the Jacobian matrix. */
		private final int rank;
		/** Diagonal elements of the R matrix in the QR decomposition. */
		private final double[] diagR;
		/** Norms of the columns of the jacobian matrix. */
		private final double[] jacNorm;
		/** Coefficients of the Householder transforms vectors. */
		private final double[] beta;

		/**
		 * @param weightedJacobian
		 *            Weighted Jacobian.
		 * @param permutation
		 *            Columns permutation array.
		 * @param rank
		 *            Rank of the Jacobian matrix.
		 * @param diagR
		 *            Diagonal elements of the R matrix in the QR decomposition.
		 * @param jacNorm
		 *            Norms of the columns of the jacobian matrix.
		 * @param beta
		 *            Coefficients of the Householder transforms vectors.
		 */
		InternalData(double[][] weightedJacobian, int[] permutation, int rank, double[] diagR, double[] jacNorm, double[] beta) {
			this.weightedJacobian = weightedJacobian;
			this.permutation = permutation;
			this.rank = rank;
			this.diagR = diagR;
			this.jacNorm = jacNorm;
			this.beta = beta;
		}
	}

	/**
	 * Determines the Levenberg-Marquardt parameter.
	 *
	 * <p>
	 * This implementation is a translation in Java of the MINPACK <a
	 * href="http://www.netlib.org/minpack/lmpar.f">lmpar</a> routine.
	 * </p>
	 * <p>
	 * This method sets the lmPar and lmDir attributes.
	 * </p>
	 * <p>
	 * The authors of the original fortran function are:
	 * </p>
	 * <ul>
	 * <li>Argonne National Laboratory. MINPACK project. March 1980</li>
	 * <li>Burton S. Garbow</li>
	 * <li>Kenneth E. Hillstrom</li>
	 * <li>Jorge J. More</li>
	 * </ul>
	 * <p>
	 * Luc Maisonobe did the Java translation.
	 * </p>
	 *
	 * @param qy
	 *            Array containing qTy.
	 * @param delta
	 *            Upper bound on the euclidean norm of diagR * lmDir.
	 * @param diag
	 *            Diagonal matrix.
	 * @param internalData
	 *            Data (modified in-place in this method).
	 * @param solvedCols
	 *            Number of solved point.
	 * @param work1
	 *            work array
	 * @param work2
	 *            work array
	 * @param work3
	 *            work array
	 * @param lmDir
	 *            the "returned" LM direction will be stored in this array.
	 * @param lmPar
	 *            the value of the LM parameter from the previous iteration.
	 * @return the new LM parameter
	 */
	private double determineLMParameter(double[] qy, double delta, double[] diag, InternalData internalData, int solvedCols,
			double[] work1, double[] work2, double[] work3, double[] lmDir, double lmPar) {
		final double[][] weightedJacobian = internalData.weightedJacobian;
		final int[] permutation = internalData.permutation;
		final int rank = internalData.rank;
		final double[] diagR = internalData.diagR;

		final int nC = weightedJacobian[0].length;

		// compute and store in x the gauss-newton direction, if the
		// jacobian is rank-deficient, obtain a least squares solution
		for (int j = 0; j < rank; ++j) {
			lmDir[permutation[j]] = qy[j];
		}
		for (int j = rank; j < nC; ++j) {
			lmDir[permutation[j]] = 0;
		}
		for (int k = rank - 1; k >= 0; --k) {
			int pk = permutation[k];
			double ypk = lmDir[pk] / diagR[pk];
			for (int i = 0; i < k; ++i) {
				lmDir[permutation[i]] -= ypk * weightedJacobian[i][pk];
			}
			lmDir[pk] = ypk;
		}

		// evaluate the function at the origin, and test
		// for acceptance of the Gauss-Newton direction
		double dxNorm = 0;
		for (int j = 0; j < solvedCols; ++j) {
			int pj = permutation[j];
			double s = diag[pj] * lmDir[pj];
			work1[pj] = s;
			dxNorm += s * s;
		}
		dxNorm = FastMath.sqrt(dxNorm);
		double fp = dxNorm - delta;
		if (fp <= 0.1 * delta) {
			lmPar = 0;
			return lmPar;
		}

		// if the jacobian is not rank deficient, the Newton step provides
		// a lower bound, parl, for the zero of the function,
		// otherwise set this bound to zero
		double sum2;
		double parl = 0;
		if (rank == solvedCols) {
			for (int j = 0; j < solvedCols; ++j) {
				int pj = permutation[j];
				work1[pj] *= diag[pj] / dxNorm;
			}
			sum2 = 0;
			for (int j = 0; j < solvedCols; ++j) {
				int pj = permutation[j];
				double sum = 0;
				for (int i = 0; i < j; ++i) {
					sum += weightedJacobian[i][pj] * work1[permutation[i]];
				}
				double s = (work1[pj] - sum) / diagR[pj];
				work1[pj] = s;
				sum2 += s * s;
			}
			parl = fp / (delta * sum2);
		}

		// calculate an upper bound, paru, for the zero of the function
		sum2 = 0;
		for (int j = 0; j < solvedCols; ++j) {
			int pj = permutation[j];
			double sum = 0;
			for (int i = 0; i <= j; ++i) {
				sum += weightedJacobian[i][pj] * qy[i];
			}
			sum /= diag[pj];
			sum2 += sum * sum;
		}
		double gNorm = FastMath.sqrt(sum2);
		double paru = gNorm / delta;
		if (paru == 0) {
			paru = Precision.SAFE_MIN / FastMath.min(delta, 0.1);
		}

		// if the input par lies outside of the interval (parl,paru),
		// set par to the closer endpoint
		lmPar = FastMath.min(paru, FastMath.max(lmPar, parl));
		if (lmPar == 0) {
			lmPar = gNorm / dxNorm;
		}

		for (int countdown = 10; countdown >= 0; --countdown) {

			// evaluate the function at the current value of lmPar
			if (lmPar == 0) {
				lmPar = FastMath.max(Precision.SAFE_MIN, 0.001 * paru);
			}
			double sPar = FastMath.sqrt(lmPar);
			for (int j = 0; j < solvedCols; ++j) {
				int pj = permutation[j];
				work1[pj] = sPar * diag[pj];
			}
			determineLMDirection(qy, work1, work2, internalData, solvedCols, work3, lmDir);

			dxNorm = 0;
			for (int j = 0; j < solvedCols; ++j) {
				int pj = permutation[j];
				double s = diag[pj] * lmDir[pj];
				work3[pj] = s;
				dxNorm += s * s;
			}
			dxNorm = FastMath.sqrt(dxNorm);
			double previousFP = fp;
			fp = dxNorm - delta;

			// if the function is small enough, accept the current value
			// of lmPar, also test for the exceptional cases where parl is zero
			if (FastMath.abs(fp) <= 0.1 * delta || (parl == 0 && fp <= previousFP && previousFP < 0)) {
				return lmPar;
			}

			// compute the Newton correction
			for (int j = 0; j < solvedCols; ++j) {
				int pj = permutation[j];
				work1[pj] = work3[pj] * diag[pj] / dxNorm;
			}
			for (int j = 0; j < solvedCols; ++j) {
				int pj = permutation[j];
				work1[pj] /= work2[j];
				double tmp = work1[pj];
				for (int i = j + 1; i < solvedCols; ++i) {
					work1[permutation[i]] -= weightedJacobian[i][pj] * tmp;
				}
			}
			sum2 = 0;
			for (int j = 0; j < solvedCols; ++j) {
				double s = work1[permutation[j]];
				sum2 += s * s;
			}
			double correction = fp / (delta * sum2);

			// depending on the sign of the function, update parl or paru.
			if (fp > 0) {
				parl = FastMath.max(parl, lmPar);
			} else if (fp < 0) {
				paru = FastMath.min(paru, lmPar);
			}

			// compute an improved estimate for lmPar
			lmPar = FastMath.max(parl, lmPar + correction);
		}

		return lmPar;
	}

	/**
	 * Solve a*x = b and d*x = 0 in the least squares sense.
	 * <p>
	 * This implementation is a translation in Java of the MINPACK <a
	 * href="http://www.netlib.org/minpack/qrsolv.f">qrsolv</a> routine.
	 * </p>
	 * <p>
	 * This method sets the lmDir and lmDiag attributes.
	 * </p>
	 * <p>
	 * The authors of the original fortran function are:
	 * </p>
	 * <ul>
	 * <li>Argonne National Laboratory. MINPACK project. March 1980</li>
	 * <li>Burton S. Garbow</li>
	 * <li>Kenneth E. Hillstrom</li>
	 * <li>Jorge J. More</li>
	 * </ul>
	 * <p>
	 * Luc Maisonobe did the Java translation.
	 * </p>
	 *
	 * @param qy
	 *            array containing qTy
	 * @param diag
	 *            diagonal matrix
	 * @param lmDiag
	 *            diagonal elements associated with lmDir
	 * @param internalData
	 *            Data (modified in-place in this method).
	 * @param solvedCols
	 *            Number of sloved point.
	 * @param work
	 *            work array
	 * @param lmDir
	 *            the "returned" LM direction is stored in this array
	 */
	private void determineLMDirection(double[] qy, double[] diag, double[] lmDiag, InternalData internalData, int solvedCols,
			double[] work, double[] lmDir) {
		final int[] permutation = internalData.permutation;
		final double[][] weightedJacobian = internalData.weightedJacobian;
		final double[] diagR = internalData.diagR;

		// copy R and Qty to preserve input and initialize s
		// in particular, save the diagonal elements of R in lmDir
		for (int j = 0; j < solvedCols; ++j) {
			int pj = permutation[j];
			for (int i = j + 1; i < solvedCols; ++i) {
				weightedJacobian[i][pj] = weightedJacobian[j][permutation[i]];
			}
			lmDir[j] = diagR[pj];
			work[j] = qy[j];
		}

		// eliminate the diagonal matrix d using a Givens rotation
		for (int j = 0; j < solvedCols; ++j) {

			// prepare the row of d to be eliminated, locating the
			// diagonal element using p from the Q.R. factorization
			int pj = permutation[j];
			double dpj = diag[pj];
			if (dpj != 0) {
				Arrays.fill(lmDiag, j + 1, lmDiag.length, 0);
			}
			lmDiag[j] = dpj;

			// the transformations to eliminate the row of d
			// modify only a single element of Qty
			// beyond the first n, which is initially zero.
			double qtbpj = 0;
			for (int k = j; k < solvedCols; ++k) {
				int pk = permutation[k];

				// determine a Givens rotation which eliminates the
				// appropriate element in the current row of d
				if (lmDiag[k] != 0) {

					final double sin;
					final double cos;
					double rkk = weightedJacobian[k][pk];
					if (FastMath.abs(rkk) < FastMath.abs(lmDiag[k])) {
						final double cotan = rkk / lmDiag[k];
						sin = 1.0 / FastMath.sqrt(1.0 + cotan * cotan);
						cos = sin * cotan;
					} else {
						final double tan = lmDiag[k] / rkk;
						cos = 1.0 / FastMath.sqrt(1.0 + tan * tan);
						sin = cos * tan;
					}

					// compute the modified diagonal element of R and
					// the modified element of (Qty,0)
					weightedJacobian[k][pk] = cos * rkk + sin * lmDiag[k];
					final double temp = cos * work[k] + sin * qtbpj;
					qtbpj = -sin * work[k] + cos * qtbpj;
					work[k] = temp;

					// accumulate the tranformation in the row of s
					for (int i = k + 1; i < solvedCols; ++i) {
						double rik = weightedJacobian[i][pk];
						final double temp2 = cos * rik + sin * lmDiag[i];
						lmDiag[i] = -sin * rik + cos * lmDiag[i];
						weightedJacobian[i][pk] = temp2;
					}
				}
			}

			// store the diagonal element of s and restore
			// the corresponding diagonal element of R
			lmDiag[j] = weightedJacobian[j][permutation[j]];
			weightedJacobian[j][permutation[j]] = lmDir[j];
		}

		// solve the triangular system for z, if the system is
		// singular, then obtain a least squares solution
		int nSing = solvedCols;
		for (int j = 0; j < solvedCols; ++j) {
			if ((lmDiag[j] == 0) && (nSing == solvedCols)) {
				nSing = j;
			}
			if (nSing < solvedCols) {
				work[j] = 0;
			}
		}
		if (nSing > 0) {
			for (int j = nSing - 1; j >= 0; --j) {
				int pj = permutation[j];
				double sum = 0;
				for (int i = j + 1; i < nSing; ++i) {
					sum += weightedJacobian[i][pj] * work[i];
				}
				work[j] = (work[j] - sum) / lmDiag[j];
			}
		}

		// permute the components of z back to components of lmDir
		for (int j = 0; j < lmDir.length; ++j) {
			lmDir[permutation[j]] = work[j];
		}
	}

	/**
	 * Decompose a matrix A as A.P = Q.R using Householder transforms.
	 * <p>
	 * As suggested in the P. Lascaux and R. Theodor book <i>Analyse
	 * num&eacute;rique matricielle appliqu&eacute;e &agrave; l'art de
	 * l'ing&eacute;nieur</i> (Masson, 1986), instead of representing the
	 * Householder transforms with u<sub>k</sub> unit vectors such that:
	 * 
	 * <pre>
	 * H<sub>k</sub> = I - 2u<sub>k</sub>.u<sub>k</sub><sup>t</sup>
	 * </pre>
	 * 
	 * we use <sub>k</sub> non-unit vectors such that:
	 * 
	 * <pre>
	 * H<sub>k</sub> = I - beta<sub>k</sub>v<sub>k</sub>.v<sub>k</sub><sup>t</sup>
	 * </pre>
	 * 
	 * where v<sub>k</sub> = a<sub>k</sub> - alpha<sub>k</sub> e<sub>k</sub>.
	 * The beta<sub>k</sub> coefficients are provided upon exit as recomputing
	 * them from the v<sub>k</sub> vectors would be costly.
	 * </p>
	 * <p>
	 * This decomposition handles rank deficient cases since the tranformations
	 * are performed in non-increasing columns norms order thanks to columns
	 * pivoting. The diagonal elements of the R matrix are therefore also in
	 * non-increasing absolute values order.
	 * </p>
	 *
	 * @param jacobian
	 *            Weighted Jacobian matrix at the current point.
	 * @param solvedCols
	 *            Number of solved point.
	 * @return data used in other methods of this class.
	 * @throws ConvergenceException
	 *             if the decomposition cannot be performed.
	 */
	private InternalData qrDecomposition(RealMatrix jacobian, int solvedCols) throws ConvergenceException {
		// Code in this class assumes that the weighted Jacobian is -(W^(1/2)
		// J),
		// hence the multiplication by -1.
		final double[][] weightedJacobian = jacobian.scalarMultiply(-1).getData();

		final int nR = weightedJacobian.length;
		final int nC = weightedJacobian[0].length;

		final int[] permutation = new int[nC];
		final double[] diagR = new double[nC];
		final double[] jacNorm = new double[nC];
		final double[] beta = new double[nC];

		// initializations
		for (int k = 0; k < nC; ++k) {
			permutation[k] = k;
			double norm2 = 0;
			for (int i = 0; i < nR; ++i) {
				double akk = weightedJacobian[i][k];
				norm2 += akk * akk;
			}
			jacNorm[k] = FastMath.sqrt(norm2);
		}

		// transform the matrix column after column
		for (int k = 0; k < nC; ++k) {

			// select the column with the greatest norm on active components
			int nextColumn = -1;
			double ak2 = Double.NEGATIVE_INFINITY;
			for (int i = k; i < nC; ++i) {
				double norm2 = 0;
				for (int j = k; j < nR; ++j) {
					double aki = weightedJacobian[j][permutation[i]];
					norm2 += aki * aki;
				}
				if (Double.isInfinite(norm2) || Double.isNaN(norm2)) {
					throw new ConvergenceException(LocalizedFormats.UNABLE_TO_PERFORM_QR_DECOMPOSITION_ON_JACOBIAN, nR, nC);
				}
				if (norm2 > ak2) {
					nextColumn = i;
					ak2 = norm2;
				}
			}
			if (ak2 <= qrRankingThreshold) {
				return new InternalData(weightedJacobian, permutation, k, diagR, jacNorm, beta);
			}
			int pk = permutation[nextColumn];
			permutation[nextColumn] = permutation[k];
			permutation[k] = pk;

			// choose alpha such that Hk.u = alpha ek
			double akk = weightedJacobian[k][pk];
			double alpha = (akk > 0) ? -FastMath.sqrt(ak2) : FastMath.sqrt(ak2);
			double betak = 1.0 / (ak2 - akk * alpha);
			beta[pk] = betak;

			// transform the current column
			diagR[pk] = alpha;
			weightedJacobian[k][pk] -= alpha;

			// transform the remaining columns
			for (int dk = nC - 1 - k; dk > 0; --dk) {
				double gamma = 0;
				for (int j = k; j < nR; ++j) {
					gamma += weightedJacobian[j][pk] * weightedJacobian[j][permutation[k + dk]];
				}
				gamma *= betak;
				for (int j = k; j < nR; ++j) {
					weightedJacobian[j][permutation[k + dk]] -= gamma * weightedJacobian[j][pk];
				}
			}
		}

		return new InternalData(weightedJacobian, permutation, solvedCols, diagR, jacNorm, beta);
	}

	/**
	 * Compute the product Qt.y for some Q.R. decomposition.
	 *
	 * @param y
	 *            vector to multiply (will be overwritten with the result)
	 * @param internalData
	 *            Data.
	 */
	private void qTy(double[] y, InternalData internalData) {
		final double[][] weightedJacobian = internalData.weightedJacobian;
		final int[] permutation = internalData.permutation;
		final double[] beta = internalData.beta;

		final int nR = weightedJacobian.length;
		final int nC = weightedJacobian[0].length;

		for (int k = 0; k < nC; ++k) {
			int pk = permutation[k];
			double gamma = 0;
			for (int i = k; i < nR; ++i) {
				gamma += weightedJacobian[i][pk] * y[i];
			}
			gamma *= beta[pk];
			for (int i = k; i < nR; ++i) {
				y[i] -= gamma * weightedJacobian[i][pk];
			}
		}
	}
}

class GaussianCurveFitter extends AbstractCurveFitter {

	@Override
	protected LeastSquaresOptimizer getOptimizer() {
		return new ConstrainedLevenbergMarquardtOptimizer();
	}

	/** Parametric function to be fitted. */
	private static final Gaussian.Parametric FUNCTION = new Gaussian.Parametric() {
		@Override
		public double value(double x, double... p) {
			double v = Double.POSITIVE_INFINITY;
			try {
				v = super.value(x, p);
			} catch (NotStrictlyPositiveException e) { // NOPMD
				// Do nothing.
			}
			return v;
		}

		@Override
		public double[] gradient(double x, double... p) {
			double[] v = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
			try {
				v = super.gradient(x, p);
			} catch (NotStrictlyPositiveException e) { // NOPMD
				// Do nothing.
			}
			return v;
		}
	};
	/** Initial guess. */
	private final double[] initialGuess;
	/** Maximum number of iterations of the optimization algorithm. */
	private final int maxIter;

	/**
	 * Contructor used by the factory methods.
	 *
	 * @param initialGuess
	 *            Initial guess. If set to {@code null}, the initial guess will
	 *            be estimated using the {@link ParameterGuesser}.
	 * @param maxIter
	 *            Maximum number of iterations of the optimization algorithm.
	 */
	private GaussianCurveFitter(double[] initialGuess, int maxIter) {
		this.initialGuess = initialGuess;
		this.maxIter = maxIter;
	}

	/**
	 * Creates a default curve fitter. The initial guess for the parameters will
	 * be {@link ParameterGuesser} computed automatically, and the maximum
	 * number of iterations of the optimization algorithm is set to
	 * {@link Integer#MAX_VALUE}.
	 *
	 * @return a curve fitter.
	 *
	 * @see #withStartPoint(double[])
	 * @see #withMaxIterations(int)
	 */
	public static GaussianCurveFitter create() {
		return new GaussianCurveFitter(null, Integer.MAX_VALUE);
	}

	/**
	 * Configure the start point (initial guess).
	 * 
	 * @param newStart
	 *            new start point (initial guess)
	 * @return a new instance.
	 */
	public GaussianCurveFitter withStartPoint(double[] newStart) {
		return new GaussianCurveFitter(newStart.clone(), maxIter);
	}

	/**
	 * Configure the maximum number of iterations.
	 * 
	 * @param newMaxIter
	 *            maximum number of iterations
	 * @return a new instance.
	 */
	public GaussianCurveFitter withMaxIterations(int newMaxIter) {
		return new GaussianCurveFitter(initialGuess, newMaxIter);
	}

	/** {@inheritDoc} */
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

		final double[] startPoint = initialGuess != null ? initialGuess :
		// Compute estimation.
				new ParameterGuesser(observations).guess();

		// Return a new least squares problem set up to fit a Gaussian curve to
		// the
		// observed points.
		return new LeastSquaresBuilder().maxEvaluations(Integer.MAX_VALUE).maxIterations(maxIter).start(startPoint).target(target)
				.weight(new DiagonalMatrix(weights)).model(model.getModelFunction(), model.getModelFunctionJacobian()).build();

	}

	/**
	 * Guesses the parameters {@code norm}, {@code mean}, and {@code sigma} of a
	 * {@link org.apache.commons.math3.analysis.function.Gaussian.Parametric}
	 * based on the specified observed points.
	 */
	public static class ParameterGuesser {
		/** Normalization factor. */
		private final double norm;
		/** Mean. */
		private final double mean;
		/** Standard deviation. */
		private final double sigma;

		/**
		 * Constructs instance with the specified observed points.
		 *
		 * @param observations
		 *            Observed points from which to guess the parameters of the
		 *            Gaussian.
		 * @throws NullArgumentException
		 *             if {@code observations} is {@code null}.
		 * @throws NumberIsTooSmallException
		 *             if there are less than 3 observations.
		 */
		public ParameterGuesser(Collection<WeightedObservedPoint> observations) {
			if (observations == null) {
				throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
			}
			if (observations.size() < 3) {
				throw new NumberIsTooSmallException(observations.size(), 3, true);
			}

			final List<WeightedObservedPoint> sorted = sortObservations(observations);
			final double[] params = basicGuess(sorted.toArray(new WeightedObservedPoint[0]));

			norm = params[0];
			mean = params[1];
			sigma = params[2];
		}

		/**
		 * Gets an estimation of the parameters.
		 *
		 * @return the guessed parameters, in the following order:
		 *         <ul>
		 *         <li>Normalization factor</li>
		 *         <li>Mean</li>
		 *         <li>Standard deviation</li>
		 *         </ul>
		 */
		public double[] guess() {
			return new double[] { norm, mean, sigma };
		}

		/**
		 * Sort the observations.
		 *
		 * @param unsorted
		 *            Input observations.
		 * @return the input observations, sorted.
		 */
		private List<WeightedObservedPoint> sortObservations(Collection<WeightedObservedPoint> unsorted) {
			final List<WeightedObservedPoint> observations = new ArrayList<WeightedObservedPoint>(unsorted);

			final Comparator<WeightedObservedPoint> cmp = new Comparator<WeightedObservedPoint>() {
				public int compare(WeightedObservedPoint p1, WeightedObservedPoint p2) {
					if (p1 == null && p2 == null) {
						return 0;
					}
					if (p1 == null) {
						return -1;
					}
					if (p2 == null) {
						return 1;
					}
					if (p1.getX() < p2.getX()) {
						return -1;
					}
					if (p1.getX() > p2.getX()) {
						return 1;
					}
					if (p1.getY() < p2.getY()) {
						return -1;
					}
					if (p1.getY() > p2.getY()) {
						return 1;
					}
					if (p1.getWeight() < p2.getWeight()) {
						return -1;
					}
					if (p1.getWeight() > p2.getWeight()) {
						return 1;
					}
					return 0;
				}
			};

			Collections.sort(observations, cmp);
			return observations;
		}

		/**
		 * Guesses the parameters based on the specified observed points.
		 *
		 * @param points
		 *            Observed points, sorted.
		 * @return the guessed parameters (normalization factor, mean and
		 *         sigma).
		 */
		private double[] basicGuess(WeightedObservedPoint[] points) {
			final int maxYIdx = findMaxY(points);
			final double n = points[maxYIdx].getY();
			final double m = points[maxYIdx].getX();

			double fwhmApprox;
			try {
				final double halfY = n + ((m - n) / 2);
				final double fwhmX1 = interpolateXAtY(points, maxYIdx, -1, halfY);
				final double fwhmX2 = interpolateXAtY(points, maxYIdx, 1, halfY);
				fwhmApprox = fwhmX2 - fwhmX1;
			} catch (OutOfRangeException e) {
				// TODO: Exceptions should not be used for flow control.
				fwhmApprox = points[points.length - 1].getX() - points[0].getX();
			}
			final double s = fwhmApprox / (2 * FastMath.sqrt(2 * FastMath.log(2)));

			return new double[] { n, m, s };
		}

		/**
		 * Finds index of point in specified points with the largest Y.
		 *
		 * @param points
		 *            Points to search.
		 * @return the index in specified points array.
		 */
		private int findMaxY(WeightedObservedPoint[] points) {
			int maxYIdx = 0;
			for (int i = 1; i < points.length; i++) {
				if (points[i].getY() > points[maxYIdx].getY()) {
					maxYIdx = i;
				}
			}
			return maxYIdx;
		}

		/**
		 * Interpolates using the specified points to determine X at the
		 * specified Y.
		 *
		 * @param points
		 *            Points to use for interpolation.
		 * @param startIdx
		 *            Index within points from which to start the search for
		 *            interpolation bounds points.
		 * @param idxStep
		 *            Index step for searching interpolation bounds points.
		 * @param y
		 *            Y value for which X should be determined.
		 * @return the value of X for the specified Y.
		 * @throws ZeroException
		 *             if {@code idxStep} is 0.
		 * @throws OutOfRangeException
		 *             if specified {@code y} is not within the range of the
		 *             specified {@code points}.
		 */
		private double interpolateXAtY(WeightedObservedPoint[] points, int startIdx, int idxStep, double y) throws OutOfRangeException {
			if (idxStep == 0) {
				throw new ZeroException();
			}
			final WeightedObservedPoint[] twoPoints = getInterpolationPointsForY(points, startIdx, idxStep, y);
			final WeightedObservedPoint p1 = twoPoints[0];
			final WeightedObservedPoint p2 = twoPoints[1];
			if (p1.getY() == y) {
				return p1.getX();
			}
			if (p2.getY() == y) {
				return p2.getX();
			}
			return p1.getX() + (((y - p1.getY()) * (p2.getX() - p1.getX())) / (p2.getY() - p1.getY()));
		}

		/**
		 * Gets the two bounding interpolation points from the specified points
		 * suitable for determining X at the specified Y.
		 *
		 * @param points
		 *            Points to use for interpolation.
		 * @param startIdx
		 *            Index within points from which to start search for
		 *            interpolation bounds points.
		 * @param idxStep
		 *            Index step for search for interpolation bounds points.
		 * @param y
		 *            Y value for which X should be determined.
		 * @return the array containing two points suitable for determining X at
		 *         the specified Y.
		 * @throws ZeroException
		 *             if {@code idxStep} is 0.
		 * @throws OutOfRangeException
		 *             if specified {@code y} is not within the range of the
		 *             specified {@code points}.
		 */
		private WeightedObservedPoint[] getInterpolationPointsForY(WeightedObservedPoint[] points, int startIdx, int idxStep, double y)
				throws OutOfRangeException {
			if (idxStep == 0) {
				throw new ZeroException();
			}
			for (int i = startIdx; idxStep < 0 ? i + idxStep >= 0 : i + idxStep < points.length; i += idxStep) {
				final WeightedObservedPoint p1 = points[i];
				final WeightedObservedPoint p2 = points[i + idxStep];
				if (isBetween(y, p1.getY(), p2.getY())) {
					if (idxStep < 0) {
						return new WeightedObservedPoint[] { p2, p1 };
					} else {
						return new WeightedObservedPoint[] { p1, p2 };
					}
				}
			}

			// Boundaries are replaced by dummy values because the raised
			// exception is caught and the message never displayed.
			// TODO: Exceptions should not be used for flow control.
			throw new OutOfRangeException(y, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		}

		/**
		 * Determines whether a value is between two other values.
		 *
		 * @param value
		 *            Value to test whether it is between {@code boundary1} and
		 *            {@code boundary2}.
		 * @param boundary1
		 *            One end of the range.
		 * @param boundary2
		 *            Other end of the range.
		 * @return {@code true} if {@code value} is between {@code boundary1}
		 *         and {@code boundary2} (inclusive), {@code false} otherwise.
		 */
		private boolean isBetween(double value, double boundary1, double boundary2) {
			return (value >= boundary1 && value <= boundary2) || (value >= boundary2 && value <= boundary1);
		}
	}
}

class OptimumImpl implements Optimum {

	/** abscissa and ordinate */
	private final Evaluation value;
	/** number of evaluations to compute this optimum */
	private final int evaluations;
	/** number of iterations to compute this optimum */
	private final int iterations;

	/**
	 * Construct an optimum from an evaluation and the values of the counters.
	 *
	 * @param value
	 *            the function value
	 * @param evaluations
	 *            number of times the function was evaluated
	 * @param iterations
	 *            number of iterations of the algorithm
	 */
	OptimumImpl(final Evaluation value, final int evaluations, final int iterations) {
		this.value = value;
		this.evaluations = evaluations;
		this.iterations = iterations;
	}

	/* auto-generated implementations */

	/** {@inheritDoc} */
	public int getEvaluations() {
		return evaluations;
	}

	/** {@inheritDoc} */
	public int getIterations() {
		return iterations;
	}

	/** {@inheritDoc} */
	public RealMatrix getCovariances(double threshold) {
		return value.getCovariances(threshold);
	}

	/** {@inheritDoc} */
	public RealVector getSigma(double covarianceSingularityThreshold) {
		return value.getSigma(covarianceSingularityThreshold);
	}

	/** {@inheritDoc} */
	public double getRMS() {
		return value.getRMS();
	}

	/** {@inheritDoc} */
	public RealMatrix getJacobian() {
		return value.getJacobian();
	}

	/** {@inheritDoc} */
	public double getCost() {
		return value.getCost();
	}

	/** {@inheritDoc} */
	public RealVector getResiduals() {
		return value.getResiduals();
	}

	/** {@inheritDoc} */
	public RealVector getPoint() {
		return value.getPoint();
	}
}

class InternalData {
	/** Weighted Jacobian. */
	private final double[][] weightedJacobian;
	/** Columns permutation array. */
	private final int[] permutation;
	/** Rank of the Jacobian matrix. */
	private final int rank;
	/** Diagonal elements of the R matrix in the QR decomposition. */
	private final double[] diagR;
	/** Norms of the columns of the jacobian matrix. */
	private final double[] jacNorm;
	/** Coefficients of the Householder transforms vectors. */
	private final double[] beta;

	/**
	 * @param weightedJacobian
	 *            Weighted Jacobian.
	 * @param permutation
	 *            Columns permutation array.
	 * @param rank
	 *            Rank of the Jacobian matrix.
	 * @param diagR
	 *            Diagonal elements of the R matrix in the QR decomposition.
	 * @param jacNorm
	 *            Norms of the columns of the jacobian matrix.
	 * @param beta
	 *            Coefficients of the Householder transforms vectors.
	 */
	InternalData(double[][] weightedJacobian, int[] permutation, int rank, double[] diagR, double[] jacNorm, double[] beta) {
		this.weightedJacobian = weightedJacobian;
		this.permutation = permutation;
		this.rank = rank;
		this.diagR = diagR;
		this.jacNorm = jacNorm;
		this.beta = beta;
	}
}