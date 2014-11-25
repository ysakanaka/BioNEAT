package reactionnetwork.visual;

import reactionnetwork.Node;
import edu.uci.ics.jung.graph.impl.SparseVertex;

public class RNVertex extends SparseVertex {

	public Node node;

	public RNVertex(Node node) {
		this.node = node;
	}
}
