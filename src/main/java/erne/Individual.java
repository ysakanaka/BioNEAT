package erne;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang3.SerializationUtils;

import common.Static;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;

public class Individual implements Serializable {

	/**
	 * 
	 */

	public static double minNodeValue = 10;
	public static double maxNodeValue = 1000;
	public static double minTemplateValue = 1;
	public static double maxTemplateValue = 60;

	private static final long serialVersionUID = 1L;
	private ReactionNetwork network;
	private AbstractFitnessResult fitnessResult;
	private int id;
	public int speciesId;
	public ArrayList<Integer> parentIds;

	public Individual(ReactionNetwork network) {
		this.network = network;
		this.id = Population.nextIndivId.incrementAndGet();
		this.speciesId = -1;
		this.parentIds = new ArrayList<Integer>();
	}

	public ReactionNetwork getNetwork() {
		return network;
	}

	public Individual clone() {
		Individual cloned = (Individual) SerializationUtils.clone(this);
		cloned.id = Population.nextIndivId.incrementAndGet();
		cloned.speciesId = -1;
		return cloned;
	}

	public AbstractFitnessResult getFitnessResult() {
		return fitnessResult;
	}

	public void setFitnessResult(AbstractFitnessResult fitnessResult) {
		this.fitnessResult = fitnessResult;
	}

	public int getId() {
		return id;
	}

	public Node addNodeByOrigin(String nodeOrigins, double parameter) {
		String newNodeName = "";
		if (Population.nodeNameOrigins.containsKey(nodeOrigins)) {
			newNodeName = Population.nodeNameOrigins.get(nodeOrigins);
			if (network.getNodeByName(newNodeName) != null) {
				int i = 0;
				while (network.getNodeByName(newNodeName + String.valueOf(i)) != null) {
					i++;
				}
				newNodeName = newNodeName + String.valueOf(i);
			}
		} else {
			newNodeName = Character.toString((char) Population.nextNodeName.incrementAndGet());
			Population.nodeNameOrigins.put(nodeOrigins, newNodeName);
		}

		Node newNode = new Node(newNodeName);
		newNode.parameter = parameter;
		network.nodes.add(newNode);
		return newNode;
	}

	public Connection addConnection(Node fromNode, Node toNode, double parameter) {
		Connection conn = new Connection(fromNode, toNode);
		conn.parameter = parameter;
		network.connections.add(conn);
		String key = conn.from.name + "->" + conn.to.name;
		int innovationNumber;
		if (Population.innovationNumbers.containsKey(key)) {
			innovationNumber = Population.innovationNumbers.get(key);
		} else {
			innovationNumber = Population.innovationNumbers.size();
			Population.innovationNumbers.put(key, innovationNumber);
		}
		conn.innovation = innovationNumber;
		return conn;
	}

	@Override
	public String toString() {
		return Static.gson.toJson(network);
	}
}
