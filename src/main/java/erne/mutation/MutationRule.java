package erne.mutation;

import java.io.Serializable;
import java.util.Random;

import erne.Individual;

public abstract class MutationRule implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int weight;
	protected transient Random rand = new Random();

	public MutationRule(int weight) {
		this.weight = weight;
	}

	public abstract Individual mutate(Individual indiv);

	public int getWeight() {
		return this.weight;
	}
}
