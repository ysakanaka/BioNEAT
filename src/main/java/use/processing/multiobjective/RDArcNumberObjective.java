package use.processing.multiobjective;

import use.processing.rd.RDSystem;

public class RDArcNumberObjective extends RDObjective {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7056462727344308377L;

	@Override
	public double evaluateScore(RDSystem system, boolean[][] pattern, boolean[][] position) {
		
		return 1.0/Math.max(1.0, system.tempAddress.keySet().size());
	}

}
