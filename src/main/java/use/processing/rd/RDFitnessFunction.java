package use.processing.rd;

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
	protected static final RDPatternFitnessResult minFitness = new RDPatternFitnessResult(new float[1][1][1], new boolean[1][1],0,0.0);
	
	public RDFitnessFunction(boolean[][] pattern){
		this.pattern = pattern;
		if(RDConstants.evalRandomDistance){
			if (RDConstants.useMatchFitness){
				randomFitness = PatternEvaluator.matchRandomDistribution(pattern)*RDConstants.spaceStep*RDConstants.spaceStep/(RDConstants.hsize*RDConstants.wsize);
			} else {
			    randomFitness =RDConstants.hsize*RDConstants.wsize/(PatternEvaluator.distanceRandomDistribution(pattern)*RDConstants.spaceStep*RDConstants.spaceStep);
			}
			System.out.println("Random fitness evaluation done: "+randomFitness);
		} else {
			randomFitness = RDConstants.defaultRandomFitness;
		}
	}

	public static void main(String[] args) {
		//TODO

	}
	

	@Override
	public AbstractFitnessResult evaluate(ReactionNetwork network) {
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
		  
		return new RDPatternFitnessResult(syst.conc,pattern,syst.os.total+syst.os.inhTotal, randomFitness);
	}

	@Override
	public AbstractFitnessResult minFitness() {
		
		return minFitness;
	}

}
