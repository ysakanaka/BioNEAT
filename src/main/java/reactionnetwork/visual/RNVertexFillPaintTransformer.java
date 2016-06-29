package reactionnetwork.visual;

import java.awt.Color;
import java.awt.Paint;

import org.apache.commons.collections15.Transformer;

public class RNVertexFillPaintTransformer implements Transformer<String, Paint> {

	RNGraph graph;

	public RNVertexFillPaintTransformer(RNGraph graph) {
		this.graph = graph;
	}

	public Paint transform(String input) {
		double seqK = graph.getVertexK(input);
		double min = Math.log(3e-4 / 0.2 * 100);
		double max = Math.log(2e3 / 0.2 / 100);
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
		if (input.startsWith("I")) {
			color = new Color(0, 255, 0);
		}
		return color;
	}

}
