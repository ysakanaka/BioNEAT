package use.processing.multiobjective;

import reactionnetwork.ReactionNetwork;


public class RDArcNumberObjective extends RDObjective {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7056462727344308377L;

	@Override
	public double evaluateScore(ReactionNetwork network, boolean[][] pattern, boolean[][] position) {
		
		return 1.0/Math.max(1.0, network.getNEnabledConnections());
	}

}
