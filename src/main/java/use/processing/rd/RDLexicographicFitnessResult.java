package use.processing.rd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import com.google.common.collect.HashBasedTable;

import erne.AbstractLexicographicFitnessResult;
import use.processing.bead.Bead;

public class RDLexicographicFitnessResult extends AbstractLexicographicFitnessResult implements RDFitnessResult{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 704570797411589929L;
	protected ArrayList<Double> fullFitness = new ArrayList<Double>();
	public static final AbstractLexicographicFitnessResult minFitness = new RDLexicographicFitnessResult(); //since length 0, always worse than the rest
	
	protected float[][][] conc;
	protected boolean[][] pattern;
	protected boolean[][] positions;
	transient protected double randomFit;
	transient protected boolean fitnessComputed = false;
	
	protected RDLexicographicFitnessResult(){
		
	}
	
	public RDLexicographicFitnessResult(float[][][] conc, boolean[][] pattern, HashBasedTable<Integer,Integer,ArrayList<Bead>> beads, double randomFit){
		//this.concGlue = conc[RDConstants.glueIndex];
		this.conc = new float[Math.min(conc.length, RDConstants.glueIndex+3)][][];
		for(int i = 0; i<this.conc.length; i++) this.conc[i] = conc[i];
		this.pattern = pattern;
		this.randomFit = randomFit;
		//this.beads = beads;
		positions = (RDConstants.useGlueAsTarget?PatternEvaluator.detectGlue(conc[RDConstants.glueIndex])
				:PatternEvaluator.detectBeads(pattern.length, pattern[0].length,beads));
		
		computeFitness();
		
	}
	
	/**
	 * Function doing the actual fitness computation, for extending classes to override
	 */
	protected void computeFitness(){
		if(!fitnessComputed){
		  fullFitness.add(getCoverageFitness());
		  fullFitness.add(getMatchingFitness());
		  fitnessComputed = true;
		}
	}

	@Override
	public List<Double> getFullFitness() {
		
		return fullFitness;
	}
	
	protected double getCoverageFitness(){
		int patternSize = RDConstants.horizontalBins*RDConstants.verticalBins;
		double[] goodConc = new double[patternSize];
		
		for(int i = 0; i<conc[RDConstants.glueIndex].length; i++){
			for(int j=0; j<conc[RDConstants.glueIndex][i].length;j++){
		    	if(conc[RDConstants.glueIndex][i][j]>RDConstants.cutOff){
		    		if(pattern[i][j]){
		    			int hpos = (i*RDConstants.horizontalBins)/pattern.length;
		    			int vpos = (j*RDConstants.verticalBins)/pattern.length;
		    			goodConc[hpos+vpos*RDConstants.horizontalBins] += conc[RDConstants.glueIndex][i][j];
		    			
		    		}
		    	}
		    	
			}
		}
		
		double res = 0.0;
		for(int i=0; i<goodConc.length;i++){
			if(goodConc[i]>0.0) res++;
		}
		
		return res;
	}
	
	protected double getMatchingFitness(){
		double fitness;
		if(RDConstants.useMatchFitness){
			fitness = ((PatternEvaluator.matchOnPattern(pattern, positions))
					*RDConstants.spaceStep*RDConstants.spaceStep)/(RDConstants.hsize*RDConstants.wsize);
		} else if(RDConstants.useHellingerDistance){
		    fitness = 1.0 - PatternEvaluator.hellingerDistance(conc[RDConstants.glueIndex], pattern);
		} else {
		fitness = RDConstants.hsize*RDConstants.wsize/((PatternEvaluator.distance(pattern, 
				positions))*RDConstants.spaceStep*RDConstants.spaceStep);
		}
		return Math.max(0.0, fitness - randomFit);
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
	
	public static void main(String[] args){
		 boolean[][] pattern=RDPatternFitnessResultIbuki.getCenterLine();
		  
		  List<boolean[][]> blurredPatterns = RDPatternFitnessResultIbuki.getBlurredPatterns(pattern);
		  boolean[][] myBlurredPattern = RDPatternFitnessResultIbuki.getCenterLine();
		  RDConstants.useMatchFitness = false;
		  RDConstants.useHellingerDistance = true;
		  RDConstants.glueIndex = 0;
		  RDConstants.speciesOffset = 0;
		  
		  AbstractLexicographicFitnessResult[] stuffs = new AbstractLexicographicFitnessResult[blurredPatterns.size()];
		  
		  for(int i = 0; i<blurredPatterns.size();i++){
			  float[][][] concs = new float[1][pattern.length][pattern[0].length];
			  
			  for(int j=0;j<pattern.length;j++){
				  for(int k=0;k<pattern[j].length;k++){
					  concs[0][j][k] = myBlurredPattern[j][k]?10.0f*RDConstants.cutOff:0.0f;
				  }
			  }
			  if(i<blurredPatterns.size()-1){
				  boolean[][] thisConfig = blurredPatterns.get(i+1);
				  for(int j=0;j<pattern.length;j++){
					  for(int k=0;k<pattern[j].length;k++){
						  if(thisConfig[j][k]) myBlurredPattern[j][k] = true;
					  }
				  }
			  }
			  
			  RDLexicographicFitnessResult res = new RDLexicographicFitnessResult(concs,pattern,null,0.0);
			  stuffs[i] = res;
			  System.out.println("Blur "+i+": "+res);
		  }
		  
		  Arrays.sort(stuffs, AbstractLexicographicFitnessResult.defaultComparator);
		  System.out.println("=============");
		  RDFitnessDisplayer display = new RDFitnessDisplayer();
		  for(int i = 0; i<stuffs.length; i++) {
			  stuffs[i].setRank(stuffs.length-i);
			  System.out.println("Blur "+i+": "+stuffs[i]);
			  JFrame myFrame = new JFrame("Blur "+i);
			  myFrame.add(display.drawVisualization(stuffs[i]));
			  myFrame.pack();
			  myFrame.setVisible(true);
		  }
	}

}
