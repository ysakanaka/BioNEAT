package use.processing.rd;


import java.util.Arrays;

import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;
import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import reactionnetwork.ReactionNetwork;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;

public class RDFitnessFunction extends AbstractFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8422873429506377841L;
	
	protected boolean[][] pattern;
	protected double randomFitness;
	protected static final RDPatternFitnessResult minFitness = RDPatternFitnessResult.getMinFitness();
	
	protected RDFitnessFunction(){
		
	}
	
	public RDFitnessFunction(boolean[][] pattern){
		this.pattern = pattern;
		if(RDConstants.evalRandomDistance){
			if (RDConstants.useMatchFitness){
				randomFitness = PatternEvaluator.matchRandomDistribution(pattern)*RDConstants.spaceStep*RDConstants.spaceStep/(RDConstants.hsize*RDConstants.wsize);
			} else {
			    randomFitness = RDConstants.useHellingerDistance?1.0-PatternEvaluator.hellingerDistanceRandomDistribution(pattern):
			    		RDConstants.hsize*RDConstants.wsize/(PatternEvaluator.distanceRandomDistribution(pattern)*RDConstants.spaceStep*RDConstants.spaceStep);
			}
			System.out.println("Random fitness evaluation done: "+randomFitness);
		} else {
			randomFitness = RDConstants.defaultRandomFitness;
		}
		if (randomFitness < 0) randomFitness = 0.0;
	}

	public static void main(String[] args) {
		//TODO

	}
	
	protected AbstractFitnessResult computeResult(RDSystem syst){
		return new RDPatternFitnessResult(syst.conc,pattern,syst.beadsOnSpot, randomFitness);
	}

	@Override
	public AbstractFitnessResult evaluate(ReactionNetwork network) {
		AbstractFitnessResult[] results = multipleEvaluation(network);
		  
		  Arrays.sort(results, new AbstractFitnessResult.AbstractFitnessResultComparator());
		  if (RDConstants.useMedian) return results[(RDConstants.reEvaluation-1)/2];
		  return results[0];
	}
	
	protected AbstractFitnessResult[] multipleEvaluation(ReactionNetwork network){
		long startTime = System.currentTimeMillis();
		AbstractFitnessResult[] results = new AbstractFitnessResult[RDConstants.reEvaluation];
		  for(int i= 0; i<RDConstants.reEvaluation; i++){
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
		  AbstractFitnessResult temp =  computeResult(syst);
		  results[i] = temp;
		  }
		  return results;
	}
	
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
		return new RDPatternFitnessResult(syst.conc,pattern,syst.beadsOnSpot, randomFitness);
	}

	@Override
	public AbstractFitnessResult minFitness() {
		
		return minFitness;
	}

}
