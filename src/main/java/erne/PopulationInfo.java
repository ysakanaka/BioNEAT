package erne;

import erne.speciation.Species;

public class PopulationInfo {
	private Individual[] individuals;
	private Species[] species;

	public void setIndividuals(Individual[] individuals) {
		this.individuals = individuals;
	}

	public Individual[] getIndividuals() {
		return individuals;
	}

	public void setSpecies(Species[] species) {
		this.species = species;
	}

	public Species[] getSpecies() {
		return species;
	}

	public Individual getBestIndividual() {
		Individual best = individuals[0];
		for (int i = 1; i < individuals.length; i++) {
			if (best.getFitnessResult().getFitness() < individuals[i].getFitnessResult().getFitness()) {
				best = individuals[i];
			}
		}
		return best;
	}
}
