package reactionnetwork.visual;

import java.awt.Color;
import java.awt.Paint;

import model.Constants;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;

public class RNVertexPaintFunction implements VertexPaintFunction {

	public Paint getFillPaint(Vertex v) {
		if (v instanceof RNVertex) {
			RNVertex vertex = (RNVertex) v;
			double seqK = vertex.node.parameter;
			double min = Math.log(Constants.simpleKmin * 100);
			double max = Math.log(Constants.simpleKmax / 100);
			double i = (Math.log(seqK) - min) / (max - min);
			double k = 3.6;
			int red = (int) (255 - 255 * Math.exp(-k * i));
			int blue = (int) (255 - 255 * Math.exp(k * (-1 + i)));
			if (red > 255) {
				red = 255;
			} else if (red < 0) {
				red = 0;
			}
			if (blue > 255) {
				blue = 255;
			} else if (blue < 0) {
				blue = 0;
			}
			Color color = new Color(red, 0, blue);
			if (vertex.node.name.startsWith("I")) {
				color = new Color(0, 255, 0);
			}
			return color;
		}
		return Color.RED;
	}

	public Paint getDrawPaint(Vertex v) {
		return Color.BLACK;
	}

}
