package reactionnetwork.visual;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class RNGraph extends DirectedSparseGraph<String, String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Map<String, MyPair<String, String>> inhibitions;
	protected Map<String, Double> edgesConcentration;
	protected Map<String, Double> verticesK;

	public RNGraph() {
		super();
		inhibitions = new HashMap<String, MyPair<String, String>>();
		edgesConcentration = new HashMap<String, Double>();
		verticesK = new HashMap<String, Double>();
	}

	public RNGraph(ReactionNetwork network) {
		this();
		for (Node node : network.nodes) {
			this.addVertex(node.name);
			this.addVertexK(node.name, node.parameter);
		}
		for (Connection connection : network.connections) {
			if (connection.enabled) {
				this.addEdge(connection.from.name + connection.to.name, connection.from.name, connection.to.name);
				this.addEdgeConcentration(connection.from.name + connection.to.name, connection.parameter);
			}
		}
		for (Node node : network.nodes) {
			if (node.type == Node.INHIBITING_SEQUENCE) {
				MyPair<String, String> inhib = new MyPair<String, String>(node.name, node.name.replace("I", ""));
				this.addInhibition(node.name, inhib);
			}
		}
	}

	public Collection<String> getInhibitions() {
		return Collections.unmodifiableCollection(inhibitions.keySet());
	}

	public boolean containsInhibition(String inhibition) {
		return inhibitions.keySet().contains(inhibition);
	}

	public boolean addInhibition(String inhibition, MyPair<String, String> myPair) {
		inhibitions.put(inhibition, myPair);
		return true;
	}

	public boolean addVertexK(String vertex, double K) {
		verticesK.put(vertex, K);
		return true;
	}

	public double getVertexK(String vertex) {
		try {
			return verticesK.get(vertex);
		} catch (Exception e) {
			return 0;
		}
	}

	public boolean addEdgeConcentration(String edge, double concentration) {
		edgesConcentration.put(edge, concentration);
		return true;
	}

	public double getEdgeConcentration(String edge) {
		if (!edgesConcentration.containsKey(edge)) {
			return 0;
		}
		return edgesConcentration.get(edge);
	}

	public MyPair<String, String> getInhibition(String inh) {
		return inhibitions.get(inh);
	}
}
