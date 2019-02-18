package use.processing.features;

import use.processing.rd.PatternEvaluator;
import use.processing.rd.RDConstants;
import use.processing.rd.RDSystem;

public class RDInFeature extends RDPatternFeature {

	public RDInFeature(boolean[][] target) {
		super(target);
	}

	@Override
	public double evaluate(RDSystem system) {
		double in = 0.0;
		double patternSize = 0.0;
		boolean[][] positions = PatternEvaluator.detectGlue(system.conc[RDConstants.glueIndex]);
		
		for(int i = 0; i<target.length; i++){
			for(int j = 0; j<target[i].length; j++){
				if(target[i][j]){
					patternSize++;
					if(positions[i][j]) in++;
				}
				
			}
		}
		return in/patternSize;
	}
}
