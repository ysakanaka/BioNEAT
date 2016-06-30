package use.math.gaussian;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import reactionnetwork.Library;
import erne.Evolver;

public class RunGaussian {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		GaussianFitnessFunction fitnessFunction = new GaussianFitnessFunction();
        erne.Constants.maxEvalTime = 1000; //Actually the current default
        erne.Constants.maxEvalClockTime = 2000;
		Evolver evolver = new Evolver(Library.startingMath, fitnessFunction, new GaussianFitnessDisplayer());
		evolver.setGUI(true);
		evolver.evolve();
        System.out.println("Evolution completed.");
        //System.exit(0);
	}
}
