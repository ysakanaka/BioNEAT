package reactionnetwork.visual;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.DirectionalEdgeArrowFunction;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class RNVisualizationViewer extends VisualizationViewer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	long[] paintTimes = new long[5];
	int paintIndex = 0;
	double paintfps, relaxfps;

	protected RNRenderer renderer;

	public RNVisualizationViewer(Layout layout, RNRenderer renderer,
			Dimension dimension) {
		super(layout, renderer, dimension);
		this.renderer = renderer;
		renderer.setEdgeStrokeFunction(new RNEdgeStrokeFunction());
		renderer.setVertexPaintFunction(new RNVertexPaintFunction());
		renderer.setVertexStringer(new RNVertexStringer());
		renderer.setVertexLabelCentering(true);
		renderer.setEdgeArrowFunction(new DirectionalEdgeArrowFunction(10, 8, 0));
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void renderGraph(Graphics2D g2d) {

		Layout layout = model.getGraphLayout();

		g2d.setRenderingHints(renderingHints);

		long start = System.currentTimeMillis();

		// the size of the VisualizationViewer
		Dimension d = getSize();

		// clear the offscreen image
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, d.width, d.height);

		AffineTransform oldXform = g2d.getTransform();
		AffineTransform newXform = new AffineTransform(oldXform);
		newXform.concatenate(viewTransformer.getTransform());

		g2d.setTransform(newXform);

		// if there are preRenderers set, paint them
		for (Iterator<?> iterator = preRenderers.iterator(); iterator.hasNext();) {
			Paintable paintable = (Paintable) iterator.next();
			if (paintable.useTransform()) {
				paintable.paint(g2d);
			} else {
				g2d.setTransform(oldXform);
				paintable.paint(g2d);
				g2d.setTransform(newXform);
			}
		}

		locationMap.clear();

		Set<Edge> renderedEdge = new HashSet<Edge>();

		// paint all the inhibition edges
		try {
			RNGraph graph = (RNGraph) layout.getGraph();
			for (Iterator<?> iter = graph.getInhibitionEdges().iterator(); iter
					.hasNext();) {
				InhibitionEdge i = (InhibitionEdge) iter.next();
				Vertex v = (Vertex) i.getEndpoints().getFirst();
				Edge e = (Edge) i.getEndpoints().getSecond();
				Vertex v1 = (Vertex) e.getEndpoints().getFirst();
				Vertex v2 = (Vertex) e.getEndpoints().getSecond();

				Point2D p = (Point2D) locationMap.get(v);
				if (p == null) {
					p = layout.getLocation(v);
					p = layoutTransformer.transform(p);
					locationMap.put(v, p);
				}
				Point2D p1 = (Point2D) locationMap.get(v1);
				if (p1 == null) {
					p1 = layout.getLocation(v1);
					p1 = layoutTransformer.transform(p1);
					locationMap.put(v1, p1);
				}
				Point2D p2 = (Point2D) locationMap.get(v2);
				if (p2 == null) {
					p2 = layout.getLocation(v2);
					p2 = layoutTransformer.transform(p2);
					locationMap.put(v2, p2);
				}

				if (p != null && p1 != null && p2 != null) {
					renderer.paintInhibitionEdge(g2d, i, (int) p.getX(),
							(int) p.getY(), (int) p1.getX(), (int) p1.getY(),
							(int) p2.getX(), (int) p2.getY());
					if (v1.equals(v2)) {
						renderer.paintInhibitedAutocatalysisEdge(g2d, i,
								(int) p.getX(), (int) p.getY(),
								(int) p1.getX(), (int) p1.getY(),
								(int) p2.getX(), (int) p2.getY());
						renderedEdge.add(e);
					}
				}
			}
		} catch (ConcurrentModificationException cme) {
			repaint();
		}

		// paint all the edges
		try {
			for (Iterator<?> iter = layout.getGraph().getEdges().iterator(); iter
					.hasNext();) {
				Edge e = (Edge) iter.next();
				if (renderedEdge.contains(e))
					continue;
				Vertex v1 = (Vertex) e.getEndpoints().getFirst();
				Vertex v2 = (Vertex) e.getEndpoints().getSecond();

				Point2D p = (Point2D) locationMap.get(v1);
				if (p == null) {

					p = layout.getLocation(v1);
					p = layoutTransformer.transform(p);
					locationMap.put(v1, p);
				}
				Point2D q = (Point2D) locationMap.get(v2);
				if (q == null) {
					q = layout.getLocation(v2);
					q = layoutTransformer.transform(q);
					locationMap.put(v2, q);
				}

				if (p != null && q != null) {
					renderer.paintEdge(g2d, e, (int) p.getX(), (int) p.getY(),
							(int) q.getX(), (int) q.getY());
				}
			}
		} catch (ConcurrentModificationException cme) {
			repaint();
		}

		// paint all the vertices
		try {
			for (Iterator<?> iter = layout.getGraph().getVertices().iterator(); iter
					.hasNext();) {

				Vertex v = (Vertex) iter.next();
				Point2D p = (Point2D) locationMap.get(v);
				if (p == null) {
					p = layout.getLocation(v);
					p = layoutTransformer.transform(p);
					locationMap.put(v, p);
				}
				if (p != null) {
					renderer.paintVertex(g2d, v, (int) p.getX(), (int) p.getY());
				}
			}
		} catch (ConcurrentModificationException cme) {
			repaint();
		}

		long delta = System.currentTimeMillis() - start;
		paintTimes[paintIndex++] = delta;
		paintIndex = paintIndex % paintTimes.length;
		paintfps = average(paintTimes);

		// if there are postRenderers set, do it
		for (Iterator<?> iterator = postRenderers.iterator(); iterator
				.hasNext();) {
			Paintable paintable = (Paintable) iterator.next();
			if (paintable.useTransform()) {
				paintable.paint(g2d);
			} else {
				g2d.setTransform(oldXform);
				paintable.paint(g2d);
				g2d.setTransform(newXform);
			}
		}
		g2d.setTransform(oldXform);
	}

}
