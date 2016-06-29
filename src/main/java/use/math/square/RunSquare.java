package use.math.square;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import reactionnetwork.Library;
import erne.Evolver;

public class RunSquare {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		SquareFitnessFunction fitnessFunction = new SquareFitnessFunction();
		
		Evolver evolver = new Evolver(Library.startingMath, fitnessFunction, new SquareFitnessDisplayer());
		evolver.evolve();
	}
}
