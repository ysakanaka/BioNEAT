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
	public double speciationThreshold = Constants.defaultSpeciationThreshold;
	
	public static double speciesPopIncreaseCappingRatio = 0.1;

	public Species[] speciate(Individual[] nextGen) {
		ArrayList<Species> nextGenSpecies = decideSpecies(nextGen);
		decideSpeciesPopulation(nextGenSpecies, nextGen.length);

		// modify the speciationThreshold
		if (Constants.autoSpeciationThreshold) {
			int nSpecies = nextGenSpecies.size();
			if (nSpecies < Constants.targetNSpecies) {
				speciationThreshold -= Constants.speciationThresholdMod;
			} else if (nSpecies > Constants.targetNSpecies) {
				speciationThreshold += Constants.speciationThresholdMod;
			}
			speciationThreshold = Math.max(speciationThreshold, Constants.speciationThresholdMin);
		}

		Species[] nextGenSpeciesArray = nextGenSpecies.toArray(new Species[nextGenSpecies.size()]);
		speciesByGeneration.add(nextGenSpeciesArray);
		return nextGenSpeciesArray;
	}
	
	

	private ArrayList<Species> decideSpeciesPopulation(ArrayList<Species> nextGenSpecies, int popSize) {
		// calculate next gen population for each species
		ArrayList<Species> processedSpecies = new ArrayList<Species>();
		int popSizeLeft = popSize;

		boolean capping;
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
					int nextGenPop = (int) (sp.getSpeciesFitness() * ((double)popSizeLeft) / sumFitness); //Processed means we already capped the max for this species
					// capping (Meaning we cannot increase pop size too fast)
					System.out.println(speciesByGeneration.size());
					if (nextGenPop > (speciesByGeneration.size() == 0 ? 0 : sp.individuals.size()) + popSize *speciesPopIncreaseCappingRatio) {
						nextGenPop = (speciesByGeneration.size() == 0 ? 0 : sp.individuals.size()) + (int) (popSize *speciesPopIncreaseCappingRatio);
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
						if (compDistance(nextGen[i], bestIndiv) < speciationThreshold) {
							nextGen[i].speciesId = sp.representative.getId();
							curSpeciesFitness = bestIndiv.getFitnessResult().getFitness();
						}
					}
				}
				// compare with every individual in previous generation
				if (nextGen[i].speciesId == -1) {
					for (Species sp : speciesByGeneration.get(speciesByGeneration.size() - 1)) {
						for (Individual indiv1 : sp.individuals) {
							if (compDistance(nextGen[i], indiv1) < speciationThreshold) {
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
					if (compDistance(nextGen[i], indiv1) < speciationThreshold) {
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
	
	/**
	 * In case of restart, check that the speciation parameter is correct
	 */
	public void checkRestart(){
		for(int i = 0; i<speciesByGeneration.size(); i++){ //we replay the match
			if (Constants.autoSpeciationThreshold) {
				int nSpecies = speciesByGeneration.get(i).length;
				if (nSpecies < Constants.targetNSpecies) {
					speciationThreshold -= Constants.speciationThresholdMod;
				} else if (nSpecies > Constants.targetNSpecies) {
					speciationThreshold += Constants.speciationThresholdMod;
				}
				speciationThreshold = Math.max(speciationThreshold, Constants.speciationThresholdMin);
			}
		}
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