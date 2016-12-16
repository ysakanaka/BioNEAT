package use.processing.rd;

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
		RDSystem syst = new RDSystem();
		
		 OligoGraph<SequenceVertex,String> g = GraphMaker.fromReactionNetwork(network);
		  g.exoConc = RDConstants.exoConc;
		  g.polConc = RDConstants.polConc;
		  g.nickConc = RDConstants.nickConc;
		 
		  syst.os = new OligoSystem<String>(g, new PadiracTemplateFactory(g));
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
	
	@Override
	public AbstractFitnessResult evaluate(OligoGraph<SequenceVertex,String> g){
		long startTime = System.currentTimeMillis();
		RDSystem syst = new RDSystem();
		syst.os = new OligoSystem<String>(g, new PadiracTemplateFactory(g));
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
	

}
