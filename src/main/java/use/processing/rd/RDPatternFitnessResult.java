package use.processing.rd;

import java.util.ArrayList;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import erne.AbstractFitnessResult;
import use.processing.bead.Bead;

public class RDPatternFitnessResult extends AbstractFitnessResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected float[][][] conc;
	protected boolean[][] pattern;
	protected Table<Integer,Integer,ArrayList<Bead>> beads;//index of the first template species, indicating bead positions;
	protected double fitness;
	
	public RDPatternFitnessResult(float[][][] conc, boolean[][] pattern, HashBasedTable<Integer,Integer,ArrayList<Bead>> beads, double randomFit){
		this.conc = conc;
		this.pattern = pattern;
		this.beads = beads;
		if(RDConstants.useMatchFitness){
			fitness = ((PatternEvaluator.matchOnPattern(pattern, PatternEvaluator.detectBeads(pattern.length, pattern[0].length,beads)))
					*RDConstants.spaceStep*RDConstants.spaceStep)/(RDConstants.hsize*RDConstants.wsize);
		} else {
		fitness = RDConstants.hsize*RDConstants.wsize/((PatternEvaluator.distance(pattern, 
				PatternEvaluator.detectBeads(pattern.length, pattern[0].length,beads)))*RDConstants.spaceStep*RDConstants.spaceStep);
		}
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
	
	public Table<Integer,Integer,ArrayList<Bead>> getBeads(){
		return beads;
	}

}
