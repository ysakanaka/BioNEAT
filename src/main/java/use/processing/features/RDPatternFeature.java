package use.processing.features;

import use.processing.rd.RDSystem;

public abstract class RDPatternFeature implements RDFeature {

	protected boolean[][] target;
	
	public RDPatternFeature(boolean[][] target) {
		this.target = target;
	}
	
	@Override
	public double evaluate(RDSystem system) {
		System.out.println("Evaluate: not implemented");
		return 0;
	}

}
