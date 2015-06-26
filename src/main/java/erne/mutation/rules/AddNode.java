package erne.mutation.rules;

import java.util.ArrayList;

import reactionnetwork.Connection;
import reactionnetwork.Node;
import erne.Individual;
import erne.Population;
import erne.mutation.MutationRule;

public class AddNode extends MutationRule {
	public double probGeneMutation = 0.8;

	public AddNode(int weight) {
		super(weight);
	}

	@Override
	public Individual mutate(Individual indiv) {
		if (rand.nextDouble() < 1.0f / (indiv.getNetwork().getNSimpleSequences() + 1)) {
			if (rand.nextBoolean()) {
				// new node connect to a simple sequence node

				Node to = null;
				while (to == null || to.type == Node.INHIBITING_SEQUENCE) {
					int index = rand.nextInt(indiv.getNetwork().nodes.size());
					to = indiv.getNetwork().nodes.get(index);
				}

				Node newNode = indiv.addNodeByOrigin("null->" + to.name);

				indiv.addConnection(newNode, newNode, (Individual.minTemplateValue + Individual.maxTemplateValue) / 2);

				indiv.addConnection(newNode, to, (Individual.minTemplateValue + Individual.maxTemplateValue) / 2);

			} else {
				// new node connect to inhibiting sequence node
				ArrayList<Connection> possibleActivations = new ArrayList<Connection>();
				for (Connection conn : indiv.getNetwork().connections) {
					if (conn.to.type != Node.INHIBITING_SEQUENCE && conn.enabled) {
						possibleActivations.add(conn);
					}
				}
				if (possibleActivations.size() != 0) {
					int index = rand.nextInt(possibleActivations.size());
					Connection connection = possibleActivations.get(index);
					String inhibitionNodeName = "I" + connection.from.name + connection.to.name;
					Node inhibitionNode = indiv.getNetwork().getNodeByName(inhibitionNodeName);
					if (inhibitionNode == null) {
						inhibitionNode = new Node(inhibitionNodeName);
						indiv.getNetwork().nodes.add(inhibitionNode);
					}
					Node newNode = indiv.addNodeByOrigin("null->" + inhibitionNode.name);

					indiv.addConnection(newNode, newNode, (Individual.minTemplateValue + Individual.maxTemplateValue) / 2);

					indiv.addConnection(newNode, inhibitionNode, (Individual.minTemplateValue + Individual.maxTemplateValue) / 2);

				}
			}

		} else if (indiv.getNetwork().connections.size() > 0) {
			ArrayList<Connection> possibleActivations = new ArrayList<Connection>();
			for (Connection conn : indiv.getNetwork().connections) {
				if (conn.enabled) {
					possibleActivations.add(conn);
				}
			}
			if (possibleActivations.size() > 0) {
				Connection connection = null;

				int index = rand.nextInt(possibleActivations.size());
				connection = getCorrectConnection(indiv, possibleActivations.get(index));

				connection.enabled = false;
				Node newNode = indiv.addNodeByOrigin(connection.from.name + "->" + connection.to.name);

				indiv.addConnection(connection.from, newNode,
						(connection.to.type == Node.INHIBITING_SEQUENCE) ? (Individual.minTemplateValue + Individual.maxTemplateValue) / 2
								: connection.parameter);

				indiv.addConnection(newNode, connection.to, connection.parameter);

				String oldNodeName = "I" + connection.from.name + connection.to.name;
				for (Node node : indiv.getNetwork().nodes) {
					if (node.name.equals(oldNodeName)) {
						node.name = "I" + connection.from.name + newNode.name;
						for (Connection conn : indiv.getNetwork().connections) {
							if (conn.to == node) {
								String key = conn.from.name + "->" + conn.to.name;
								int innovationNumber;
								if (Population.innovationNumbers.containsKey(key)) {
									innovationNumber = Population.innovationNumbers.get(key);
								} else {
									innovationNumber = Population.innovationNumbers.size();
									Population.innovationNumbers.put(key, innovationNumber);
								}
								conn.innovation = innovationNumber;
							}
						}
						break;
					}
				}
			}
		}
		return indiv;
	}

	private Connection getBackConnection(Individual indiv, Connection connection) {
		Connection result = null;
		int countIn = 0;
		int countOut = 0;
		for (Connection conn : indiv.getNetwork().connections) {
			if (conn.from == connection.from && conn.enabled) {
				countOut++;
			}
			if (conn != connection && conn.to == connection.from && conn.enabled) {
				result = conn;
				countIn++;
			}
		}
		if (countIn == 1 & countOut == 1) {
			return result;
		} else {
			return null;
		}
	}

	protected Connection getCorrectConnection(Individual indiv, Connection connection) {
		Connection conn = connection;
		while (getBackConnection(indiv, conn) != null && getBackConnection(indiv, conn) != connection) {
			conn = getBackConnection(indiv, conn);
		}
		return conn;

	}
}
