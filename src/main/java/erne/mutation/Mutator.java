package erne.mutation;

import java.util.ArrayList;
import java.util.Random;

import erne.Individual;

public class Mutator {
	private ArrayList<MutationRule> mutationRules;
	private Random rand = new Random();

	public Mutator(ArrayList<MutationRule> mutationRules) {
		this.mutationRules = mutationRules;
	}

	public Individual mutate(Individual indiv) {
		int totalMutationWeight = 0;
		for (MutationRule mutationRule : mutationRules) {
			totalMutationWeight += mutationRule.getWeight();
		}
		int randomizedInt = rand.nextInt(totalMutationWeight);
		for (MutationRule mutationRule : mutationRules) {
			if (mutationRule.getWeight() > randomizedInt) {
				try {
					return mutationRule.mutate(indiv);
				} catch (Exception e) {
					e.printStackTrace();
					return indiv;
				}
			} else {
				randomizedInt -= mutationRule.getWeight();
			}
		}
		return indiv;
	}
}
