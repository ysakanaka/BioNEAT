package use.processing.rd;

import java.util.Arrays;

import erne.AbstractFitnessResult;
import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import reactionnetwork.ReactionNetwork;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;

public class RDFitnessBlurFunction extends RDFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected int[][] dists;
	
	public RDFitnessBlurFunction(boolean[][] pattern) {
		this.pattern = pattern;
		this.dists = PatternEvaluator.getDistanceMatrix(pattern);
		
		if (RDConstants.evalRandomDistance){
			// we always use the blur fitness
			System.err.println("WARNING: random distance evaluation not implemented yet for this distance");
		}
		
		this.randomFitness = RDConstants.defaultRandomFitness;
		
	}

	
	@Override
	public AbstractFitnessResult evaluate(ReactionNetwork network) {
		long startTime = System.currentTimeMillis();
		AbstractFitnessResult[] results = new AbstractFitnessResult[RDConstants.reEvaluation];
		  for(int i= 0; i<RDConstants.reEvaluation; i++){
		RDSystem syst = new RDSystem();
		
		 OligoGraph<SequenceVertex,String> g = GraphMaker.fromReactionNetwork(network);
		  g.exoConc = RDConstants.exoConc;
		  g.polConc = RDConstants.polConc;
		  g.nickConc = RDConstants.nickConc;
		 
		  syst.setNetwork(network);
			syst.setOS(new OligoSystem<String>(g, new PadiracTemplateFactory(g)));
		  syst.init(false); //no GUI
		  
		  for(int step=0; step<RDConstants.maxTimeEval; step++){
			  syst.update();
		  }
		  if(RDConstants.timing){
			  System.out.println("total time: "+(System.currentTimeMillis()-startTime));
			  System.out.println("total bead update:"+syst.totalBeads);
			  System.out.println("total conc update:"+syst.totalConc);
		  }
		AbstractFitnessResult temp = new RDPatternBlurFitnessResult(syst.conc,dists,syst.beadsOnSpot, randomFitness);
		results[i] = temp;
		  }
		  
		  Arrays.sort(results, new AbstractFitnessResult.AbstractFitnessResultComparator());
		  if (RDConstants.useMedian) return results[(RDConstants.reEvaluation-1)/2];
		  return results[0];
	}
	
	@Override
	public AbstractFitnessResult evaluate(OligoGraph<SequenceVertex,String> g){
		long startTime = System.currentTimeMillis();
		RDSystem syst = new RDSystem();
		syst.setNetwork(null); //TODO: should have a reverse engineering function. Also repeated code with RDFitnessFunction
		syst.setOS(new OligoSystem<String>(g, new PadiracTemplateFactory(g)));
		  syst.init(false); //no GUI
		  
		  for(int step=0; step<RDConstants.maxTimeEval; step++){
			  syst.update();
		  }
		  if(RDConstants.timing){
			  System.out.println("total time: "+(System.currentTimeMillis()-startTime));
			  System.out.println("total bead update:"+syst.totalBeads);
			  System.out.println("total conc update:"+syst.totalConc);
		  }
		return new RDPatternBlurFitnessResult(syst.conc,dists,syst.beadsOnSpot, randomFitness);
	}
	

	public static void main(String[] args){
		boolean[][] pattern = new boolean[7][7];
		float width = 0.3f;
		
		for(int i = 0; i<pattern.length; i++){
			for(int j = 0; j<pattern[i].length;j++){
				pattern[i][j] = (i>=pattern.length*(0.5f-width/2.0f)&&i<=pattern.length*(0.5f+width/2.0f));
			}
		}
		
		RDFitnessBlurFunction fun = new RDFitnessBlurFunction(pattern);
		
		for(int i = 0; i<fun.dists.length;i++) System.out.println(Arrays.toString(fun.pattern[i]));
		
		System.out.println(" ");
		
		for(int i = 0; i<fun.dists.length;i++) System.out.println(Arrays.toString(fun.dists[i]));
	}
	
}
