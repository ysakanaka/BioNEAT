package cluster;

import java.io.Serializable;

import reactionnetwork.ReactionNetwork;
import erne.AbstractFitnessFunction;

public class FitnessEvaluationData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public AbstractFitnessFunction fitnessFunction;
	public ReactionNetwork network;

	public FitnessEvaluationData(AbstractFitnessFunction fitnessFunction, ReactionNetwork network) {
		this.fitnessFunction = fitnessFunction;
		this.network = network;
	}
}
