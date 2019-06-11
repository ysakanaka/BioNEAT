package use.processing.rd;

import java.util.ArrayList;
import java.util.Arrays;

import erne.AbstractFitnessResult;
import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import reactionnetwork.ReactionNetwork;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;

public class RDFitnessTransfert extends RDFitnessFunction{
 private static final long serialVersionUID=-261536139595844890L;
 protected int nApproxEvals; //number of evals using an approximate evaluation
 protected boolean hybrid; //alternate between approximate and real evals?
 protected int nRealEvals; //number of real evals before going back to approximate if hybrid
 protected int currentEvalNumber = 0;
 
 public RDFitnessTransfert(boolean[][] pattern, int nApproxEvals, boolean hybrid, int nRealEvals){
  super(pattern);
  this.nApproxEvals = nApproxEvals;
  this.hybrid = hybrid;
  this.nRealEvals = nRealEvals;
 }
 
 public RDFitnessTransfert(boolean[][] pattern, int nApproxEvals) {
	 this(pattern, nApproxEvals, false, 0);
 }
 
 
 /**
  * Basically the same of the original class.
  * It returns my original fitness result object. 
  */
 @Override public AbstractFitnessResult evaluate(ReactionNetwork network){
  //First, decide if we will use an approximate evaluation or not
  boolean useApproximate = currentEvalNumber < nApproxEvals || (hybrid && currentEvalNumber % (nApproxEvals+nRealEvals) < nApproxEvals);
  long startTime=System.currentTimeMillis();
  AbstractFitnessResult[] results = new AbstractFitnessResult[useApproximate?1:RDConstants.reEvaluation];
  //System.out.println(network);
  for(int i= 0; i<(useApproximate?1:RDConstants.reEvaluation); i++){
  RDSystem syst= useApproximate?new RDSystemApprox() : new RDSystem();
  OligoGraph<SequenceVertex,String> g=GraphMaker.fromReactionNetwork(network);
  g.exoConc=RDConstants.exoConc;
  g.polConc=RDConstants.polConc;
  g.nickConc=RDConstants.nickConc;
  syst.setNetwork(network);
	syst.setOS(new OligoSystem<String>(g, new PadiracTemplateFactory(g)));
  syst.init(false); // no GUI
  for(int step=0;step<RDConstants.maxTimeEval;step++){
   syst.update();
  }
  //System.out.println(Arrays.toString(syst.conc[3][10]));
  if(RDConstants.timing){
   System.out.println("total time: "+(System.currentTimeMillis()-startTime));
   System.out.println("total bead update:"+syst.totalBeads);
   System.out.println("total conc update:"+syst.totalConc);
  }
  RDPatternFitnessResultIbuki temp = new RDPatternFitnessResultIbuki(syst.conc,pattern,syst.beadsOnSpot,randomFitness);
  results[i] = temp;
  }
  
  Arrays.sort(results, new AbstractFitnessResult.AbstractFitnessResultComparator());
  if (RDConstants.useMedian) return results[(useApproximate?0:RDConstants.reEvaluation-1)/2];
  currentEvalNumber++;
  return results[0];
 }
 
}
 
