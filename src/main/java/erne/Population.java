package erne;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import cluster.Cluster;
import erne.mutation.Mutator;
import erne.speciation.SpeciationSolver;
import erne.speciation.Species;
import reactionnetwork.ReactionNetwork;

public class Population {
	public static AtomicInteger nextIndivId = new AtomicInteger();
	public static AtomicInteger nextNodeName = new AtomicInteger((int) 'b');
	public static Map<String, Integer> innovationNumbers = new HashMap<String, Integer>();
	public static Map<Integer, Individual> allIndividuals = new HashMap<Integer, Individual>();
	public static Map<String, String> nodeNameOrigins = new HashMap<String, String>();

	private Individual initIndividual;
	private Individual[] individuals;
	private AbstractFitnessFunction fitnessFunction;
	private SpeciationSolver speciationSolver;

	private Random rand = new Random();

	private Mutator mutator;

	public Population(int size, ReactionNetwork initNetwork) {
		this.individuals = new Individual[size];
		this.initIndividual = new Individual(initNetwork);
		speciationSolver = new SpeciationSolver();
	}

	public void setFitnessFunction(AbstractFitnessFunction fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}

	public void setMutator(Mutator mutator) {
		this.mutator = mutator;
	}

	public Individual[] resetPopulation() {
		for (int i = 0; i < individuals.length; i++) {
			individuals[i] = initIndividual.clone();
			mutator.mutate(individuals[i]);
			individuals[i].parentIds.add(initIndividual.getId());
		}
		return individuals;
	}

	public Individual[] evolve() throws InterruptedException, ExecutionException {
		evaluateFitness();
		Species[] species = speciationSolver.speciate(individuals);
		reproduction(species);
		return individuals;
	}

	public Individual getBestIndividual() {
		Individual bestIndividual = individuals[0];
		for (Individual individual : individuals) {
			if (bestIndividual.getFitnessResult().getFitness() < individual.getFitnessResult().getFitness()) {
				bestIndividual = individual;
			}
		}
		return bestIndividual;
	}

	public Individual[] reproduction(Species[] species) {
		int i = 0;
		for (Species sp : species) {
			int nextGenPop = sp.getNextGenPopulation();
			if (sp.getNextGenPopulation() > 0) {
				if (i >= individuals.length)
					break;
				individuals[i] = sp.getBestIndividual().clone();
				i++;
				nextGenPop--;
				if (i >= individuals.length)
					break;
			}
			for (int j = 0; j < nextGenPop; j++) {
				Individual[] parents = selectCrossoverParents(sp.individuals);
				individuals[i] = parents[0].clone();
				mutator.mutate(individuals[i]);
				individuals[i].parentIds.add(parents[0].getId());
				i++;
				if (i >= individuals.length)
					break;
			}
		}

		// fill the rest with mutation only
		while (i < individuals.length) {
			Species randomSpecies = species[rand.nextInt(species.length)];
			Individual parent = randomSpecies.individuals.get(rand.nextInt(randomSpecies.individuals.size()));
			individuals[i] = parent.clone();
			mutator.mutate(individuals[i]);
			individuals[i].parentIds.add(parent.getId());
			i++;
			if (i >= individuals.length)
				break;
		}
		return individuals;
	}

	private Individual[] selectCrossoverParents(ArrayList<Individual> curGen) {
		double maxFitness = -Double.MAX_VALUE;
		Individual parent1 = null;
		Individual parent2 = null;
		if (curGen.size() == 1) {
			return new Individual[] { curGen.get(0), curGen.get(0) };
		}

		for (int k = 0; k < Constants.tournamentSize; k++) {
			int winnerIndiv = this.rand.nextInt(curGen.size());
			Individual i = curGen.get(winnerIndiv);
			if (i.getFitnessResult().getFitness() > maxFitness) {
				parent1 = i;
				maxFitness = i.getFitnessResult().getFitness();
			}
		}
		maxFitness = -Double.MAX_VALUE;

		for (int k = 0; k < Constants.tournamentSize; k++) {
			Individual i = null;
			do {
				int winnerIndiv = this.rand.nextInt(curGen.size());
				i = curGen.get(winnerIndiv);
			} while (i == parent1);

			if (i.getFitnessResult().getFitness() > maxFitness) {
				parent2 = i;
				maxFitness = i.getFitnessResult().getFitness();
			}
		}

		return new Individual[] { parent1, parent2 };
	}

	private void evaluateFitness() throws InterruptedException, ExecutionException {
		System.out.println("Evaluating fitness");
		List<ReactionNetwork> networks = new LinkedList<ReactionNetwork>();
		for (int i = 0; i < individuals.length; i++) {
			networks.add(individuals[i].getNetwork());
		}
		Map<ReactionNetwork, AbstractFitnessResult> fitnesses = Cluster.evaluateFitness(fitnessFunction, networks);
		for (int i = 0; i < individuals.length; i++) {
			individuals[i].setFitnessResult(fitnesses.get(individuals[i].getNetwork()));
			System.out.println("Indiv " + i + " Fitness: " + individuals[i].getFitnessResult());
		}
	}
}
