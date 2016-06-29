package use.math.gaussian;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import reactionnetwork.Library;
import erne.Evolver;

public class RunGaussianNoGUI {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		GaussianFitnessFunction fitnessFunction = new GaussianFitnessFunction();
        erne.Constants.maxEvalTime = 4000; //Actually the current default
		Evolver evolver = new Evolver(Library.startingMath, fitnessFunction, new GaussianFitnessDisplayer());
		evolver.setGUI(false);
		evolver.evolve();
        System.out.println("Evolution completed.");
        System.exit(0);
	}
}
