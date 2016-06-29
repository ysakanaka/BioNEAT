package use.ready.mutation.rules;

import java.util.ArrayList;
import edu.uci.ics.jung.graph.util.Pair;
import erne.Individual;
import erne.mutation.MutationRule;
import erne.util.Randomizer;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import use.ready.ReadyReactionNetwork;

public class AddBeadActivation extends MutationRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7472674762251709403L;
	public double probGeneMutation = 0.8;
	
	public AddBeadActivation(int weight) {
		super(weight);
		// TODO Auto-generated constructor stub
	}


	@Override
	public Individual mutate(Individual indiv) {
		ReactionNetwork rn = indiv.getNetwork();
		ArrayList<Pair<Node>> possibleActivations = new ArrayList<Pair<Node>>();
		for (Node from : rn.nodes) {
			if (from.type != Node.INHIBITING_SEQUENCE) {
				for (Node to : rn.nodes) {
					Connection connection = rn.getConnectionByEnds(from, to);
					if (connection == null || !connection.enabled) {
						possibleActivations.add(new Pair<Node>(from, to));
					}
				}
			}
		}
		if (possibleActivations.size() > 0) {
			int index = rand.nextInt(possibleActivations.size());
			Pair<Node> nodes = possibleActivations.get(index);
			Connection connection = rn.getConnectionByEnds(nodes.getFirst(), nodes.getSecond());
			if (connection == null) {
				indiv.addConnection(nodes.getFirst(), nodes.getSecond(),
						Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue));
				if(ReadyReactionNetwork.class.isAssignableFrom(rn.getClass())){
					((ReadyReactionNetwork) rn).addTemplateOnBead(connection, rand.nextInt(((ReadyReactionNetwork) rn).templateOnBeads.size()));
				}
			} else {
				connection.enabled = true;
			}
		}
		return indiv;
	}

}
