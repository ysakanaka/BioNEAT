package use.processing.mutation.rules;

import java.util.ArrayList;

import erne.Individual;
import erne.Population;
import erne.algorithm.bioNEAT.BioNEATPopulation;
import erne.mutation.rules.AddNode;
import erne.util.Randomizer;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import use.processing.rd.RDConstants;

public class AddNodeWithGradients extends AddNode {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddNodeWithGradients(int weight) {
		super(weight);
	}

	@Override
	public Individual mutate(Individual indiv) {
		if (rand.nextDouble() < 1.0f / (indiv.getNetwork().getNSimpleSequences() + 1)) {
			if ((RDConstants.ceilingTemplates && indiv.getNetwork().getNEnabledConnections() >= RDConstants.maxTemplates - 1) ||
					rand.nextBoolean()) {
				// new node connect to a simple sequence node

				Node to = null;
				while (to == null || to.type == Node.INHIBITING_SEQUENCE || isGradient(to)) {
					int index = rand.nextInt(indiv.getNetwork().nodes.size());
					to = indiv.getNetwork().nodes.get(index);
				}

				Node newNode = indiv.addNodeByOrigin("null->" + to.name,
						Randomizer.getRandomLogScale(Individual.minNodeValue, Individual.maxNodeValue));

				indiv.addConnection(newNode, newNode,
						Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue));

				indiv.addConnection(newNode, to, Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue));

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
					String inhibitionNodeName = "I" + connection.from.name +"T"+ connection.to.name;
					Node inhibitionNode = indiv.getNetwork().getNodeByName(inhibitionNodeName);
					if (inhibitionNode == null) {
						inhibitionNode = new Node(inhibitionNodeName, Node.INHIBITING_SEQUENCE);
						indiv.getNetwork().nodes.add(inhibitionNode);
					}
					Node newNode = indiv.addNodeByOrigin("null->" + inhibitionNode.name,
							Randomizer.getRandomLogScale(Individual.minNodeValue, Individual.maxNodeValue));

					indiv.addConnection(newNode, newNode,
							Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue));

					indiv.addConnection(newNode, inhibitionNode,
							Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue));

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
				Node newNode = indiv.addNodeByOrigin(connection.from.name + "->" + connection.to.name,
						Randomizer.getRandomLogScale(Individual.minNodeValue, Individual.maxNodeValue));

				indiv.addConnection(
						connection.from,
						newNode,
						(connection.to.type == Node.INHIBITING_SEQUENCE) ? Randomizer.getRandomLogScale(Individual.minTemplateValue,
								Individual.maxTemplateValue) : connection.parameter);

				indiv.addConnection(newNode, connection.to, connection.parameter);

				String oldNodeName = "I" + connection.from.name + "T" + connection.to.name;
				for (Node node : indiv.getNetwork().nodes) {
					if (node.name.equals(oldNodeName)) {
						node.name = "I" + connection.from.name + "T" + newNode.name;
						for (Connection conn : indiv.getNetwork().connections) {
							if (conn.to == node) {
								String key = conn.from.name + "->" + conn.to.name;
								int innovationNumber;
								if (BioNEATPopulation.innovationNumbers.containsKey(key)) {
									innovationNumber = BioNEATPopulation.innovationNumbers.get(key);
								} else {
									innovationNumber = BioNEATPopulation.innovationNumbers.size();
									BioNEATPopulation.innovationNumbers.put(key, innovationNumber);
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
	
	protected boolean isGradient(Node node){
		for(int i = 0; i<RDConstants.gradientsName.length; i++){
			if(RDConstants.gradientsName[i].equals(node.name)) return true;
		}
		return false;
	}
	
	@Override
	public boolean isApplicable(Individual indiv) {
		//It's always possible to add a connection.
		//This should be modified if we want to implement a maximum system size.
		if (RDConstants.ceilingTemplates && indiv.getNetwork().getNEnabledConnections() >= RDConstants.maxTemplates)
			return false;
		
		if (RDConstants.useMaxTotalNodes)
			return !RDConstants.ceilingNodes || indiv.getNetwork().nodes.size() < RDConstants.maxNodes;
		
		return !RDConstants.ceilingNodes || indiv.getNetwork().getNSimpleSequences() < RDConstants.maxNodes;
	}

}
