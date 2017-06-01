package use.processing.targets.centerlinetests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import erne.algorithm.EvolutionaryAlgorithm;
import erne.algorithm.nsgaII.NSGAIIBuilder;
import erne.algorithm.nsgaII.NSGAIIPopulationFactory;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.PruningMutator;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import use.processing.multiobjective.RDArcNumberObjective;
import use.processing.multiobjective.RDInObjective;
import use.processing.multiobjective.RDMultiobjectiveFitnessFunction;
import use.processing.multiobjective.RDObjective;
import use.processing.multiobjective.RDOutObjective;
import use.processing.mutation.rules.AddActivationWithGradients;
import use.processing.mutation.rules.AddInhibitionWithGradients;
import use.processing.mutation.rules.AddNodeWithGradients;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessDisplayer;
import use.processing.rd.RDPatternFitnessResultIbuki;
import utils.RDLibrary;

public class RDCenterLineMultiobjectiveInOutNodes {
	public static void main(String[] args) throws InterruptedException,ExecutionException,IOException,ClassNotFoundException{
		RDPatternFitnessResultIbuki.width = 0.2;
		boolean[][] target = RDPatternFitnessResultIbuki.getCenterLine();
		
		  RDConstants.evalRandomDistance = false;
		   RDConstants.populationSize=50;
		RDConstants.maxGeneration = 200;
		RDConstants.maxTimeEval = 4000;
		RDConstants.hardTrim = false;
		RDConstants.maxNodes = 16;
		RDConstants.maxBeads = 500;
		
		RDConstants.useMedian = false; //use median score of reevaluations
		RDConstants.reEvaluation = 3;
		RDConstants.weightDisableTemplate = 2;
		  RDConstants.weightMutateParameter = 80;
		  RDConstants.weightAddActivationWithGradients = 6;
		  RDConstants.weightAddInhibitionWithGradients = 6;
		  RDConstants.weightAddNodeWithGradients = 6;
		
		Class<?> currentClass = new Object() { }.getClass().getEnclosingClass();
		RDConstants.targetName = currentClass.getName();
		
		
		ArrayList<RDObjective> objs = new ArrayList<RDObjective>();
		objs.add(new RDInObjective());
		objs.add(new RDOutObjective());
		objs.add(new RDArcNumberObjective());
		RDMultiobjectiveFitnessFunction fitnessFunction = new RDMultiobjectiveFitnessFunction(target,objs);
		
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
EvolutionaryAlgorithm algorithm = new NSGAIIBuilder().mutator(mutator).populationFactory(new NSGAIIPopulationFactory(true)).buildAlgorithm();
        
		Evolver evolver = new Evolver(RDConstants.populationSize, RDConstants.maxGeneration, RDLibrary.rdstart,
				fitnessFunction, new RDFitnessDisplayer(), algorithm);
		//evolver.setGUI(true);
		evolver.setExtraConfig(RDConstants.configsToString());
		evolver.evolve();
	    System.out.println("Evolution completed.");
	    if(!Evolver.hasGUI()) System.exit(0);
	}
}
