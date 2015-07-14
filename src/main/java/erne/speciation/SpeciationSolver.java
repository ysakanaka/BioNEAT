package erne.speciation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import erne.Constants;
import erne.Individual;
import reactionnetwork.Connection;

public class SpeciationSolver implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<Species[]> speciesByGeneration = new ArrayList<Species[]>();
	public ArrayList<Individual> speciesLib = new ArrayList<Individual>();

	public Species[] speciate(Individual[] nextGen) {
		ArrayList<Species> nextGenSpecies = decideSpecies(nextGen);
		decideSpeciesPopulation(nextGenSpecies, nextGen.length);

		// modify the speciationThreshold
		if (Constants.autoSpeciationThreshold) {
			int nSpecies = nextGenSpecies.size();
			if (nSpecies < Constants.targetNSpecies) {
				Constants.speciationThreshold -= Constants.speciationThresholdMod;
			} else if (nSpecies > Constants.targetNSpecies) {
				Constants.speciationThreshold += Constants.speciationThresholdMod;
			}
			Constants.speciationThreshold = Math.max(Constants.speciationThreshold, Constants.speciationThresholdMin);
		}

		Species[] nextGenSpeciesArray = nextGenSpecies.toArray(new Species[nextGenSpecies.size()]);
		speciesByGeneration.add(nextGenSpeciesArray);
		return nextGenSpeciesArray;
	}

	private ArrayList<Species> decideSpeciesPopulation(ArrayList<Species> nextGenSpecies, int popSize) {
		// calculate next gen population for each species
		ArrayList<Species> processedSpecies = new ArrayList<Species>();
		int popSizeLeft = popSize;

		boolean capping = false;
		Map<Species, Integer> tempNextGenPop = new HashMap<Species, Integer>();
		do {
			capping = false;
			double sumFitness = 0;
			for (Species sp : nextGenSpecies) {
				if (!processedSpecies.contains(sp)) {
					sumFitness += sp.getSpeciesFitness();
				}
			}
			for (Species sp : nextGenSpecies) {
				if (!processedSpecies.contains(sp)) {
					int nextGenPop = (int) (sp.getSpeciesFitness() * popSizeLeft / sumFitness);
					// capping
					System.out.println(speciesByGeneration.size());
					if (nextGenPop > (speciesByGeneration.size() == 0 ? 0 : sp.individuals.size()) + popSize / 10) {
						nextGenPop = (speciesByGeneration.size() == 0 ? 0 : sp.individuals.size()) + popSize / 10;
						capping = true;
						processedSpecies.add(sp);
						popSizeLeft -= nextGenPop;
						sp.setNextGenPopulation(nextGenPop);
					} else {
						tempNextGenPop.put(sp, nextGenPop);
					}
				}
			}
		} while (capping);

		for (Species sp : tempNextGenPop.keySet()) {
			if (!processedSpecies.contains(sp)) {
				int nextGenPop = tempNextGenPop.get(sp);
				popSizeLeft -= nextGenPop;
				sp.setNextGenPopulation(nextGenPop);
			}

		}

		// if there is still spaces left, randomly assign them to each species
		Random rand = new Random();
		for (int i = 0; i < popSizeLeft; i++) {
			int index = rand.nextInt(nextGenSpecies.size());
			nextGenSpecies.get(index).setNextGenPopulation(nextGenSpecies.get(index).getNextGenPopulation() + 1);
		}
		return nextGenSpecies;
	}

	private ArrayList<Species> decideSpecies(Individual[] nextGen) {
		for (int i = 0; i < nextGen.length; i++) {
			double curSpeciesFitness = 0;
			if (speciesByGeneration.size() > 0) {
				for (Species sp : speciesByGeneration.get(speciesByGeneration.size() - 1)) {
					Individual bestIndiv = sp.getBestIndividual();
					if (curSpeciesFitness < bestIndiv.getFitnessResult().getFitness()) {
						if (compDistance(nextGen[i], bestIndiv) < Constants.speciationThreshold) {
							nextGen[i].speciesId = sp.representative.getId();
							curSpeciesFitness = bestIndiv.getFitnessResult().getFitness();
						}
					}
				}
				// compare with every individual in previous generation
				if (nextGen[i].speciesId == -1) {
					for (Species sp : speciesByGeneration.get(speciesByGeneration.size() - 1)) {
						for (Individual indiv1 : sp.individuals) {
							if (compDistance(nextGen[i], indiv1) < Constants.speciationThreshold) {
								nextGen[i].speciesId = sp.representative.getId();
								break;
							}
						}
					}
				}

			}

			// compare with history species
			if (nextGen[i].speciesId == -1) {
				for (Individual indiv1 : speciesLib) {
					if (compDistance(nextGen[i], indiv1) < Constants.speciationThreshold) {
						nextGen[i].speciesId = indiv1.getId();
						break;
					}
				}
			}

			// make new species
			if (nextGen[i].speciesId == -1) {
				speciesLib.add(nextGen[i]);
				nextGen[i].speciesId = nextGen[i].getId();
			}

		}

		ArrayList<Species> nextGenSpecies = new ArrayList<Species>();
		for (Individual species : speciesLib) {
			Species sp = new Species(species);
			for (int i = 0; i < nextGen.length; i++) {
				if (nextGen[i].speciesId == sp.representative.getId()) {
					sp.individuals.add(nextGen[i]);
				}
			}
			if (sp.individuals.size() > 0) {
				nextGenSpecies.add(sp);
			}
		}
		return nextGenSpecies;
	}

	public double compDistance(Individual indiv1, Individual indiv2) {
		int N = Math.max(indiv1.getNetwork().connections.size(), indiv2.getNetwork().connections.size());
		int nMatches = 0;
		for (Connection conn : indiv1.getNetwork().connections) {
			if (indiv2.getNetwork().getConnectionByIN(conn.innovation) != null) {
				nMatches++;
			}
		}
		return (double) (N - nMatches) / N;
	}
}