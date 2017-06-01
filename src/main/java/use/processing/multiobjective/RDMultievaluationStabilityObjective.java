package use.processing.multiobjective;

import java.util.ArrayList;

import use.processing.rd.PatternEvaluator;

public class RDMultievaluationStabilityObjective extends RDMultievaluationObjective{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public double evaluateScore(RDMultiobjectivePatternFitnessResult[] results){
		
		//We use the distance function to determine the distance between reevaluations
		double matches = 0.0;
		
		for(int i = 0; i<results.length-1; i++){
			boolean[][] positionsI = results[i].positions;
			for(int j = i+1; j<results.length; j++){
				boolean[][] positionsJ = results[j].positions;
				matches += 1.0 - PatternEvaluator.distance(positionsI, positionsJ)/((double)positionsI.length*positionsI[0].length);
			}
		}
		double size = results.length*(results.length-1)/2.0;
		return matches/size;
	}

}
