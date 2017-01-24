package use.processing.rd;

import java.util.ArrayList;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import erne.AbstractFitnessResult;
import use.processing.bead.Bead;

public class RDAgencyFitnessResult extends RDPatternFitnessResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected float[][][] conc;
	transient protected float[][][][] concHistory;
	//protected Table<Integer,Integer,ArrayList<Bead>> beads;//index of the first template species, indicating bead positions;
	protected double fitness;
	
	protected RDAgencyFitnessResult(){
		fitness = 0.0;
	}
	
	public RDAgencyFitnessResult(float[][][] conc, float[][][][] concHistory){
		//this.concGlue = conc[RDConstants.glueIndex];
		this.conc = new float[Math.min(conc.length, RDConstants.glueIndex+3)][][];
		this.concHistory = concHistory;
		for(int i = 0; i<this.conc.length; i++) this.conc[i] = conc[i];
		
		//TODO calculate happily fitness
		//this.beads = beads;
		
		for(int i = 0; i<concHistory.length-2; i++) // timestep
			for(int j = 0; j<concHistory[0].length-1; j++) //species
				for(int x = 0; x<concHistory[0][0].length-1; x++)
					for(int y = 0; y<concHistory[0][0][0].length-1; y++) {
						fitness = Math.pow(concHistory[i+1][j][x][y] - concHistory[i][j][x][y], 2);
					}
		
		fitness = Math.max(0.0, fitness);
		//RDImagePrinter ip = new 
	}
	
	/**
	 * For child classes, skipping fitness evaluation, just setting params.
	 * @param conc
	 * @param pattern
	 * @param beads
	 */
	protected void init(float[][][] conc){
		this.conc = new float[Math.min(conc.length, RDConstants.glueIndex+3)][][];
		for(int i = 0; i<this.conc.length; i++) this.conc[i] = conc[i];
	}

	@Override
	public double getFitness() {
		return fitness;
	}
	
	public float[][][] getConc(){
		return conc;
	}
	
	public static RDAgencyFitnessResult getMinFitness(){
		return new RDAgencyFitnessResult();
	}

}
