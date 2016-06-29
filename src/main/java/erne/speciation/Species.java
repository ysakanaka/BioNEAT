package erne.speciation;

import java.io.Serializable;
import java.util.ArrayList;

import erne.Individual;

public class Species implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Individual representative;
	public ArrayList<Individual> individuals;
	private int nextGenPopulation = 0;

	public Species(Individual representative) {
		this.representative = representative;
		this.individuals = new ArrayList<Individual>();
	}

	public int getPopulation() {
		return individuals.size();
	}

	public Individual getBestIndividual() {
		Individual bestIndividual = individuals.get(0);
		for (Individual indiv : individuals) {
			if (indiv.getFitnessResult() != null && indiv.getFitnessResult().getFitness() > bestIndividual.getFitnessResult().getFitness()) {
				bestIndividual = indiv;
			}
		}
		return bestIndividual;
	}

	public String getName() {
		return String.valueOf(this.representative.getId());
	}

	@Override
	public String toString() {
		String result = "Species representative: " + getName() + "\n";
		result += "Population: " + this.individuals.size() + "\n";
		result += "Species fitness: " + this.getSpeciesFitness() + "\n";
		result += "Fitness's Standard deviation: " + this.getStandardDeviation() + "\n";
		result += "Next gen population: " + this.getNextGenPopulation() + "\n";
		return result;
	}

	private double getStandardDeviation() {
		double result = 0;
		double speciesFitness = getSpeciesFitness();
		for (Individual indiv : individuals) {
			result += (indiv.getFitnessResult().getFitness() - speciesFitness) * (indiv.getFitnessResult().getFitness() - speciesFitness);
		}
		return result / individuals.size();
	}

	public double getSpeciesFitness() {
		double result = 0;
		for (Individual indiv : individuals) {
			result += indiv.getFitnessResult().getFitness();
		}
		return result / individuals.size();
	}

	public int getNextGenPopulation() {
		return nextGenPopulation;
	}

	public void setNextGenPopulation(int nextGenPopulation) {
		this.nextGenPopulation = nextGenPopulation;
	}
}
