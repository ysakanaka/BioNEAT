package use.processing.rd;

import erne.AbstractFitnessResult;

public class RDPatternFitnessResult extends AbstractFitnessResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected float[][][] conc;
	protected boolean[][] pattern;
	protected int beadIndex;//index of the first template species, indicating bead positions;
	protected double fitness;
	
	public RDPatternFitnessResult(float[][][] conc, boolean[][] pattern, int beadIndex, double randomFit){
		this.conc = conc;
		this.pattern = pattern;
		this.beadIndex = beadIndex;
		fitness = RDConstants.hsize*RDConstants.wsize/((PatternEvaluator.distance(pattern, 
				PatternEvaluator.detectBeads(conc[beadIndex])))*RDConstants.spaceStep*RDConstants.spaceStep);
		fitness = Math.max(0.0, fitness - randomFit);
	}

	@Override
	public double getFitness() {
		return fitness;
	}
	
	public float[][][] getConc(){
		return conc;
	}
	
	public boolean[][] getPattern(){
		return pattern;
	}
	
	public int getBeadIndex(){
		return beadIndex;
	}

}
