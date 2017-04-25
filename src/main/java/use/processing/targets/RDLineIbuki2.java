package use.processing.targets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import erne.algorithm.EvolutionaryAlgorithm;
import erne.algorithm.bioNEAT.BioNEATBuilder;
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
import use.processing.rd.RDPatternFitnessResultIbuki;
import utils.RDLibrary;

public class RDLineIbuki2 {
	public static float width=0.2f;
	 public static float offset=0.5f*RDConstants.hsize;
	 public static void main(String[] args) throws InterruptedException,ExecutionException,IOException,ClassNotFoundException{
	//  boolean[][] target=RDPatternFitnessResultIbuki.getCenterLine();
	//  boolean [][] target=RDPatternFitnessResultIbuki.getSmileyFace();
	  boolean[][] target=RDPatternFitnessResultIbuki.getTopLine();
	  // RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
	  //RDConstants.evalRandomDistance=true;
	  // RDConstants.defaultRandomFitness = 0.0;
	  RDPatternFitnessResultIbuki.weightExponential = 1.0/Math.E;
	  RDConstants.matchPenalty=-RDPatternFitnessResultIbuki.weightExponential;
	  RDConstants.populationSize=10;
	  RDConstants.maxGeneration=100;
	  RDConstants.maxTimeEval=2000;
	  RDConstants.weightDisableTemplate=3;

	  RDConstants.targetName = "Ibuki2";
	  
	  RDConstants.evalRandomDistance=false;
	  boolean[][] fullMap = new boolean[target.length][target[0].length];
	  for(int i=0;i<fullMap.length; i++){
		  for(int j=0;j<fullMap[0].length; j++){
			  fullMap[i][j] = true;
		  }
	  }
	   RDConstants.defaultRandomFitness = RDPatternFitnessResultIbuki.distanceBlurExponential(target,fullMap);
	  
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
	  EvolutionaryAlgorithm algorithm = new BioNEATBuilder().mutator(mutator).buildAlgorithm();
      
		Evolver evolver = new Evolver(RDConstants.populationSize, RDConstants.maxGeneration, RDLibrary.rdstart,
				fitnessFunction, new RDFitnessDisplayer(), algorithm);
	  //evolver.setGUI(true);
	  evolver.setExtraConfig(RDConstants.configsToString());
	  evolver.evolve();
	  System.out.println("Evolution completed.");
	  if(!Evolver.hasGUI()) System.exit(0);
	 }
}
