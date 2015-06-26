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
}
