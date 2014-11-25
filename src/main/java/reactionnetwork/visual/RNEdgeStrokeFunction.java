package reactionnetwork.visual;

import java.awt.BasicStroke;
import java.awt.Stroke;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.decorators.EdgeStrokeFunction;

public class RNEdgeStrokeFunction implements EdgeStrokeFunction {

	public Stroke getStroke(Edge e) {
		if (e instanceof RNEdge) {
			RNEdge edge = (RNEdge) e;
			float thickness = (float) (1 + edge.connection.parameter * 3 / 60);
			return new BasicStroke(thickness);
		}
		return new BasicStroke(1.0f);
	}

}
