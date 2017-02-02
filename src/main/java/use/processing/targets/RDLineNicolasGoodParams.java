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
import use.processing.rd.RDPatternFitnessResultIbuki;
import utils.RDLibrary;

public class RDLineNicolasGoodParams {
	public static void main(String[] args) throws InterruptedException,ExecutionException,IOException,ClassNotFoundException{
	RDPatternFitnessResultIbuki.width = 0.3;
	boolean[][] target = RDPatternFitnessResultIbuki.getCenterLine();
	RDPatternFitnessResultIbuki.weightExponential = 0.2; //good candidate so far: 0.1 0.1
	  RDConstants.matchPenalty=-0.02;
	
	RDConstants.reEvaluation = 1;
	
	//RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
	RDConstants.evalRandomDistance = false;
	boolean[][] fullMap = new boolean[target.length][target[0].length];
	  for(int i=0;i<fullMap.length; i++){
		  for(int j=0;j<fullMap[0].length; j++){
			  fullMap[i][j] = true;
		  }
	  }
	   RDConstants.defaultRandomFitness = Math.max(0.0, RDPatternFitnessResultIbuki.distanceNicolasExponential(target,fullMap));
	   System.out.println("Default fitness: "+RDConstants.defaultRandomFitness);
	   RDConstants.populationSize=50;
	RDConstants.maxGeneration = 400;
	RDConstants.maxTimeEval = 5000;
	RDConstants.hardTrim = false;
	RDConstants.maxNodes = 16;
	RDConstants.maxBeads = 500;
	RDConstants.showBeads = true;
	RDConstants.weightDisableTemplate = 1;
	  RDConstants.weightMutateParameter = 96;
	  RDConstants.weightAddActivationWithGradients = 1;
	  RDConstants.weightAddInhibitionWithGradients = 1;
	  RDConstants.weightAddNodeWithGradients = 1;
	  RDConstants.cutOff = 10.0f;
	
	RDConstants.targetName = "ClusterLineIbuki24";
	
	//RDConstants.showBeads = true;
	//RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
	RDFitnessFunctionIbuki fitnessFunction = new RDFitnessFunctionIbuki(target);
	
Mutator mutator;
    
    if(RDConstants.hardTrim){
    	mutator = new PruningMutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {
    			new DisableTemplate(RDConstants.weightDisableTemplate), 
    			new MutateParameter(RDConstants.weightMutateParameter), 
    			new AddNodeWithGradients(RDConstants.weightAddNodeWithGradients), 
    			new AddActivationWithGradients(RDConstants.weightAddActivationWithGradients), 
    			new AddInhibitionWithGradients(RDConstants.weightAddInhibitionWithGradients)})));
    } else {
        mutator = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {
			new DisableTemplate(RDConstants.weightDisableTemplate), 
			new MutateParameter(RDConstants.weightMutateParameter), 
			new AddNodeWithGradients(RDConstants.weightAddNodeWithGradients), 
			new AddActivationWithGradients(RDConstants.weightAddActivationWithGradients), 
			new AddInhibitionWithGradients(RDConstants.weightAddInhibitionWithGradients)})));
    }
	Evolver evolver = new Evolver(RDConstants.populationSize, RDConstants.maxGeneration, RDLibrary.rdstart,
			fitnessFunction, mutator, new RDFitnessDisplayer());
	//evolver.setGUI(false);
	evolver.setExtraConfig(RDConstants.configsToString());
	evolver.evolve();
    System.out.println("Evolution completed.");
    if(!Evolver.hasGUI()) System.exit(0);
}
}
