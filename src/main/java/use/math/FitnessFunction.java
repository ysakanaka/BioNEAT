package use.math;

import java.util.HashMap;

import reactionnetwork.ReactionNetwork;
import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;

public class FitnessFunction extends AbstractFitnessFunction {

	public static final double stoppingError = 0.05;
	public static final int maxDowntime = 400;
	public static int nInput = 1;

	@Override
	public HashMap<String, String> getDefaultParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParameters(HashMap<String, String> parameters) {
		// TODO Auto-generated method stub

	}

	@Override
	public AbstractFitnessResult minFitness() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractFitnessResult evaluate(ReactionNetwork network) {
		// TODO Auto-generated method stub
		return null;
	}

}
