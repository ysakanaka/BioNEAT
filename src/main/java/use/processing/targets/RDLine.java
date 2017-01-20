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
import use.processing.rd.RDBeadPositionFitnessFunction;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessDisplayer;
import use.processing.rd.RDFitnessFunction;
import use.processing.rd.RDPatternFitnessResultIbuki;
import utils.RDLibrary;

/**
 * Defines a target with a centered line of ratio width compared to the whole area
 * @author naubertkato
 *
 */
public class RDLine {
	
	//public static float width = 0.3f; 
	//public static float offset = 0.5f*RDConstants.hsize;
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		boolean[][] target = RDPatternFitnessResultIbuki.getCenterLine();
		
		
		
		
		//RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
		RDConstants.evalRandomDistance = false;
		RDConstants.defaultRandomFitness = 0.0;
		RDConstants.useHellingerDistance = true;
		RDConstants.useMatchFitness = false;
		RDConstants.matchPenalty = -0.48;
		RDConstants.maxGeneration = 200;
		RDConstants.maxTimeEval = 10000;
		RDConstants.hardTrim = false;
		RDConstants.maxNodes = 16;
		
		RDConstants.targetName = "RDLine";
		
		//RDConstants.showBeads = true;
		//RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
		RDFitnessFunction fitnessFunction = new RDFitnessFunction(target);
		
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
