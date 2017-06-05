package use.processing.targets.centerlinetests;

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

public class RDLineNicolasWithParameterScan {
	
	public static void setParams(String paramName, int value){
		RDConstants.weightDisableTemplate = 1;
		RDConstants.weightAddActivationWithGradients = 1;
		RDConstants.weightAddInhibitionWithGradients = 1;
		RDConstants.weightAddNodeWithGradients = 1;
		switch(paramName){
		case  "disTemplate":
			RDConstants.weightDisableTemplate = value;
			break;
		case "addAct":
			RDConstants.weightAddActivationWithGradients = value;
			break;
		case "addInh":
			RDConstants.weightAddInhibitionWithGradients = value;
			break;
		case "addNode":
			RDConstants.weightAddNodeWithGradients = value;
			break;
		default:
			System.out.println("Unknown parameter name");
		}
		int subTotal = RDConstants.weightDisableTemplate + RDConstants.weightAddActivationWithGradients +
		    RDConstants.weightAddInhibitionWithGradients +RDConstants.weightAddNodeWithGradients;
		RDConstants.weightMutateParameter = 100 - subTotal; 
	}
	
	public static void main(String[] args)
			throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		RDPatternFitnessResultIbuki.width = 0.2;
		boolean debug = false;
		boolean[][] target = RDPatternFitnessResultIbuki.getCenterLine();
		RDPatternFitnessResultIbuki.weightExponential = 0.1; // good candidate
																// so far: 0.1
																// 0.1
		RDConstants.matchPenalty = -0.06;

		RDConstants.reEvaluation = 2;

		int[] params = { 0, 1, 5, 10, 50 };
		String[] paramTargets = { "disTemplate", "addAct", "addInh", "addNode" };

		// RDBeadPositionFitnessFunction fitnessFunction = new
		// RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
		RDConstants.evalRandomDistance = false;
		double ratio = 0.0;
		boolean[][] fullMap = new boolean[target.length][target[0].length];
		for (int i = (int) ratio*fullMap.length; i <  (1.0-ratio)* (double)fullMap.length; i++) {
			for (int j = 0; j <fullMap[0].length; j++) {
				fullMap[i][j] = true;
			}
		}
		RDConstants.defaultRandomFitness = Math.max(0.0,
				RDPatternFitnessResultIbuki.distanceNicolasExponential(target, fullMap));
		System.out.println("Default fitness: " + RDConstants.defaultRandomFitness);
		if (debug) return;
		RDConstants.populationSize = 50;
		RDConstants.maxGeneration = 200;//200;
		RDConstants.maxTimeEval = 4000;//4000;
		RDConstants.hardTrim = false;
		RDConstants.maxNodes = 16;
		RDConstants.maxBeads = 500;
		RDConstants.showBeads = false;
		RDConstants.useMedian = true; // use median score of reevaluations
		for (int toScan = 0; toScan < paramTargets.length; toScan++) {
			for (int i = 0; i < params.length; i++) {
				setParams(paramTargets[toScan],params[i]);
				// RDConstants.cutOff = 10.0f;

				RDConstants.targetName = "ClusterLineParamScan";

				// RDConstants.showBeads = true;
				// RDBeadPositionFitnessFunction fitnessFunction = new
				// RDBeadPositionFitnessFunction(new BeadLineTarget(offset),
				// target);
				RDFitnessFunctionIbuki fitnessFunction = new RDFitnessFunctionIbuki(target);
				

				Mutator mutator;

				if (RDConstants.hardTrim) {
					mutator = new PruningMutator(new ArrayList<MutationRule>(
							Arrays.asList(new MutationRule[] { new DisableTemplate(RDConstants.weightDisableTemplate),
									new MutateParameter(RDConstants.weightMutateParameter),
									new AddNodeWithGradients(RDConstants.weightAddNodeWithGradients),
									new AddActivationWithGradients(RDConstants.weightAddActivationWithGradients),
									new AddInhibitionWithGradients(RDConstants.weightAddInhibitionWithGradients) })));
				} else {
					mutator = new Mutator(new ArrayList<MutationRule>(
							Arrays.asList(new MutationRule[] { new DisableTemplate(RDConstants.weightDisableTemplate),
									new MutateParameter(RDConstants.weightMutateParameter),
									new AddNodeWithGradients(RDConstants.weightAddNodeWithGradients),
									new AddActivationWithGradients(RDConstants.weightAddActivationWithGradients),
									new AddInhibitionWithGradients(RDConstants.weightAddInhibitionWithGradients) })));
				}
				EvolutionaryAlgorithm algorithm = new BioNEATBuilder().mutator(mutator).buildAlgorithm();

				Evolver evolver = new Evolver(RDConstants.populationSize, RDConstants.maxGeneration, RDLibrary.rdstart,
						fitnessFunction, new RDFitnessDisplayer(), algorithm);
				evolver.setGUI(false);
				evolver.setExtraConfig(RDConstants.configsToString());
				evolver.evolve();
				System.out.println("Evolution completed with param " + params[i]);
			}
		}
		if (!Evolver.hasGUI())
			System.exit(0);
	}
}
