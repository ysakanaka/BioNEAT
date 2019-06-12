package use.processing.rd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

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
 protected static AtomicInteger currentEvalNumber = new AtomicInteger(0);
 
 protected boolean[][] patternApprox; //may be of a different size or shape
 
 protected float realSpaceStep = RDConstants.spaceStep;
 protected float approxSpaceStep = RDConstants.approxSpaceStep;
 protected double realMatchPenalty = RDConstants.matchPenalty;
 protected double approxMatchPenalty = RDConstants.approxMatchPenalty;
 
 public RDFitnessTransfert(boolean[][] pattern, boolean[][] patternApprox, int nApproxEvals, boolean hybrid, int nRealEvals){
  super(pattern);
  this.nApproxEvals = nApproxEvals;
  this.hybrid = hybrid;
  this.nRealEvals = nRealEvals;
  this.patternApprox = patternApprox;
 }
 
 public RDFitnessTransfert(boolean[][] pattern, int nApproxEvals) {
	 this(pattern, pattern, nApproxEvals, false, 0);
 }
 
 
 /**
  * Basically the same of the original class.
  * It returns my original fitness result object. 
  */
 @Override public AbstractFitnessResult evaluate(ReactionNetwork network){
  //First, decide if we will use an approximate evaluation or not
	 int currentVal = currentEvalNumber.getAndIncrement();
  boolean useApproximate = currentVal < nApproxEvals || (hybrid && currentVal % (nApproxEvals+nRealEvals) < nApproxEvals);
  if(useApproximate) {
	  RDConstants.matchPenalty = approxMatchPenalty; //TODO:not thread safe!
	  RDConstants.spaceStep = approxSpaceStep;
  } else {
	  RDConstants.matchPenalty = realMatchPenalty; //TODO:not thread safe! Fine, as long as we change per gen
	  RDConstants.spaceStep = realSpaceStep;
  }
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
  RDPatternFitnessResultIbuki temp = new RDPatternFitnessResultIbuki(syst.conc,useApproximate?patternApprox:pattern,syst.beadsOnSpot,randomFitness);
  results[i] = temp;
  }
  
  Arrays.sort(results, new AbstractFitnessResult.AbstractFitnessResultComparator());
  if (RDConstants.useMedian) return results[(useApproximate?0:RDConstants.reEvaluation-1)/2];
 
  return results[0];
 }
 
}
 
