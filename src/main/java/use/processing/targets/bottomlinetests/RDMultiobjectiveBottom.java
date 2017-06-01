package use.processing.targets.bottomlinetests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import erne.algorithm.EvolutionaryAlgorithm;
import erne.algorithm.bioNEAT.BioNEATBuilder;
import erne.algorithm.nsgaII.NSGAIIBuilder;
import erne.algorithm.nsgaII.NSGAIIPopulationFactory;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.PruningMutator;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import use.processing.multiobjective.RDArcNumberObjective;
import use.processing.multiobjective.RDFullPatternObjective;
import use.processing.multiobjective.RDInObjective;
import use.processing.multiobjective.RDMultievaluationObjective;
import use.processing.multiobjective.RDMultievaluationStabilityObjective;
import use.processing.multiobjective.RDMultiobjectiveFitnessFunction;
import use.processing.multiobjective.RDObjective;
import use.processing.multiobjective.RDOutObjective;
import use.processing.mutation.rules.AddActivationWithGradients;
import use.processing.mutation.rules.AddInhibitionWithGradients;
import use.processing.mutation.rules.AddNodeWithGradients;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessDisplayer;
import use.processing.rd.RDFitnessFunctionIbuki;
import use.processing.rd.RDPatternFitnessResultIbuki;
import utils.RDLibrary;

public class RDMultiobjectiveBottom {
	public static void main(String[] args) throws InterruptedException,ExecutionException,IOException,ClassNotFoundException{
		RDPatternFitnessResultIbuki.width = 0.2;
		boolean[][] target = RDPatternFitnessResultIbuki.getBottomLine();
		RDPatternFitnessResultIbuki.weightExponential = 0.1; //good candidate so far: 0.1 0.1
		RDConstants.matchPenalty=-0.1;
		
		  RDConstants.evalRandomDistance = false;
		   RDConstants.populationSize=50;
		RDConstants.maxGeneration = 200;
		RDConstants.maxTimeEval = 1000;
		RDConstants.hardTrim = false;
		RDConstants.maxNodes = 16;
		RDConstants.maxBeads = 500;
		
		RDConstants.useMedian = false; //use median score of reevaluations
		RDConstants.weightDisableTemplate = 1;
		  RDConstants.weightMutateParameter = 90;
		  RDConstants.weightAddActivationWithGradients = 3;
		  RDConstants.weightAddInhibitionWithGradients = 3;
		  RDConstants.weightAddNodeWithGradients = 3;
		
		RDConstants.targetName = "ClusterBottomMultiobjective";
		
		RDConstants.reEvaluation = 2;
		
		
		ArrayList<RDObjective> directObjs = new ArrayList<RDObjective>();
		//directObjs.add(new RDFullPatternObjective());
		//directObjs.add(new RDArcNumberObjective());
		directObjs.add(new RDInObjective());
		directObjs.add(new RDOutObjective());
		
		ArrayList<RDMultievaluationObjective> multiObjs = new ArrayList<RDMultievaluationObjective>();
		multiObjs.add(new RDMultievaluationStabilityObjective());
		//RDConstants.showBeads = true;
		//RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
		RDMultiobjectiveFitnessFunction fitnessFunction = new RDMultiobjectiveFitnessFunction(target,directObjs,multiObjs);
		
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