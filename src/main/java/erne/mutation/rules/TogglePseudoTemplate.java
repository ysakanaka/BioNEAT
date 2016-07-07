package erne.mutation.rules;

import erne.Individual;
import erne.mutation.MutationRule;
import reactionnetwork.Node;

public class TogglePseudoTemplate extends MutationRule {
	
	public double probGeneMutation = 0.3;

	public TogglePseudoTemplate(int weight) {
		super(weight);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Individual mutate(Individual indiv) {
		for (Node node : indiv.getNetwork().nodes) {
			if (node.type == Node.SIMPLE_SEQUENCE && !node.protectedSequence) { //Elongating protected sequences would be a mess...
				if (rand.nextDouble() < probGeneMutation) {
					node.hasPseudoTemplate = !node.hasPseudoTemplate;
				}
			}
		}
		return indiv;
	}

}
