package reactionnetwork.visual;

import java.awt.Color;
import java.awt.Dimension;

import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import reactionnetwork.ReactionNetwork;

public class RNVisualizationViewerFactory {
	public RNVisualizationViewer createVisualizationViewer(
			ReactionNetwork newNetwork) {
		RNGraph g = new RNGraph(newNetwork); // initial graph

		ISOMLayout layout = new ISOMLayout(g);

		RNVisualizationViewer vv = new RNVisualizationViewer(layout,
				new RNRenderer(), new Dimension(300, 300));
		vv.setBackground(Color.WHITE);

		final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		graphMouse.setMode(Mode.PICKING);
		vv.setGraphMouse(graphMouse);
		vv.setPickSupport(new ShapePickSupport());
		return vv;
	}
}
