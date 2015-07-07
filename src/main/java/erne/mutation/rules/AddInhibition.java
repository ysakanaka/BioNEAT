package erne.mutation.rules;

import java.util.ArrayList;

import reactionnetwork.Connection;
import reactionnetwork.Node;
import erne.Individual;
import erne.mutation.MutationRule;
import erne.util.Randomizer;

public class AddInhibition extends MutationRule {

	public AddInhibition(int weight) {
		super(weight);
	}

	@Override
	public Individual mutate(Individual indiv) {
		if (rand.nextBoolean()) {
			// add single inhibition
			ArrayList<Node> possibleNodes = new ArrayList<Node>();
			for (Node node : indiv.getNetwork().nodes) {
				if (node.type == Node.SIMPLE_SEQUENCE) {
					possibleNodes.add(node);
				}
			}
			if (possibleNodes.size() > 0) {
				Node nodeFrom = possibleNodes.get(rand.nextInt(possibleNodes.size()));
				Node nodeTo = possibleNodes.get(rand.nextInt(possibleNodes.size()));
				addInhibition(indiv, nodeFrom, nodeTo);
			}
			return indiv;
		} else {
			// add double inhibition
			ArrayList<Connection> possibleConnections = new ArrayList<Connection>();
			for (Connection conn : indiv.getNetwork().connections) {
				if (conn.enabled && conn.to.type != Node.INHIBITING_SEQUENCE) {
					possibleConnections.add(conn);
				}
			}
			if (possibleConnections.size() > 0) {
				Connection conn = possibleConnections.get(rand.nextInt(possibleConnections.size()));
				conn.enabled = false;
				Node nodeFrom = conn.from;
				Node nodeTo = conn.to;
				Node newNode = indiv.addNodeByOrigin(nodeFrom.name + "->DI->" + nodeTo.name,
						Randomizer.getRandomLogScale(Individual.minNodeValue, Individual.maxNodeValue));
				addInhibition(indiv, nodeFrom, newNode);
				addInhibition(indiv, newNode, nodeTo);
			}
			return indiv;
		}
	}

	private Individual addInhibition(Individual indiv, Node nodeFrom, Node nodeTo) {
		ArrayList<Connection> possibleConnections = new ArrayList<Connection>();
		for (Connection conn : indiv.getNetwork().connections) {
			if (conn.to.name.equals(nodeTo.name) && conn.enabled) {
				possibleConnections.add(conn);
			}
		}
		Connection conn = null;
		if (possibleConnections.size() == 0) {
			conn = indiv.addConnection(nodeTo, nodeTo,
					Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue));
		} else {
			conn = possibleConnections.get(rand.nextInt(possibleConnections.size()));
		}
		String inhibitionNodeName = "I" + conn.from.name + conn.to.name;
		Node inhibitionNode = indiv.getNetwork().getNodeByName(inhibitionNodeName);
		if (inhibitionNode == null) {
			inhibitionNode = new Node(inhibitionNodeName, Node.INHIBITING_SEQUENCE);
			indiv.getNetwork().nodes.add(inhibitionNode);
		}
		conn = indiv.getNetwork().getConnectionByEnds(nodeFrom, inhibitionNode);
		if (conn == null) {
			indiv.addConnection(nodeFrom, inhibitionNode,
					Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue));
		} else {
			conn.enabled = true;
			conn.parameter = Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue);
		}

		return indiv;
	}
}
