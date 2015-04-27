package erne;

import java.util.HashMap;

import reactionnetwork.ReactionNetwork;

public abstract class AbstractFitnessFunction {
	public abstract AbstractFitnessResult evaluate(ReactionNetwork network);

	public abstract HashMap<String, String> getDefaultParameters();

	public abstract void setParameters(HashMap<String, String> parameters);
	
	public abstract AbstractFitnessResult minFitness();
}
