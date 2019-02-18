package use.processing.multiobjective;

import java.io.Serializable;

public class RDMultievaluationObjective implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public double evaluateScore(RDMultiobjectivePatternFitnessResult[] results){
		System.out.println("WARNING: dummy class "+this.getClass());
		return 0.0;
	}

}
