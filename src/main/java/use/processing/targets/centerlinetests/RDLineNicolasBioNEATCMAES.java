package use.processing.targets.centerlinetests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import erne.algorithm.EvolutionaryAlgorithm;
import erne.algorithm.bioNEAT.BioNEATBuilder;
import erne.algorithm.bioNEAT.BioNEATCMAESPopulation;
import erne.algorithm.bioNEAT.BioNEATCMAESPopulationFactory;
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

public class RDLineNicolasBioNEATCMAES {
	public static void main(String[] args) throws InterruptedException,ExecutionException,IOException,ClassNotFoundException{
	RDPatternFitnessResultIbuki.width = 0.2;
	boolean[][] target = RDPatternFitnessResultIbuki.getCenterLine();
	RDPatternFitnessResultIbuki.weightExponential = 0.1; //good candidate so far: 0.1 0.1
	  RDConstants.matchPenalty=-0.07; //0.1 was bad. 0.05 made blobs 0.01 seems to fill everything
	  //running: -0.03 favors just covering everything and -0.07 blobs so far
	
	  RDConstants.reEvaluation = 2;
		
		//RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
		RDConstants.evalRandomDistance = false;
		boolean[][] fullMap = new boolean[target.length][target[0].length];
		  for(int i=0;i<fullMap.length; i++){
			  for(int j=0;j<fullMap[0].length; j++){
				  fullMap[i][j] = true;
			  }
		  }
		   RDConstants.defaultRandomFitness = Math.max(0.0, RDPatternFitnessResultIbuki.distanceNicolasExponential(target,fullMap)); //TODO: carefull, not standard
		   System.out.println("Default fitness: "+RDConstants.defaultRandomFitness);
		   RDConstants.populationSize=20;
		RDConstants.maxGeneration = 100; //must be multiplied by evaluations per individual through CMAES: 400 by default. So 40000 evals in the end 
		RDConstants.maxTimeEval = 4000;
		BioNEATCMAESPopulation.cmaesPopulation = 20;
		BioNEATCMAESPopulation.cmaesRounds = 5;
		RDConstants.hardTrim = false;
		RDConstants.maxNodes = 16;
		RDConstants.maxBeads = 500;
		RDConstants.showBeads = false;
		RDConstants.useMedian = true; //use median score of reevaluations
		RDConstants.weightDisableTemplate = 25;
		  RDConstants.weightAddActivationWithGradients = 25;
		  RDConstants.weightAddInhibitionWithGradients = 25;
		  RDConstants.weightAddNodeWithGradients = 25;
	  //RDConstants.cutOff = 0.5f;
	
	RDConstants.targetName = "BioNEATCMAESLineNicolas";
	
	//RDConstants.showBeads = true;
	//RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
	RDFitnessFunctionIbuki fitnessFunction = new RDFitnessFunctionIbuki(target);
	
Mutator mutator;
    
    if(RDConstants.hardTrim){
    	mutator = new PruningMutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {
    			new DisableTemplate(RDConstants.weightDisableTemplate), 
    			new AddNodeWithGradients(RDConstants.weightAddNodeWithGradients), 
    			new AddActivationWithGradients(RDConstants.weightAddActivationWithGradients), 
    			new AddInhibitionWithGradients(RDConstants.weightAddInhibitionWithGradients)})));
    } else {
        mutator = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {
			new DisableTemplate(RDConstants.weightDisableTemplate), 
			new AddNodeWithGradients(RDConstants.weightAddNodeWithGradients), 
			new AddActivationWithGradients(RDConstants.weightAddActivationWithGradients), 
			new AddInhibitionWithGradients(RDConstants.weightAddInhibitionWithGradients)})));
    }
    EvolutionaryAlgorithm algorithm = 
      new BioNEATBuilder().mutator(mutator).populationFactory(new BioNEATCMAESPopulationFactory()).buildAlgorithm();
	Evolver evolver = new Evolver(RDConstants.populationSize, RDConstants.maxGeneration, RDLibrary.rdstart,
			fitnessFunction, new RDFitnessDisplayer(), algorithm);
	//evolver.setGUI(false);
	evolver.setExtraConfig(RDConstants.configsToString()+"\n"+RDConstants.configsToString(BioNEATCMAESPopulation.class));
	evolver.evolve();
    System.out.println("Evolution completed.");
    if(!Evolver.hasGUI()) System.exit(0);
}
}
