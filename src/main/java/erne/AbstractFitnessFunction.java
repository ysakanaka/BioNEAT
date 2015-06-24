package erne;

import java.io.Serializable;

import reactionnetwork.ReactionNetwork;

public abstract class AbstractFitnessFunction implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public abstract AbstractFitnessResult evaluate(ReactionNetwork network);

	public abstract AbstractFitnessResult minFitness();
}
