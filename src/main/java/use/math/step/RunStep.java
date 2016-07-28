package use.math.step;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import reactionnetwork.Library;

public class RunStep {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		StepFitnessFunction fitnessFunction = new StepFitnessFunction();
		
		StepFitnessFunction.hysteresis = false;
		StepFitnessFunction.diff = false;
		
		if(StepFitnessFunction.hysteresis) fitnessFunction.nTests *= 2;
		
		Evolver evolver = new Evolver(Library.startingMath, fitnessFunction, new StepFitnessDisplayer());
		evolver.evolve();
	}
	
}
