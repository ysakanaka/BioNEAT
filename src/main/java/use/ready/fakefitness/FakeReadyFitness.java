package use.ready.fakefitness;

import use.math.FitnessResult;
import use.ready.AbstractReadyFitnessFunction;
import erne.AbstractFitnessResult;

/**
 * This class is here to test the whole process
 * @author naubertkato
 *
 */
public class FakeReadyFitness extends AbstractReadyFitnessFunction {

	@Override
	protected FitnessResult calculateFitnessResult(String path, String[] inputNames) {
		// TODO Auto-generated method stub
		return (FitnessResult) minFitness();
	}

}
