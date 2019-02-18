package use.processing.features;

import use.processing.rd.RDSystem;

public interface RDFeature {
	
	/**
	 * Very similar to use.processing.multiobjective.RDObjective
	 * May need to be unified
	 */

	public double evaluate(RDSystem system);
	
}
