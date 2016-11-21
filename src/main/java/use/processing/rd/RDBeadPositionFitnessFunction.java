package use.processing.rd;

import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;
import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import reactionnetwork.ReactionNetwork;
import use.processing.targets.BeadPositionTarget;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;

public class RDBeadPositionFitnessFunction extends AbstractFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7453389085701880972L;
	protected BeadPositionTarget target;
	protected boolean[][] pattern;
	
	public RDBeadPositionFitnessFunction(BeadPositionTarget target, boolean [][] pattern){
		this.target = target;
		this.pattern = pattern;
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
		  
		return new RDBeadPositionFitnessResult(syst.conc,pattern,syst.os.total+syst.os.inhTotal, 0.0,target,syst.beads);
	}

	@Override
	public AbstractFitnessResult minFitness() {
		return RDBeadPositionFitnessResult.minFitness;
	}

}
