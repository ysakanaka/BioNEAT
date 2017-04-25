package use.ready.polishFlag;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import erne.algorithm.EvolutionaryAlgorithm;
import erne.algorithm.bioNEAT.BioNEATBuilder;
import use.ready.AbstractReadyFitnessFunction;
import use.ready.export.ReadyExporter;
import use.ready.fakefitness.RunReady;

public class RunPolishFlag {
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		AbstractReadyFitnessFunction.simulationPath = "/home/naubertkato/Documents/Simulation/TestCode/";
		AbstractReadyFitnessFunction.simulationName = "polishFlag";
		PolishFlagFitness fitness = new PolishFlagFitness("b");
		ReadyExporter.maxSteps = 100000;
		ReadyExporter.interval = 100000;
		
		EvolutionaryAlgorithm algorithm = new BioNEATBuilder().mutator(RunReady.mutator).buildAlgorithm();
		
		Evolver evolver = new Evolver(Evolver.DEFAULT_POP_SIZE, Evolver.MAX_GENERATIONS,RunReady.startingReady, fitness, Evolver.DEFAULT_FITNESS_DISPLAYER,algorithm);
		evolver.evolve();
        System.out.println("Evolution completed.");
        System.exit(0);
	}


}
