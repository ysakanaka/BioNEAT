package erne;

import reactionnetwork.ReactionNetwork;
import erne.speciation.Species;

public class PopulationInfo {
	private Individual[] individuals;
	private Species[] species;

	public PopulationInfo(Individual[] individuals, Species[] species) {
		this.individuals = individuals;
		this.species = species;
	}

	public void setSpecies(Species[] species) {
		this.species = species;
	}

	public Species[] getSpecies() {
		return species;
	}

	public AbstractFitnessResult getBestFitness() {
		Individual best = individuals[0];
		for (int i = 1; i < individuals.length; i++) {
			if (best.getFitnessResult().getFitness() < individuals[i].getFitnessResult().getFitness()) {
				best = individuals[i];
			}
		}
		return best.getFitnessResult();
	}

	public ReactionNetwork getBestNetwork() {
		Individual best = individuals[0];
		for (int i = 1; i < individuals.length; i++) {
			if (best.getFitnessResult().getFitness() < individuals[i].getFitnessResult().getFitness()) {
				best = individuals[i];
			}
		}
		return best.getNetwork();
	}
}
