package use.processing.rd;

import java.util.Arrays;
import java.util.List;


import erne.MultiobjectiveAbstractFitnessResult;

public class RDMultiobjectivePatternFitnessResult extends MultiobjectiveAbstractFitnessResult implements RDFitnessResult {

	protected Double[] fits = new Double[2]; //how many in, how many out
	protected boolean[][] pattern;
	protected float[][][] conc;
	protected boolean[][] positions;
	protected int patternSize = 0;
	
	public RDMultiobjectivePatternFitnessResult(float[][][] conc, boolean[][] pattern) {
		this.pattern = pattern;
		this.conc = conc;
		computePatternSize();
		computePositions();
		computeFits();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2113460513597336190L;

	
	protected void computePatternSize(){
		
		for(int i = 0; i<pattern.length; i++){
			for(int j = 0; j<pattern[i].length; j++){
				if(pattern[i][j]) patternSize++;
			}
		}
	}
	
	protected void computeFits(){
		double in = 0.0;
		
		double out = 0.0;
		double dummy;
		for(int i = 0; i<pattern.length; i++){
			for(int j = 0; j<pattern[i].length; j++){
				if(positions[i][j]) dummy = (pattern[i][j]?in++:out++); //hack to use the trinary operator
			}
		}
		fits[0] = in/((double) patternSize);
		fits[1] = 1.0 - out/((double) pattern.length*pattern[0].length - patternSize);
	}
	
	protected void computePositions(){
		if(!RDConstants.useGlueAsTarget) System.err.println("WARNING: glue concentration required as target, ignoring settings");
		positions = PatternEvaluator.detectGlue(conc[RDConstants.glueIndex]);
	}
	
	@Override
	public List<Double> getFullFitness() {
		
		return Arrays.asList(fits);
	}

	@Override
	public float[][][] getConc() {
		
		return conc;
	}

	@Override
	public boolean[][] getPattern() {
		
		return pattern;
	}

	@Override
	public boolean[][] getPositions() {
		
		return positions;
	}
	
	@Override
	public double getFitness(){
		return fits[0]*fits[1];
	}

}
