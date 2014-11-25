package reactionnetwork.visual;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;

public class RNGraph extends DirectedSparseGraph {
	protected Set<InhibitionEdge> mInhibitionEdges;

	public RNGraph() {
		super();
	}

	public RNGraph(ReactionNetwork network) {
		super();
		for (Node node : network.nodes) {
			this.addVertex(new RNVertex(node));
		}
		for (Connection connection : network.connections) {
			this.addEdge(connection);
		}
		for (Node node : network.nodes) {
			if (node.type == Node.INHIBITING_SEQUENCE) {
				this.addInhibitionEdge(node);
			}
		}
	}

	private InhibitionEdge addInhibitionEdge(Node node) {
		for (Iterator<?> iter = this.getEdges().iterator(); iter.hasNext();) {
			Edge edge = (Edge) iter.next();
			RNVertex v1 = (RNVertex) edge.getEndpoints().getFirst();
			RNVertex v2 = (RNVertex) edge.getEndpoints().getSecond();
			if (node.name.equals("I" + v1.node.name + v2.node.name)) {
				return this.addInhibitionEdge(new InhibitionEdge(
						getVertex(node), edge));
			}
		}
		return null;
	}

	public Edge addEdge(Connection connection) {
		Vertex from = getVertex(connection.from);
		Vertex to = getVertex(connection.to);
		if (from != null && to != null) {
			return this.addEdge(new RNEdge(from, to, connection));
		} else {
			return null;
		}
	}

	public Vertex getVertex(Node node) {
		for (Iterator<?> iter = this.getVertices().iterator(); iter.hasNext();) {
			RNVertex v = (RNVertex) iter.next();
			if (v.node.equals(node)) {
				return v;
			}
		}
		return null;
	}

	@Override
	protected void initialize() {
		mInhibitionEdges = new HashSet<InhibitionEdge>();
		super.initialize();
	}

	public InhibitionEdge addInhibitionEdge(InhibitionEdge i) {
		mInhibitionEdges.add(i);
		return i;
	}

	public Set<InhibitionEdge> getInhibitionEdges() {
		return Collections.unmodifiableSet(mInhibitionEdges);
	}

}
