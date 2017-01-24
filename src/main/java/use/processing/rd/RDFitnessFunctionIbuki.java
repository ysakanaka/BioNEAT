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

public class RDFitnessFunctionIbuki extends RDFitnessFunction{
 private static final long serialVersionUID=-261536139595844890L;
 
 
 public RDFitnessFunctionIbuki(boolean[][] pattern){
  super(pattern);
 }
 /**
  * Basically the same of the original class.
  * It returns my original fitness result object. 
  */
 @Override public AbstractFitnessResult evaluate(ReactionNetwork network){
  long startTime=System.currentTimeMillis();
  AbstractFitnessResult[] results = new AbstractFitnessResult[RDConstants.reEvaluation];
  for(int i= 0; i<RDConstants.reEvaluation; i++){
  RDSystem syst=new RDSystem();
  OligoGraph<SequenceVertex,String> g=GraphMaker.fromReactionNetwork(network);
  g.exoConc=RDConstants.exoConc;
  g.polConc=RDConstants.polConc;
  g.nickConc=RDConstants.nickConc;
  syst.os=new OligoSystem<String>(g,new PadiracTemplateFactory(g));
  syst.init(false); // no GUI
  for(int step=0;step<RDConstants.maxTimeEval;step++){
   syst.update();
  }
  if(RDConstants.timing){
   System.out.println("total time: "+(System.currentTimeMillis()-startTime));
   System.out.println("total bead update:"+syst.totalBeads);
   System.out.println("total conc update:"+syst.totalConc);
  }
  RDPatternFitnessResultIbuki temp = new RDPatternFitnessResultIbuki(syst.conc,pattern,syst.beadsOnSpot,randomFitness);
  results[i] = temp;
  }
  
  Arrays.sort(results, new AbstractFitnessResult.AbstractFitnessResultComparator());
  if (RDConstants.useMedian) return results[(RDConstants.reEvaluation-1)/2];
  return results[0];
 }
 
}
 
