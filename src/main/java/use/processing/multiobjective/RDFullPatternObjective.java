package use.processing.multiobjective;

import use.processing.rd.RDPatternFitnessResultIbuki;
import use.processing.rd.RDSystem;

public class RDFullPatternObjective extends RDObjective {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double evaluateScore(RDSystem system, boolean[][] pattern, boolean[][] positions) {
		double fitness= RDPatternFitnessResultIbuki.distanceNicolasExponential(pattern,positions);//distanceBlurExponential(pattern,positions);
		fitness=Math.max(0.0,(fitness)/(RDPatternFitnessResultIbuki.distanceNicolasExponential(pattern,pattern)));
		
		return fitness;
	}

}
