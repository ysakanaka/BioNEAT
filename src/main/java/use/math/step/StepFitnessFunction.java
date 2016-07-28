package use.math.step;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.special.Erf;

import use.math.AbstractMathFitnessFunction;
import use.math.FitnessResult;


public class StepFitnessFunction extends AbstractMathFitnessFunction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static boolean hysteresis = false;
	static boolean diff = false;
	
	static final double[] initialGuess = new double[] { 100.0, 50.0, 0.1 , 3.0}; //we need a high error factor (last param), or wrong fit
	static final double[] targetCoeff = new double[] { 100.0, 30.0, 0.1 , 0.01}; // y=100.0*HS(x-10.0) + 0.1 with slope factor 1
    static final double[] hystCoeff = new double[] { 100.0, 30.0, 0.1, 0.01, 100.0, 30.0, 0.1 , 0.01}; //going there and back
    static final double[] hystdiffCoeff = new double[] { 100.0, 60.0, 0.1 , 0.01, 100.0, 30.0, 0.1, 0.01};
	static final int maxIterations = 10000;
	@Override
	protected FitnessResult calculateFitnessResult(ArrayList<double[]> tests, double[] actualOutputs) {
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		int size = hysteresis?tests.size()/2:tests.size();
		FitnessResult result = new FitnessResult(false);
		double[] fullCoeffs;
		double[] targetOutputs = new double[tests.size()];
		for (int i = 0; i < size; i++) {
			obs.add(tests.get(i)[0], actualOutputs[i]);
		}

		final StepCurveFitter fitter = StepCurveFitter.create().withMaxIterations(maxIterations).withStartPoint(initialGuess);

		final double[] coeff = fitter.fit(obs.toList());
		
		
		for (int i = 0; i < size; i++) {
			targetOutputs[i] = coeff[0]*(0.5+0.5*Erf.erf((tests.get(i)[0]-coeff[1])/coeff[3]))+coeff[2];
		}
		
		if(hysteresis){
			//we do it once more, with feelings
			final WeightedObservedPoints obs2 = new WeightedObservedPoints();
			for (int i = size; i < tests.size(); i++) {
				obs2.add(tests.get(i)[0], actualOutputs[i]);
			}

			final StepCurveFitter fitter2 = StepCurveFitter.create().withMaxIterations(maxIterations).withStartPoint(initialGuess);

			final double[] coeff2 = fitter2.fit(obs2.toList());
			
			
			for (int i = size; i < tests.size(); i++) {
				targetOutputs[i] = coeff2[0]*(0.5+0.5*Erf.erf((tests.get(i)[0]-coeff2[1])/coeff2[3]))+coeff2[2];
			}
			
			fullCoeffs = new double[hystCoeff.length];
			for (int i=0; i<coeff.length; i++){
				fullCoeffs[i] = coeff[i];
			}
			for (int i=0; i<coeff2.length; i++){
				fullCoeffs[i+coeff.length] = coeff2[i];
			}
		} else {
			fullCoeffs = Arrays.copyOf(coeff, coeff.length);
		}
		
		
		result.targetOutputs = targetOutputs;

		result.tests = tests;
		result.actualOutputs = actualOutputs;

		
		
		result.targetFittingParams = (hysteresis?(diff?hystdiffCoeff:hystCoeff):targetCoeff);
		
		result.actualFittingParams = fullCoeffs;
		return result;
	}
	
	@Override
	protected ArrayList<double[]> getInputs(double minInputValue, double maxInputValue, int nTests, int nInputs) {
		int trueNTests = nTests;
		if(hysteresis) trueNTests /= 2;
		ArrayList<double[]> vals = super.getInputs(minInputValue, maxInputValue, trueNTests, nInputs);
		if (hysteresis){
			ArrayList<double[]> vals2 = super.getInputs(minInputValue, maxInputValue, trueNTests, nInputs);
			for(int i = vals2.size()-1;i>=0; i--){
				vals.add(vals2.get(i)); // we move back in concentrations
			}
		}
		return vals;
	}
	
	public static void main(String[] args) {
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		// for (int i = 0; i < 10; i++) {
		// obs.add(i, 50 * Math.exp((-Math.pow(i - 25, 2)) / (2 * Math.pow(3.5,
		// 2))));
		// }

		obs.add(1.0, 0.000000000000001);
		obs.add(6.444444444444445, 0.000000000000001);
		obs.add(11.88888888888889, 0.000000000000001);
		obs.add(17.333333333333336, 0.0);
		obs.add(22.77777777777778, 52.0);
		obs.add(28.22222222222222, 52.0);
		obs.add(33.66666666666667, 52.0);
		obs.add(39.111111111111114, 52.0);
		obs.add(44.55555555555556, 52.0);
		obs.add(50.0, 52.0);

		final StepCurveFitter fitter = StepCurveFitter.create().withMaxIterations(maxIterations).withStartPoint(initialGuess);

		final double[] coeff = fitter.fit(obs.toList());
		System.out.println(Arrays.toString(coeff));
		System.out.println();
		
		//StepFitnessFunction fitness = new StepFitnessFunction();
		
		//fitness.calculateFitnessResult(tests, actualOutputs);
	}

}

class StepFunction implements ParametricUnivariateFunction{
	//We use a continuous linear approximation, since concentrations are continuous by nature

	@Override
	public double value(double x, double... parameters) {
		if(parameters.length >=4){
			return parameters[0]*(0.5+0.5*Erf.erf((x-parameters[1])/parameters[3]))+parameters[2];
		}
		return 0;
	}

	@Override
	public double[] gradient(double x, double... parameters) {
		if(parameters.length >=4){
			double[] grad = new double[parameters.length];
			Arrays.fill(grad, 0.0);
			grad[0] = (0.5+0.5*Erf.erf((x-parameters[1])/parameters[3]));
			grad[1] = -parameters[0]/(Math.sqrt(Math.PI)*parameters[3])*Math.exp(-((x-parameters[1])/parameters[3])*((x-parameters[1])/parameters[3]));
			grad[2] = 1.0;
			grad[3] = -parameters[0]*(x-parameters[1])/(Math.sqrt(Math.PI)*parameters[3]*parameters[3])*Math.exp(-((x-parameters[1])/parameters[3])*((x-parameters[1])/parameters[3]));;
			return grad;
		}
		return null;
	}
	
}

class StepCurveFitter extends AbstractCurveFitter {
	
	private final double[] initialGuess;
	private final int maxIter;

	private StepCurveFitter(double[] initialGuess, int maxIter) {
		this.initialGuess = initialGuess;
		this.maxIter = maxIter;
	}
	
	public static StepCurveFitter create(){
		return new StepCurveFitter(null,Integer.MAX_VALUE);
	}

	public StepCurveFitter withStartPoint(double[] newStart) {
		return new StepCurveFitter(newStart.clone(), maxIter);
	}

	public StepCurveFitter withMaxIterations(int newMaxIter) {
		return new StepCurveFitter(initialGuess, newMaxIter);
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

		final AbstractCurveFitter.TheoreticalValuesFunction model = new AbstractCurveFitter.TheoreticalValuesFunction(new StepFunction(),
				observations);

		if (initialGuess == null) {
			throw new MathInternalError();
		}

		return new LeastSquaresBuilder().maxEvaluations(Integer.MAX_VALUE).maxIterations(maxIter).start(initialGuess).target(target)
				.weight(new DiagonalMatrix(weights)).model(model.getModelFunction(), model.getModelFunctionJacobian()).build();

	}

}