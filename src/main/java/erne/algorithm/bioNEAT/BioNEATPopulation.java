package erne.algorithm.bioNEAT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import erne.DummyFitnessFunction;
import erne.Evolver;
import erne.Individual;
import erne.Population;
import erne.PopulationInfo;
import erne.speciation.SpeciationSolver;
import erne.speciation.Species;
import reactionnetwork.Connection;
import reactionnetwork.Library;
import reactionnetwork.ReactionNetwork;

public class BioNEATPopulation extends Population {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3690565019600893994L;
	
	private SpeciationSolver speciationSolver; 
	
	/**
	 * Keeps track of NEAT innovations
	 */
	public static Map<String, Integer> innovationNumbers = new HashMap<String, Integer>();
	
	public BioNEATPopulation(int size, ReactionNetwork initNetwork) {
		super(size, initNetwork);
		
		//bionNEAT specific initialization
		
		for (Connection conn : initNetwork.connections) {
			innovationNumbers.put(conn.from.name + "->" + conn.to.name, innovationNumbers.size());
		}
		speciationSolver = new SpeciationSolver();
	}

	
	public ArrayList<Species[]> getSpeciesByGenerations() {
		return speciationSolver.speciesByGeneration;
	}
	
	@Override
	public PopulationInfo getPopulationInfo(int i) {
		return new PopulationInfo(populations.get(i), speciationSolver.speciesByGeneration.get(i));
	}
	
	@Override
	public Individual[] resetPopulation() throws InterruptedException, ExecutionException {
		Individual[] individuals = super.resetPopulation();
		speciationSolver.speciate(individuals);
		return individuals;
	}
	
	@Override
	public Individual[] evolve() throws InterruptedException, ExecutionException {
		Individual[] individuals = super.evolve();
		speciationSolver.speciate(individuals);
		return individuals;
	}
	
	@Override
	public Individual[] reproduction() {
		Species[] species = speciationSolver.speciesByGeneration.get(speciationSolver.speciesByGeneration.size() - 1);
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
	
	@Override
	public void checkRestart(){
		speciationSolver.checkRestart();
	}
	
	
	

	public static void main(String[] args){
		BioNEATPopulation pop = new BioNEATPopulation(1,Library.startingMath);
		int fakeGens = 10;
		try {
			pop.mutator = Evolver.DEFAULT_MUTATOR;
			pop.setFitnessFunction(new DummyFitnessFunction());
			pop.resetPopulation();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i< fakeGens; i++){
			System.out.println(pop);
			pop.reproduction();
		}
		
		System.exit(0);
	}
	
}
