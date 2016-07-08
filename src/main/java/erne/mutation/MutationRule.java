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
	
	public abstract boolean isApplicable(Individual indiv);

	/**
	 * Return the weight given to the mutation for a given individual.
	 * The most common need is to give a weight of 0 to mutations that are not applicable
	 * @param indiv
	 * @return
	 */
	public int getWeight(Individual indiv) {
		if(!isApplicable(indiv)) return 0;
		
		return this.weight;
	}
}
