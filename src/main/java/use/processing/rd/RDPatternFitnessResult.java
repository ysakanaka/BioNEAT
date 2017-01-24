package use.processing.rd;

import java.util.ArrayList;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import erne.AbstractFitnessResult;
import use.processing.bead.Bead;

public class RDPatternFitnessResult extends AbstractFitnessResult implements RDFitnessResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected float[][][] conc;
	protected boolean[][] pattern;
	//protected Table<Integer,Integer,ArrayList<Bead>> beads;//index of the first template species, indicating bead positions;
	protected double fitness;
	protected boolean[][] positions;
	
	protected RDPatternFitnessResult(){
		fitness = 0.0;
	}
	
	public RDPatternFitnessResult(float[][][] conc, boolean[][] pattern, HashBasedTable<Integer,Integer,ArrayList<Bead>> beads, double randomFit){
		//this.concGlue = conc[RDConstants.glueIndex];
		this.conc = new float[Math.min(conc.length, RDConstants.glueIndex+3)][][];
		for(int i = 0; i<this.conc.length; i++) this.conc[i] = conc[i];
		this.pattern = pattern;
		//this.beads = beads;
		positions = (RDConstants.useGlueAsTarget?PatternEvaluator.detectGlue(conc[RDConstants.glueIndex])
				:PatternEvaluator.detectBeads(pattern.length, pattern[0].length,beads));
		if(RDConstants.useMatchFitness){
			fitness = ((PatternEvaluator.matchOnPattern(pattern, positions))
					*RDConstants.spaceStep*RDConstants.spaceStep)/(RDConstants.hsize*RDConstants.wsize);
		} else if(RDConstants.useHellingerDistance){
		    fitness = 1.0 - PatternEvaluator.hellingerDistance(conc[RDConstants.glueIndex], pattern);
		} else {
		fitness = RDConstants.hsize*RDConstants.wsize/((PatternEvaluator.distance(pattern, 
				positions))*RDConstants.spaceStep*RDConstants.spaceStep);
		}
		fitness = Math.max(0.0, fitness - randomFit);
	}
	
	/**
	 * For child classes, skipping fitness evaluation, just setting params.
	 * @param conc
	 * @param pattern
	 * @param beads
	 */
	protected RDPatternFitnessResult(float[][][] conc, boolean[][] pattern, HashBasedTable<Integer,Integer,ArrayList<Bead>> beads){
		this.conc = new float[Math.min(conc.length, RDConstants.glueIndex+3)][][];
		for(int i = 0; i<this.conc.length; i++) this.conc[i] = conc[i];
		this.pattern = pattern;
		//this.beads = beads;
		positions = (RDConstants.useGlueAsTarget?PatternEvaluator.detectGlue(conc[RDConstants.glueIndex])
				:PatternEvaluator.detectBeads(pattern.length, pattern[0].length,beads));
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
	
	//public Table<Integer,Integer,ArrayList<Bead>> getBeads(){
	//	return beads;
	//}
	
	public boolean[][] getPositions(){
		return positions;
	}
	
	public static RDPatternFitnessResult getMinFitness(){
		return new RDPatternFitnessResult();
	}

}
