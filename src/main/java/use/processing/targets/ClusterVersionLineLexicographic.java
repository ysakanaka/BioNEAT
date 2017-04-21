package use.processing.targets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import erne.algorithm.BioNEATBuilder;
import erne.algorithm.EvolutionaryAlgorithm;
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
import use.processing.rd.RDLexicographicFitnessFunction;
import use.processing.rd.RDPatternFitnessResultIbuki;
import utils.RDLibrary;

public class ClusterVersionLineLexicographic {
	public static void main(String[] args) throws InterruptedException,ExecutionException,IOException,ClassNotFoundException{
		RDPatternFitnessResultIbuki.width = 0.3; 
		boolean[][] target=RDPatternFitnessResultIbuki.getCenterLine();
		//  boolean [][] target=RDPatternFitnessResultIbuki.getSmileyFace();
		 // boolean[][] target=RDPatternFitnessResultIbuki.getTopLine();
		  // RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
		 
		  RDConstants.populationSize=100;
		  RDConstants.maxGeneration=200;
		  RDConstants.maxTimeEval=5000; //TODO wrong val
		  RDConstants.hardTrim = false;
			RDConstants.maxNodes = 14;
			RDConstants.maxBeads = 500;
		  RDConstants.reEvaluation = 2;
		  RDConstants.showBeads = true;
			RDConstants.weightDisableTemplate = 1;
			  RDConstants.weightMutateParameter = 96;
			  RDConstants.weightAddActivationWithGradients = 1;
			  RDConstants.weightAddInhibitionWithGradients = 1;
			  RDConstants.weightAddNodeWithGradients = 1;
		  
		  RDConstants.targetName = "ClusterLineLexicographic";
		  
		  RDConstants.evalRandomDistance=false;
		  
		  RDConstants.defaultRandomFitness = RDPatternFitnessResultIbuki.width;

		   
		   RDConstants.useMatchFitness = false;
			RDConstants.useHellingerDistance = true;
			RDConstants.patternHellinger = false;
			//RDConstants.horizontalBins = 1;
			//RDConstants.verticalBins = 1;
		   
		  RDLexicographicFitnessFunction fitnessFunction = new RDLexicographicFitnessFunction(target);
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
