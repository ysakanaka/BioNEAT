package use.processing.multiobjective;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import erne.MultiobjectiveAbstractFitnessResult;
import reactionnetwork.ReactionNetwork;
import use.processing.rd.PatternEvaluator;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessResult;
import use.processing.rd.RDSystem;

public class RDMultiobjectivePatternFitnessResult extends MultiobjectiveAbstractFitnessResult implements RDFitnessResult {

	protected transient ArrayList<RDObjective> objectives;
	protected Double[] fits;
	protected boolean[][] pattern;
	protected float[][][] conc;
	protected boolean[][] positions;
	protected transient ReactionNetwork network;
	protected int patternSize = 0;
	
	public RDMultiobjectivePatternFitnessResult(RDSystem system, boolean[][] pattern, ArrayList<RDObjective> objectives) {
		this.pattern = pattern;
		this.conc = new float[Math.min(system.conc.length, RDConstants.glueIndex+3)][][];
	    for(int i = 0; i<this.conc.length; i++) this.conc[i] = system.conc[i];
		this.objectives = objectives;
		this.network = system.getReactionNetwork();
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
		fits = new Double[objectives.size()];
		for(int i = 0; i<objectives.size(); i++){
			fits[i] = objectives.get(i).evaluateScore(network, pattern, positions);
		}
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
		double val = fits[0];
		for(int i = 1; i<fits.length; i++) val *=fits[i];
		return val;
	}
    
	//WARNING: only use when no other way around.
	public void addFit(double val){
		fits = Arrays.copyOf(fits, fits.length+1);
		fits[fits.length-1] = val;
	}
}
