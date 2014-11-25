package reactionnetwork.visual;

import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.decorators.VertexStringer;

public class RNVertexStringer implements VertexStringer {

	public String getLabel(ArchetypeVertex v) {
		if (v instanceof RNVertex) {
			RNVertex vertex = (RNVertex) v;
			return vertex.node.name;
		} else {
			return null;
		}

	}

}
