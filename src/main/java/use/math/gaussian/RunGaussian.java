package use.math.gaussian;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import reactionnetwork.Library;
import erne.Evolver;

public class RunGaussian {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		GaussianFitnessFunction fitnessFunction = new GaussianFitnessFunction();
        erne.Constants.maxEvalTime = 3000; //Actually the current default
		Evolver evolver = new Evolver(Library.startingMath, fitnessFunction, new GaussianFitnessDisplayer());
		evolver.evolve();
        System.out.println("Evolution completed.");
        System.exit(0); //Somehow, we still have some remnants of GUI somewhere which prevent closing.
	}
}
