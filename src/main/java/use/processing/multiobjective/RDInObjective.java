package use.processing.multiobjective;

import reactionnetwork.ReactionNetwork;

public class RDInObjective extends RDObjective{

	

	/**
	 * 
	 */
	private static final long serialVersionUID = -1276715684260855245L;

	@Override
	public double evaluateScore(ReactionNetwork network, boolean[][] pattern, boolean[][] positions) {
		double in = 0.0;
		double patternSize = 0.0;
		
		for(int i = 0; i<pattern.length; i++){
			for(int j = 0; j<pattern[i].length; j++){
				if(pattern[i][j]){
					patternSize++;
					if(positions[i][j]) in++;
				}
				
			}
		}
		return in/patternSize;
	}

	public static void main(String[] args){
		System.out.println((new RDInObjective()).name);
	}
	
}
