package use.processing.mutation.rules;

import java.util.ArrayList;

import erne.Individual;
import erne.mutation.MutationRule;
import erne.util.Randomizer;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import use.processing.rd.RDConstants;

/**
 * Copy/paste of the similar class in bioneat with some modifications
 * (Gradients cannot be targets anymore)
 * @author naubertkato
 *
 */

public class AddInhibitionWithGradients extends MutationRule {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddInhibitionWithGradients(int weight) {
		super(weight);
	}

	@Override
	public Individual mutate(Individual indiv) {
		if (rand.nextBoolean()) {
			// add single inhibition
			ArrayList<Node> possibleNodesfrom = new ArrayList<Node>();
			ArrayList<Node> possibleNodesto = new ArrayList<Node>();
			for (Node node : indiv.getNetwork().nodes) {
				if (node.type == Node.SIMPLE_SEQUENCE) {
					possibleNodesfrom.add(node);
					if(!isGradient(node)) possibleNodesto.add(node);
				}
			}
			if (possibleNodesto.size()>0) {
				Node nodeFrom = possibleNodesfrom.get(rand.nextInt(possibleNodesfrom.size()));
				Node nodeTo = possibleNodesto.get(rand.nextInt(possibleNodesto.size()));
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

	protected Individual addInhibition(Individual indiv, Node nodeFrom, Node nodeTo) {
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
		String inhibitionNodeName = "I" + conn.from.name +"T"+ conn.to.name;
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

	protected boolean isGradient(Node node){
		for(int i = 0; i<RDConstants.gradientsName.length; i++){
			if(RDConstants.gradientsName[i].equals(node.name)) return true;
		}
		return false;
	}
	
	@Override
	public boolean isApplicable(Individual indiv) {
		
		return !RDConstants.ceilingNodes || indiv.getNetwork().nodes.size() < RDConstants.maxNodes;
	}
}
