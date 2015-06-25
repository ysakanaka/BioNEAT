package erne.mutation;

import java.util.Random;

import erne.Individual;

public abstract class MutationRule {
	private int weight;
	protected Random rand = new Random();

	public MutationRule(int weight) {
		this.weight = weight;
	}

	public abstract Individual mutate(Individual indiv);

	public int getWeight() {
		return this.weight;
	}
}
