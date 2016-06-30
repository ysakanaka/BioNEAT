package reactionnetwork.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;

import javax.swing.border.LineBorder;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.util.ArrowFactory;
import reactionnetwork.ReactionNetwork;

public class RNVisualizationViewerFactory {
	public VisualizationViewer<String, String> createVisualizationViewer(
			ReactionNetwork newNetwork) {
		RNGraph g = new RNGraph(newNetwork); // initial graph
        
		ISOMLayout<String, String> layout = new ISOMLayout<String, String>(g);
		layout.setSize(new Dimension(250, 250));
		VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(
				layout);
		vv.setPreferredSize(new Dimension(250, 250));
		vv.setBorder(new LineBorder(new Color(0, 0, 0)));
		// Setup up a new vertex to paint transformer...
		Function<String, Paint> vertexPaint = new Function<String, Paint>() {
			public Paint apply(String i) {
				return Color.GREEN;
			}
		};

		vv.setBackground(Color.WHITE);
		vv.setRenderer(new RNRenderer());
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		vv.getRenderContext()
				.setEdgeArrowTransformer(
						new Function<Context<Graph<String, String>, String>, Shape>() {

							public Shape apply(
									Context<Graph<String, String>, String> input) {
								return ArrowFactory.getWedgeArrow(10, 8);
							}
						});
		vv.getRenderContext().setVertexLabelTransformer(
				new Function<String, String>() {

					public String apply(String input) {
						return input;
					}
				});

		vv.getRenderContext().setVertexFillPaintTransformer(
				new RNVertexFillPaintTransformer(g));

		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<String, String>();
		gm.setMode(ModalGraphMouse.Mode.PICKING);
		vv.setGraphMouse(gm);
		return vv;
	}
}