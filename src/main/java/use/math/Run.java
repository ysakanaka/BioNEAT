package use.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import reactionnetwork.Library;
import erne.Population;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.rules.AddNode;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;

public class Run {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		Population population = new Population(100, Library.startingMath);
		population.setFitnessFunction(new SquareFitnessFunction());
		population.setMutator(new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] { new DisableTemplate(1),
				new MutateParameter(1), new AddNode(1) }))));
		population.resetPopulation();
		for (int i = 0; i < 100; i++) {
			population.evolve();
			System.out.println("Best fitness = " + population.getBestIndividual().getFitnessResult().getFitness());
			System.out.println(population.getBestIndividual());
		}
	}

}
