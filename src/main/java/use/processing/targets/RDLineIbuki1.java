package use.processing.targets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import erne.Evolver;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.PruningMutator;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import use.processing.mutation.rules.AddActivationWithGradients;
import use.processing.mutation.rules.AddInhibitionWithGradients;
import use.processing.mutation.rules.AddNodeWithGradients;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessDisplayer;
import use.processing.rd.RDFitnessFunctionIbuki;
import utils.RDLibrary;

/**
 * Defines a target with a centered line of ratio width compared to the whole area
 */
public class RDLineIbuki1{
 public static float width=0.2f;
 public static float offset=0.5f*RDConstants.hsize;
 public static void main(String[] args) throws InterruptedException,ExecutionException,IOException,ClassNotFoundException{
  boolean[][] target=new boolean[(int)(RDConstants.wsize/RDConstants.spaceStep)][(int)(RDConstants.hsize/RDConstants.spaceStep)];
  for(int i=0;i<target.length;i++){
   for(int j=0;j<target[i].length;j++){
    target[i][j]=(i>=target.length*(0.5f-width/2.0f)&&i<=target.length*(0.5f+width/2.0f));
   }
  }
  // RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
  RDConstants.evalRandomDistance=true;
  // RDConstants.defaultRandomFitness = 0.0;
  RDConstants.useGlueAsTarget=false;
  RDConstants.matchPenalty=-0.1;
  RDConstants.populationSize=50;
  RDConstants.maxGeneration=200;
  RDConstants.maxTimeEval=3000;
  RDConstants.maxNodes=16;
  // RDConstants.showBeads = true;
  // RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
  RDFitnessFunctionIbuki fitnessFunction=new RDFitnessFunctionIbuki(target);
  Mutator mutator;
  if(RDConstants.hardTrim){
   mutator=new PruningMutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[]{new DisableTemplate(RDConstants.weightDisableTemplate),new MutateParameter(RDConstants.weightMutateParameter),new AddNodeWithGradients(RDConstants.weightAddNodeWithGradients),new AddActivationWithGradients(RDConstants.weightAddActivationWithGradients),new AddInhibitionWithGradients(RDConstants.weightAddInhibitionWithGradients)})));
  }else{
   mutator=new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[]{new DisableTemplate(RDConstants.weightDisableTemplate),new MutateParameter(RDConstants.weightMutateParameter),new AddNodeWithGradients(RDConstants.weightAddNodeWithGradients),new AddActivationWithGradients(RDConstants.weightAddActivationWithGradients),new AddInhibitionWithGradients(RDConstants.weightAddInhibitionWithGradients)})));
  }
  Evolver evolver=new Evolver(RDConstants.populationSize,RDConstants.maxGeneration,RDLibrary.rdstart,fitnessFunction,mutator,new RDFitnessDisplayer());
  evolver.setGUI(true);
  evolver.setExtraConfig(RDConstants.configsToString());
  evolver.evolve();
  System.out.println("Evolution completed.");
 }
}
