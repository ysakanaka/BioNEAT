package erne;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang3.SerializationUtils;

import common.Static;
import erne.algorithm.bioNEAT.BioNEATPopulation;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import use.ready.eqwriter.Utils;

public class Individual implements Serializable {

	/**
	 * 
	 */

	public static double minNodeValue = 10;
	public static double maxNodeValue = 1000;
	public static double minTemplateValue = 1;
	public static double maxTemplateValue = 200;

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
			//newNodeName = Character.toString((char) Population.nextNodeName.incrementAndGet()); //Huy version, making weird characters
			newNodeName = Utils.idToString(Population.nextNodeName.incrementAndGet());
			Population.nodeNameOrigins.put(nodeOrigins, newNodeName);
		}

		Node newNode = new Node(newNodeName);
		newNode.parameter = parameter;
		network.nodes.add(newNode);
		return newNode;
	}

	public Connection addConnection(Node fromNode, Node toNode, double parameter) {
		Connection conn = network.addConnection(0, fromNode, toNode); //NAT: innovation is set latter. TODO: make BioNEAT Individuals or something
		conn.parameter = parameter;
		String key = conn.from.name + "->" + conn.to.name;
		int innovationNumber;
		if (BioNEATPopulation.innovationNumbers.containsKey(key)) {
			innovationNumber = BioNEATPopulation.innovationNumbers.get(key);
		} else {
			innovationNumber = BioNEATPopulation.innovationNumbers.size();
			BioNEATPopulation.innovationNumbers.put(key, innovationNumber);
		}
		conn.innovation = innovationNumber;
		
		return conn;
	}

	@Override
	public String toString() {
		return Static.gson.toJson(network);
	}

	public void sanitize() {
		// NOTE: I can do that later, actually
		// Now, we need to check if the system is still sane
		// So far, it only means that we have no inhibitor pointing to nothing.
		//ArrayList<Node> toRemove = new ArrayList<Node>();
//		for (Node inhib: network.nodes){
//			if (inhib.type == Node.INHIBITING_SEQUENCE){
//				Node from = network.getNodeByName(""+inhib.name.charAt(1)); // TODO: warning, very implementation dependent
//				Node to = network.getNodeByName(""+inhib.name.charAt(2));
//				Connection inhibited = network.getConnectionByEnds(from, to);
//				if(inhibited == null || !inhibited.enabled){
//					for (Connection cn : network.connections){
//						if (cn.to.equals(inhib)){
//							cn.enabled = false;
//						}
//					}
//					//toRemove.add(inhib); // No choice
//				}
//			}
//		}
		//network.nodes.removeAll(toRemove);
	}
}
