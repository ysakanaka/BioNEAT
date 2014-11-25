package reactionnetwork.visual;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.AbstractArchetypeEdge;
import edu.uci.ics.jung.utils.Pair;

public class InhibitionEdge extends AbstractArchetypeEdge {

	/**
	 * One of the two incident vertices of this edge. If this edge is directed,
	 * this is its source.
	 */
	protected Vertex mFrom;

	/**
	 * One of the two incident vertices of this edge. If this edge is directed,
	 * this is its destination.
	 */
	protected Edge mTo;

	protected int id = -1;

	private static int nextGlobalInhibitionEdgeID = 0;

	/**
	 * Creates a inhibition whose source is <code>from</code> and whose
	 * destination is <code>to</code>.
	 */
	public InhibitionEdge(Vertex from, Edge to) {
		super();
		if (from == null || to == null)
			throw new IllegalArgumentException(
					"Vertices and edges passed in can not be null");

		if (from.getGraph() != to.getGraph())
			throw new IllegalArgumentException(
					"Vertices and edges must be from same graph");

		if (from.getGraph() == null || to.getGraph() == null)
			throw new IllegalArgumentException(
					"Orphaned vertices and edgescan not "
							+ "be connected by an edge");

		mFrom = from;
		mTo = to;

		this.id = nextGlobalInhibitionEdgeID++;
	}

	public Pair getEndpoints() {
		return new Pair(mFrom, mTo);
	}

	/**
	 * @see DirectedEdge#getSource()
	 */
	public Vertex getSource() {
		return (Vertex) getEndpoints().getFirst();
	}

	/**
	 * @see DirectedEdge#getDest()
	 */
	public Edge getDest() {
		return (Edge) getEndpoints().getSecond();
	}

	public Set getIncidentVertices() {
		Set vertices = new LinkedHashSet(1);
		vertices.add(mFrom);

		return Collections.unmodifiableSet(vertices);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getSource() + "->" + getDest();
	}
}
