package reactionnetwork.visual;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.ConcurrentModificationException;

import javax.swing.JComponent;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.IndexedRendering;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class RNRenderer extends BasicRenderer<String, String> {

	RNEdgeArrowRenderingSupport edgeArrowRenderingSupport = new RNEdgeArrowRenderingSupport();

	public RNRenderer() {
		super();
	}

	@Override
	public void render(RenderContext<String, String> renderContext,
			Layout<String, String> layout) {
		// paint all the edges
		try {
			for (String e : layout.getGraph().getEdges()) {

				renderEdge(renderContext, layout, e);
				renderEdgeLabel(renderContext, layout, e);
			}
		} catch (ConcurrentModificationException cme) {
			renderContext.getScreenDevice().repaint();
		}

		// paint all the inhibitions
		try {
			RNGraph graph = (RNGraph) layout.getGraph();
			Collection<String> collection = graph.getInhibitions();
			for (String inh : collection) {
				renderInhibition(renderContext, layout, inh);
			}
		} catch (ConcurrentModificationException cme) {
		}

		// paint all the vertices
		try {
			for (String v : layout.getGraph().getVertices()) {
				if (layout.getGraph().degree(v) > 0) {
					renderVertex(renderContext, layout, v);
					renderVertexLabel(renderContext, layout, v);
				}
			}
		} catch (ConcurrentModificationException cme) {
			renderContext.getScreenDevice().repaint();
		}
	}

	public void renderInhibition(RenderContext<String, String> rc,
			Layout<String, String> layout, String inh) {
		RNGraph graph = (RNGraph) layout.getGraph();
		MyPair<String, String> pair = graph.getInhibition(inh);
		String v = pair.getLeft();
		String e = pair.getRight();
		Pair<String> endpoints = graph.getEndpoints(e);
		if (endpoints != null) {
			String v1 = endpoints.getFirst();
			String v2 = endpoints.getSecond();

			GraphicsDecorator g = rc.getGraphicsContext();
			g.setStroke(new BasicStroke(1));
			Point2D p = layout.transform(v);
			Point2D p1 = layout.transform(v1);
			Point2D p2 = layout.transform(v2);

			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
			p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
			p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
			float x = (float) p.getX();
			float y = (float) p.getY();
			float x1 = (float) p1.getX();
			float y1 = (float) p1.getY();
			float x2 = (float) p2.getX();
			float y2 = (float) p2.getY();

			double dist12 = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)
					* (y1 - y2));
			boolean isLoop = v1.equals(v2);
			float endX, endY;
			float finalX, finalY;
			if (isLoop) {
				double distToInhibition = Math.sqrt((x1 - x) * (x1 - x)
						+ (y1 - y) * (y1 - y));
				endX = (float) (x1 + (x - x1) * 10 / distToInhibition);
				endY = (float) (y1 + (y - y1) * 10 / distToInhibition);
				float dx = endX - x;
				float dy = endY - y;
				float distance = (float) Math.sqrt(dx * dx + dy * dy);
				finalX = endX - 15 * (endX - x) / distance;
				finalY = endY - 15 * (endY - y) / distance;
			} else {
				endX = (x2 + x1) / 2;
				endY = (y2 + y1) / 2;
				double[] pt = { endX, endY };
				AffineTransform.getRotateInstance(Math.atan2(20, dist12), x1,
						y1).transform(pt, 0, pt, 0, 1); // specifying to use
				// this
				// double[] to hold coords
				endX = (float) pt[0];
				endY = (float) pt[1];
				float dx = endX - x;
				float dy = endY - y;
				float distance = (float) Math.sqrt(dx * dx + dy * dy);

				finalX = endX - 5 * (endX - x) / distance;
				finalY = endY - 5 * (endY - y) / distance;
			}

			GeneralPath instance = new GeneralPath();
			instance.moveTo(x, y);
			instance.lineTo(finalX, finalY);
			Shape edgeShape = instance;

			instance = new GeneralPath();
			instance.moveTo(-1, 0);
			instance.lineTo(1, 0);
			Shape endShape = instance;
			AffineTransform xform = AffineTransform.getTranslateInstance(
					finalX, finalY);
			float dx = finalX - x;
			float dy = finalY - y;
			float thetaRadians = (float) Math.atan2(dy, dx);
			xform.rotate(thetaRadians + Math.PI / 2);
			xform.scale(4, 0.0);
			endShape = xform.createTransformedShape(endShape);

			Paint oldPaint = g.getPaint();

			// get Paints for filling and drawing
			// (filling is done first so that drawing and label use same Paint)
			Paint fill_paint = rc.getEdgeFillPaintTransformer().transform(e);
			if (fill_paint != null) {
				g.setPaint(fill_paint);
				g.fill(edgeShape);
				g.fill(endShape);
			}
			Paint draw_paint = rc.getEdgeDrawPaintTransformer().transform(e);
			if (draw_paint != null) {
				g.setPaint(draw_paint);
				g.draw(edgeShape);
				g.draw(endShape);
			}

			// restore old paint
			g.setPaint(oldPaint);
		}
	}

	@Override
	public void renderEdge(RenderContext<String, String> rc,
			Layout<String, String> layout, String e) {
		Dimension d = layout.getSize();
		double height = d.getHeight();
		double width = d.getWidth();
		GraphicsDecorator g = rc.getGraphicsContext();
		RNGraph graph = (RNGraph) layout.getGraph();
		Pair<String> endpoints = graph.getEndpoints((String) e);
		String v1 = endpoints.getFirst();
		String v2 = endpoints.getSecond();
		int thickness = (int) (1 + graph.getEdgeConcentration((String) e) * 3 / 60);
		g.setStroke(new BasicStroke(thickness));

		Point2D p1 = layout.transform(v1);
		Point2D p2 = layout.transform(v2);
		p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
		p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
		float x1 = (float) p1.getX();
		float y1 = (float) p1.getY();
		float x2 = (float) p2.getX();
		float y2 = (float) p2.getY();

		boolean isLoop = v1.equals(v2);
		Shape s2 = rc.getVertexShapeTransformer().transform(v2);
		Shape edgeShape = rc.getEdgeShapeTransformer().transform(
				Context.<Graph<String, String>, String> getInstance(graph, e));

		boolean edgeHit = true;
		boolean arrowHit = true;
		Rectangle deviceRectangle = null;
		JComponent vv = rc.getScreenDevice();
		if (vv != null) {
			deviceRectangle = new Rectangle(0, 0, d.width, d.height);
		}

		AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

		if (isLoop) {
			// this is a self-loop. scale it is larger than the vertex
			// it decorates and translate it so that its nadir is
			// at the center of the vertex.
			boolean inhibited = false;
			String inhibitionNode = null;
			for (String v : layout.getGraph().getVertices()) {
				if (v.equals("I" + e.toString())) {
					inhibited = true;
					inhibitionNode = v;
					break;
				}
			}
			if (inhibited) {
				Point2D p3 = layout.transform(inhibitionNode);
				p3 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p3);
				float x3 = (float) p3.getX();
				float y3 = (float) p3.getY();
				Rectangle2D s2Bounds = s2.getBounds2D();
				xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
				double distToInhibition = Math.sqrt((x1 - x3) * (x1 - x3)
						+ (y1 - y3) * (y1 - y3));
				xform.translate((x3 - x1) * edgeShape.getBounds2D().getWidth()
						/ 2 / distToInhibition, (y3 - y1)
						* edgeShape.getBounds2D().getHeight() / 2
						/ distToInhibition);
			} else {
				Rectangle2D s2Bounds = s2.getBounds2D();
				xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
				double distToCenter = Math.sqrt((x1 - width / 2)
						* (x1 - width / 2) + (y1 - height / 2)
						* (y1 - height / 2));

				xform.translate(
						-(width / 2 - x1) * edgeShape.getBounds2D().getWidth()
								/ 2 / distToCenter, -(height / 2 - y1)
								* edgeShape.getBounds2D().getHeight() / 2
								/ distToCenter);
			}
			// xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
		} else if (rc.getEdgeShapeTransformer() instanceof EdgeShape.Orthogonal) {
			float dx = x2 - x1;
			float dy = y2 - y1;
			int index = 0;
			if (rc.getEdgeShapeTransformer() instanceof IndexedRendering) {
				@SuppressWarnings("unchecked")
				EdgeIndexFunction<String, String> peif = ((IndexedRendering<String, String>) rc
						.getEdgeShapeTransformer()).getEdgeIndexFunction();
				index = peif.getIndex(graph, e);
				index *= 20;
			}
			GeneralPath gp = new GeneralPath();
			gp.moveTo(0, 0);// the xform will do the translation to x1,y1
			if (x1 > x2) {
				if (y1 > y2) {
					gp.lineTo(0, index);
					gp.lineTo(dx - index, index);
					gp.lineTo(dx - index, dy);
					gp.lineTo(dx, dy);
				} else {
					gp.lineTo(0, -index);
					gp.lineTo(dx - index, -index);
					gp.lineTo(dx - index, dy);
					gp.lineTo(dx, dy);
				}

			} else {
				if (y1 > y2) {
					gp.lineTo(0, index);
					gp.lineTo(dx + index, index);
					gp.lineTo(dx + index, dy);
					gp.lineTo(dx, dy);

				} else {
					gp.lineTo(0, -index);
					gp.lineTo(dx + index, -index);
					gp.lineTo(dx + index, dy);
					gp.lineTo(dx, dy);

				}

			}

			edgeShape = gp;

		} else {
			// this is a normal edge. Rotate it to the angle between
			// vertex endpoints, then scale it to the distance between
			// the vertices
			float dx = x2 - x1;
			float dy = y2 - y1;
			float thetaRadians = (float) Math.atan2(dy, dx);
			xform.rotate(thetaRadians);
			float dist = (float) Math.sqrt(dx * dx + dy * dy);
			xform.scale(dist, 1.0);
		}
		edgeShape = xform.createTransformedShape(edgeShape);

		MutableTransformer vt = rc.getMultiLayerTransformer().getTransformer(
				Layer.VIEW);
		if (vt instanceof LensTransformer) {
			vt = ((LensTransformer) vt).getDelegate();
		}
		edgeHit = vt.transform(edgeShape).intersects(deviceRectangle);

		if (edgeHit == true) {

			Paint oldPaint = g.getPaint();

			// get Paints for filling and drawing
			// (filling is done first so that drawing and label use same Paint)
			Paint fill_paint = rc.getEdgeFillPaintTransformer().transform(e);
			if (fill_paint != null) {
				g.setPaint(fill_paint);
				g.fill(edgeShape);
			}
			Paint draw_paint = rc.getEdgeDrawPaintTransformer().transform(e);
			if (draw_paint != null) {
				g.setPaint(draw_paint);
				g.draw(edgeShape);
			}

			float scalex = (float) g.getTransform().getScaleX();
			float scaley = (float) g.getTransform().getScaleY();
			// see if arrows are too small to bother drawing
			if (scalex < .3 || scaley < .3)
				return;

			if (rc.getEdgeArrowPredicate().evaluate(
					Context.<Graph<String, String>, String> getInstance(graph,
							e))) {

				Stroke new_stroke = rc.getEdgeArrowStrokeTransformer()
						.transform(e);
				Stroke old_stroke = g.getStroke();
				if (new_stroke != null)
					g.setStroke(new_stroke);

				Shape destVertexShape = rc.getVertexShapeTransformer()
						.transform(graph.getEndpoints(e).getSecond());

				AffineTransform xf = AffineTransform.getTranslateInstance(x2,
						y2);
				destVertexShape = xf.createTransformedShape(destVertexShape);

				arrowHit = rc.getMultiLayerTransformer()
						.getTransformer(Layer.VIEW).transform(destVertexShape)
						.intersects(deviceRectangle);
				if (arrowHit) {

					AffineTransform at = edgeArrowRenderingSupport
							.getArrowTransform(rc, edgeShape, destVertexShape);
					if (at == null)
						return;
					Shape arrow = rc
							.getEdgeArrowTransformer()
							.transform(
									Context.<Graph<String, String>, String> getInstance(
											graph, e));
					arrow = at.createTransformedShape(arrow);
					g.setPaint(rc.getArrowFillPaintTransformer().transform(e));
					g.fill(arrow);
					g.setPaint(rc.getArrowDrawPaintTransformer().transform(e));
					g.draw(arrow);
				}
				// restore paint and stroke
				if (new_stroke != null)
					g.setStroke(old_stroke);

			}

			// restore old paint
			g.setPaint(oldPaint);
		}
	}
}
