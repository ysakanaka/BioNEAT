package erne.mutation.rules;

import reactionnetwork.Connection;
import reactionnetwork.Node;
import erne.Individual;
import erne.mutation.MutationRule;

public class MutateParameter extends MutationRule {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double probGeneMutation = 0.8;

	public MutateParameter(int weight) {
		super(weight);
	}

	@Override
	public Individual mutate(Individual indiv) {
		for (Node node : indiv.getNetwork().nodes) {
			if (node.type == Node.SIMPLE_SEQUENCE) {
				if (rand.nextDouble() < probGeneMutation) {
					node.parameter = mutateParam(node.parameter, Individual.minNodeValue, Individual.maxNodeValue);
				}
				if (node.hasPseudoTemplate && rand.nextDouble() < probGeneMutation) {
					node.pseudoTemplateConcentration = mutateParam(node.parameter, Individual.minTemplateValue, Individual.maxTemplateValue);
				}
			}
		}
		for (Connection conn : indiv.getNetwork().connections) {
			if (conn.enabled && rand.nextDouble() < probGeneMutation) {
				conn.parameter = mutateParam(conn.parameter, Individual.minTemplateValue, Individual.maxTemplateValue);
			}
		}
		return indiv;
	}

	private double mutateParam(double oldParam, double min, double max) {
		double randGaussian = rand.nextGaussian();
		double mutatedParam = Math.exp(Math.log(oldParam) * (1 + randGaussian * 0.2)) + randGaussian * 2;
		if (mutatedParam < min)
			return min;
		if (mutatedParam > max)
			return max;
		return mutatedParam;
	}

	@Override
	public boolean isApplicable(Individual indiv) {
		// Always possible
		return true;
	}

}
