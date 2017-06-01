package use.processing.multiobjective;

import use.processing.rd.RDSystem;

public class RDOutObjective extends RDObjective {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8489374037921650252L;

	@Override
	public double evaluateScore(RDSystem system, boolean[][] pattern, boolean[][] positions) {
		double out = 0.0;
		double patternSize = 0.0;
		
		for(int i = 0; i<pattern.length; i++){
			for(int j = 0; j<pattern[i].length; j++){
				if(pattern[i][j]){
					patternSize++;
				} else {
					if(positions[i][j]) out++;
				}
				
			}
		}
		return  1.0 - out/((double) pattern.length*pattern[0].length - patternSize);
	}

}
