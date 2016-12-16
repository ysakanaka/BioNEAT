package use.processing.rd;

import java.util.ArrayList;

import com.google.common.collect.HashBasedTable;

import use.processing.bead.Bead;

public class RDPatternBlurFitnessResult extends RDPatternFitnessResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected int[][] dists;
	
	public RDPatternBlurFitnessResult(float[][][] conc, int[][] dists, HashBasedTable<Integer,Integer,ArrayList<Bead>> beads, double randomFit){
		//this.concGlue = conc[RDConstants.glueIndex];
		this.conc = new float[Math.min(conc.length, RDConstants.glueIndex+3)][][];
		for(int i = 0; i<this.conc.length; i++) this.conc[i] = conc[i];
		
		this.dists = dists;
		
		fitness = 1.0/(1.0+getTotalDist()); //TODO
	}
	
	public double getTotalDist(){
		double res = 0.0;
		double totalConc = 0.0;
		
		for(int i=0; i<dists.length; i++){
			for(int j=0; j<dists[i].length; j++){
				float localconc = conc[RDConstants.glueIndex][i][j];
				res += dists[i][j]*localconc;
				totalConc += localconc;
			}
		}
		return res/totalConc; //So that it's normalized
	}

}
