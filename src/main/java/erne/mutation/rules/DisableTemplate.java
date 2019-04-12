package erne.mutation.rules;

import reactionnetwork.Connection;
import erne.Individual;
import erne.mutation.MutationRule;

public class DisableTemplate extends MutationRule {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double probGeneMutation = 0.8;
	public double probConnectionDisabling = 0.1;

	public DisableTemplate(int weight) {
		super(weight);
	}

	@Override
	public Individual mutate(Individual indiv) {
		for (Connection conn : indiv.getNetwork().connections) {
			if (conn.enabled && rand.nextDouble() < probGeneMutation) {
                if (rand.nextDouble() < Math.min(1, Math.max(0, probConnectionDisabling - Math.log10(conn.parameter / 10.0)))
						&& indiv.getNetwork().getNEnabledConnections() > 1) {
					conn.enabled = false;
					
					
				}
			}
		}
		return indiv;
	}

	@Override
	public boolean isApplicable(Individual indiv) {
		for (Connection conn : indiv.getNetwork().connections) {
			if (conn.enabled) return true;
		}
		return false;
	}
}
