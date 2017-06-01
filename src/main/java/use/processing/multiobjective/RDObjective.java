package use.processing.multiobjective;

import java.io.Serializable;

import reactionnetwork.ReactionNetwork;

public abstract class RDObjective implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5501550572733354783L;
	
	
	public final String name;

	public RDObjective(){
		this.name = this.getClass().getName();
	}
	
	
	public abstract double evaluateScore(ReactionNetwork network, boolean[][] pattern, boolean[][] position);

}
