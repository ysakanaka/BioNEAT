package reactionnetwork.visual;

import reactionnetwork.Connection;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;

public class RNEdge extends DirectedSparseEdge {

	public Connection connection;

	public RNEdge(Vertex from, Vertex to, Connection connection) {
		super(from, to);
		this.connection = connection;
	}

}
