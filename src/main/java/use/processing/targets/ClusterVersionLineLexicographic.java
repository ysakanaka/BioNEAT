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
import use.processing.rd.RDLexicographicFitnessFunction;
import use.processing.rd.RDPatternFitnessResultIbuki;
import utils.RDLibrary;

public class ClusterVersionLineLexicographic {
	public static void main(String[] args) throws InterruptedException,ExecutionException,IOException,ClassNotFoundException{
		 boolean[][] target=RDPatternFitnessResultIbuki.getCenterLine();
		//  boolean [][] target=RDPatternFitnessResultIbuki.getSmileyFace();
		 // boolean[][] target=RDPatternFitnessResultIbuki.getTopLine();
		  // RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
		 
		  RDConstants.populationSize=50;
		  RDConstants.maxGeneration=200;
		  RDConstants.maxTimeEval=10000;
		  RDConstants.hardTrim = false;
			RDConstants.maxNodes = 16;
			RDConstants.maxBeads = 500;
		  RDConstants.reEvaluation = 2;
		  
		  RDConstants.targetName = "ClusterLineLexicographic";
		  
		  RDConstants.evalRandomDistance=false;
		  
		   RDConstants.defaultRandomFitness = 0.25;

		   
		   RDConstants.useMatchFitness = false;
			RDConstants.useHellingerDistance = true;
			//RDConstants.horizontalBins = 1;
			//RDConstants.verticalBins = 1;
		   
		  RDLexicographicFitnessFunction fitnessFunction = new RDLexicographicFitnessFunction(target);
		  Mutator mutator;
		  if(RDConstants.hardTrim){
		   mutator=new PruningMutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[]{new DisableTemplate(RDConstants.weightDisableTemplate),new MutateParameter(RDConstants.weightMutateParameter),new AddNodeWithGradients(RDConstants.weightAddNodeWithGradients),new AddActivationWithGradients(RDConstants.weightAddActivationWithGradients),new AddInhibitionWithGradients(RDConstants.weightAddInhibitionWithGradients)})));
		  }else{
		   mutator=new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[]{new DisableTemplate(RDConstants.weightDisableTemplate),new MutateParameter(RDConstants.weightMutateParameter),new AddNodeWithGradients(RDConstants.weightAddNodeWithGradients),new AddActivationWithGradients(RDConstants.weightAddActivationWithGradients),new AddInhibitionWithGradients(RDConstants.weightAddInhibitionWithGradients)})));
		  }
		  Evolver evolver=new Evolver(RDConstants.populationSize,RDConstants.maxGeneration,RDLibrary.rdstart,fitnessFunction,mutator,new RDFitnessDisplayer());
		  //evolver.setGUI(true);
		  evolver.setExtraConfig(RDConstants.configsToString());
		  evolver.evolve();
		  System.out.println("Evolution completed.");
		  if(!Evolver.hasGUI()) System.exit(0);
		 }
}
