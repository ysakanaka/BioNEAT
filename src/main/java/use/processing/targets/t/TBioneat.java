package use.processing.targets.t;

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

public class TBioneat {
    public static void main(String[] args) throws InterruptedException,ExecutionException,IOException,ClassNotFoundException{
        RDPatternFitnessResultIbuki.width = 0.2;
        //RDConstants.spaceStep = 8;
        RDConstants.useApprox = false;
		boolean[][] target = RDPatternFitnessResultIbuki.getTPattern();
        RDPatternFitnessResultIbuki.weightExponential = 0.1; //good candidate so far: 0.1 0.1
        RDConstants.matchPenalty=-0.2;

        RDConstants.reEvaluation = 5;

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
        RDConstants.maxGeneration = 100;
        RDConstants.maxTimeEval = 4000;
        RDConstants.hardTrim = false;
        //RDConstants.maxNodes = 7;
        RDConstants.maxBeads = 500;
        RDConstants.showBeads = false;
        RDConstants.useMedian = true; //use median score of reevaluations

        RDConstants.ceilingNodes = true;
        RDConstants.ceilingTemplates = true;
        RDConstants.useMaxTotalNodes = false;
        RDConstants.maxNodes = 6;
        RDConstants.maxTemplates = 13;

        RDConstants.weightDisableTemplate = 5;
        RDConstants.weightMutateParameter = 80;
        RDConstants.weightAddActivationWithGradients = 5;
        RDConstants.weightAddInhibitionWithGradients = 5;
        RDConstants.weightAddNodeWithGradients = 5;
        //RDConstants.cutOff = 10.0f;

        RDConstants.targetName = "TBioneat";

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
        EvolutionaryAlgorithm algorithm = new BioNEATBuilder().mutator(mutator).buildAlgorithm();

        Evolver evolver = new Evolver(RDConstants.populationSize, RDConstants.maxGeneration, RDLibrary.rdstart,
                fitnessFunction, new RDFitnessDisplayer(), algorithm);
        //evolver.setGUI(false);
        evolver.setExtraConfig(RDConstants.configsToString());
        evolver.evolve();
        System.out.println("Evolution completed.");
        if(!Evolver.hasGUI()) System.exit(0);
    }
}
