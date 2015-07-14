package use.math.gaussian;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import reactionnetwork.Library;
import erne.Evolver;

public class RunGaussian {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		GaussianFitnessFunction fitnessFunction = new GaussianFitnessFunction();

		Evolver evolver = new Evolver(Library.startingMath, fitnessFunction, new GaussianFitnessDisplayer());
		evolver.evolve();
	}
}
