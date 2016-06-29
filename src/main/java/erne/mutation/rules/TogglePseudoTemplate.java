package erne.mutation.rules;

import erne.Individual;
import erne.mutation.MutationRule;

public class TogglePseudoTemplate extends MutationRule {

	public TogglePseudoTemplate(int weight) {
		super(weight);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Individual mutate(Individual indiv) {
		//TODO Toggle pseudotemplates
		return indiv;
	}

}
