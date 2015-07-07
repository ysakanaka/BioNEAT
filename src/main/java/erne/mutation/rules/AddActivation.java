package erne.mutation.rules;

import java.util.ArrayList;

import reactionnetwork.Connection;
import reactionnetwork.Node;
import edu.uci.ics.jung.graph.util.Pair;
import erne.Individual;
import erne.mutation.MutationRule;
import erne.util.Randomizer;

public class AddActivation extends MutationRule {
	public double probGeneMutation = 0.8;

	public AddActivation(int weight) {
		super(weight);
	}

	@Override
	public Individual mutate(Individual indiv) {
		ArrayList<Pair<Node>> possibleActivations = new ArrayList<Pair<Node>>();
		for (Node from : indiv.getNetwork().nodes) {
			if (from.type != Node.INHIBITING_SEQUENCE) {
				for (Node to : indiv.getNetwork().nodes) {
					Connection connection = indiv.getNetwork().getConnectionByEnds(from, to);
					if (connection == null || !connection.enabled) {
						possibleActivations.add(new Pair<Node>(from, to));
					}
				}
			}
		}
		if (possibleActivations.size() > 0) {
			int index = rand.nextInt(possibleActivations.size());
			Pair<Node> nodes = possibleActivations.get(index);
			Connection connection = indiv.getNetwork().getConnectionByEnds(nodes.getFirst(), nodes.getSecond());
			if (connection == null) {
				indiv.addConnection(nodes.getFirst(), nodes.getSecond(),
						Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue));
			} else {
				connection.enabled = true;
			}
		}
		return indiv;
	}
}
