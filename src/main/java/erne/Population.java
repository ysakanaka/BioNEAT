package erne;

import java.io.Serializable;
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
import reactionnetwork.Connection;
import reactionnetwork.ReactionNetwork;

public class Population implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static AtomicInteger nextIndivId = new AtomicInteger();
	public static AtomicInteger nextNodeName = new AtomicInteger((int) 'b');
	public static Map<String, Integer> innovationNumbers = new HashMap<String, Integer>();
	public static Map<String, String> nodeNameOrigins = new HashMap<String, String>();

	private Individual initIndividual;
	private ArrayList<Individual[]> populations = new ArrayList<Individual[]>();
	private AbstractFitnessFunction fitnessFunction;
	private SpeciationSolver speciationSolver;

	private Random rand = new Random();

	private Mutator mutator;

	public Population(int size, ReactionNetwork initNetwork) {
		for (Connection conn : initNetwork.connections) {
			innovationNumbers.put(conn.from.name + "->" + conn.to.name, innovationNumbers.size());
		}
		this.populations.add(new Individual[size]);
		this.initIndividual = new Individual(initNetwork);
		speciationSolver = new SpeciationSolver();
	}

	public int getTotalGeneration() {
		return populations.size();
	}

	public ArrayList<Species[]> getSpeciesByGenerations() {
		return speciationSolver.speciesByGeneration;
	}

	public PopulationInfo getPopulationInfo(int i) {
		return new PopulationInfo(populations.get(i), speciationSolver.speciesByGeneration.get(i));
	}

	public void setFitnessFunction(AbstractFitnessFunction fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}

	public void setMutator(Mutator mutator) {
		this.mutator = mutator;
	}

	public Individual[] resetPopulation() throws InterruptedException, ExecutionException {
		Individual[] individuals = populations.get(populations.size() - 1);
		for (int i = 0; i < individuals.length; i++) {
			individuals[i] = initIndividual.clone();
			mutator.mutate(individuals[i]);
			individuals[i].parentIds.add(initIndividual.getId());
		}
		evaluateFitness();
		speciationSolver.speciate(individuals);
		return individuals;
	}

	public Individual[] evolve() throws InterruptedException, ExecutionException {
		reproduction(speciationSolver.speciesByGeneration.get(speciationSolver.speciesByGeneration.size() - 1));
		evaluateFitness();
		Individual[] individuals = populations.get(populations.size() - 1);
		speciationSolver.speciate(individuals);
		return individuals;
	}

	public Individual[] reproduction(Species[] species) {
		Individual[] individuals = populations.get(populations.size() - 1);
		Individual[] nextGeneIndividuals = new Individual[individuals.length];
		int i = 0;
		for (Species sp : species) {
			int nextGenPop = sp.getNextGenPopulation();
			if (sp.getNextGenPopulation() > 0) {
				if (i >= nextGeneIndividuals.length)
					break;
				nextGeneIndividuals[i] = sp.getBestIndividual().clone();
				i++;
				nextGenPop--;
				if (i >= nextGeneIndividuals.length)
					break;
			}
			for (int j = 0; j < nextGenPop; j++) {
				Individual[] parents = selectCrossoverParents(sp.individuals);
				nextGeneIndividuals[i] = parents[0].clone();
				mutator.mutate(nextGeneIndividuals[i]);
				nextGeneIndividuals[i].parentIds.add(parents[0].getId());
				i++;
				if (i >= nextGeneIndividuals.length)
					break;
			}
		}

		// fill the rest with mutation only
		while (i < nextGeneIndividuals.length) {
			Species randomSpecies = species[rand.nextInt(species.length)];
			Individual parent = randomSpecies.individuals.get(rand.nextInt(randomSpecies.individuals.size()));
			nextGeneIndividuals[i] = parent.clone();
			mutator.mutate(nextGeneIndividuals[i]);
			nextGeneIndividuals[i].parentIds.add(parent.getId());
			i++;
			if (i >= nextGeneIndividuals.length)
				break;
		}
		populations.add(nextGeneIndividuals);
		return nextGeneIndividuals;
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
		Individual[] individuals = populations.get(populations.size() - 1);
		System.out.println("Evaluating fitness");
		List<ReactionNetwork> networks = new LinkedList<ReactionNetwork>();
		for (int i = 0; i < individuals.length; i++) {
			networks.add(individuals[i].getNetwork());
		}
		Map<ReactionNetwork, AbstractFitnessResult> fitnesses = Cluster.evaluateFitness(fitnessFunction, networks);
		for (int i = 0; i < individuals.length; i++) {
			individuals[i].setFitnessResult(fitnesses.get(individuals[i].getNetwork()));
			// System.out.println("Indiv " + i + " Fitness: " +
			// individuals[i].getFitnessResult());
		}
	}
}
