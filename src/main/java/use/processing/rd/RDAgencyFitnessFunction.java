package use.processing.rd;


import java.util.Arrays;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;
import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import reactionnetwork.ReactionNetwork;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;

public class RDAgencyFitnessFunction extends AbstractFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8422873429506377841L;
	
	protected boolean[][] pattern;
	protected static final RDPatternFitnessResult minFitness = RDPatternFitnessResult.getMinFitness();
	
	public RDAgencyFitnessFunction(){
		
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
		  syst.init(false); //no GUI or yes
		  
		  float concHistory[][][][] = new float[RDConstants.maxTimeEval/RDConstants.bigTimeStep][syst.chemicalSpecies][(int) (RDConstants.wsize/RDConstants.spaceStep)][(int) (RDConstants.hsize/RDConstants.spaceStep)];
		  
		  for(int step=0; step<RDConstants.maxTimeEval; step++){
			  if(step % RDConstants.bigTimeStep == 0) {
				  for(int i = 0; i< syst.chemicalSpecies; i++){
					  for(int j=0; j< syst.conc[i].length; j++){
						  for(int k= 0; k < syst.conc[i][j].length; k++){
							  concHistory[step/RDConstants.bigTimeStep][i][j][k] = syst.conc[i][j][k];
						  }
					  }
				  }
				  
			  }
			  syst.update();
			  
		  }
		  if(RDConstants.timing){
			  System.out.println("total time: "+(System.currentTimeMillis()-startTime));
			  System.out.println("total bead update:"+syst.totalBeads);
			  System.out.println("total conc update:"+syst.totalConc);
		  }
		return new RDAgencyFitnessResult(syst.conc, concHistory);//new RDPatternFitnessResult(syst.conc,pattern,syst.beadsOnSpot, randomFitness);
	}
	
	public AbstractFitnessResult evaluate(OligoGraph<SequenceVertex,String> g){
		long startTime = System.currentTimeMillis();
		RDSystem syst = new RDSystem();
		syst.os = new OligoSystem<String>(g, new PadiracTemplateFactory(g));
		  syst.init(false); //no GUI

		float concHistory[][][][] = new float[RDConstants.maxTimeEval
				/ RDConstants.bigTimeStep][syst.chemicalSpecies][(int) (RDConstants.wsize
						/ RDConstants.spaceStep)][(int) (RDConstants.hsize / RDConstants.spaceStep)];

		for (int step = 0; step < RDConstants.maxTimeEval; step++) {
			if (step % RDConstants.bigTimeStep == 0) {
				for (int i = 0; i < syst.chemicalSpecies; i++) {
					for (int j = 0; j < syst.conc[i].length; j++) {
						for (int k = 0; k < syst.conc[i][j].length; k++) {
							concHistory[step / RDConstants.bigTimeStep][i][j][k] = syst.conc[i][j][k];
						}
					}
				}

			}
			syst.update();
		}
		  if(RDConstants.timing){
			  System.out.println("total time: "+(System.currentTimeMillis()-startTime));
			  System.out.println("total bead update:"+syst.totalBeads);
			  System.out.println("total conc update:"+syst.totalConc);
		  }
		  return new RDAgencyFitnessResult(syst.conc, concHistory);
	}

	@Override
	public AbstractFitnessResult minFitness() {
		
		return minFitness;
	}

}
