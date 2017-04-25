package use.math.gaussian;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import erne.algorithm.nsgaII.NSGAIIBuilder;
import erne.algorithm.nsgaII.NSGAIIPopulationFactory;
import reactionnetwork.Library;

public class RunGaussianNSGAII {

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException{
		

			
		GaussianFitnessFunction fitnessFunction = new GaussianFitnessFunction();
		erne.Constants.maxEvalTime = 3000; //Actually the current default
		erne.Constants.maxEvalClockTime = -1;
		NSGAIIPopulationFactory popFactory = new NSGAIIPopulationFactory();
		popFactory.superElitist = true;
		Evolver evolver = new Evolver(Evolver.DEFAULT_POP_SIZE,Evolver.MAX_GENERATIONS,
				Library.startingMath, fitnessFunction, new GaussianFitnessDisplayer(),
				new NSGAIIBuilder().populationFactory(popFactory).buildAlgorithm());
		evolver.setGUI(true);
		evolver.evolve();
		System.out.println("Evolution completed.");
		        //System.exit(0);
			}
	
}
