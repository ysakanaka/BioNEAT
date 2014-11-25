package reactionnetwork.visual;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.visualization.PluggableRenderer;

public class RNRenderer extends PluggableRenderer {

	public void paintInhibitedAutocatalysisEdge(Graphics2D g, InhibitionEdge i,
			int x, int y, int x1, int y1, int x2, int y2) {

		Pair endpoints = i.getEndpoints();
		Edge e = (Edge) endpoints.getSecond();
		Vertex v1 = (Vertex) e.getEndpoints().getFirst();
		Shape s2 = vertexShapeFunction.getShape(v1);
		Shape edgeShape = edgeShapeFunction.getShape(e);

		Stroke new_stroke = edgeStrokeFunction.getStroke(e);
		Stroke old_stroke = g.getStroke();
		if (new_stroke != null)
			g.setStroke(new_stroke);

		boolean edgeHit = true;
		boolean arrowHit = true;
		Rectangle deviceRectangle = null;
		if (screenDevice != null) {
			Dimension d = screenDevice.getSize();
			if (d.width <= 0 || d.height <= 0) {
				d = screenDevice.getPreferredSize();
			}
			deviceRectangle = new Rectangle(0, 0, d.width, d.height);
		}

		AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

		Rectangle2D s2Bounds = s2.getBounds2D();
		xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
		double distToInhibition = Math.sqrt((x1 - x) * (x1 - x) + (y1 - y)
				* (y1 - y));
		xform.translate((x - x1) * edgeShape.getBounds2D().getWidth() / 2
				/ distToInhibition, (y - y1)
				* edgeShape.getBounds2D().getHeight() / 2 / distToInhibition);

		edgeShape = xform.createTransformedShape(edgeShape);

		edgeHit = viewTransformer.transform(edgeShape).intersects(
				deviceRectangle);

		if (edgeHit == true) {

			Paint oldPaint = g.getPaint();

			// get Paints for filling and drawing
			// (filling is done first so that drawing and label use same Paint)
			Paint fill_paint = edgePaintFunction.getFillPaint(e);
			if (fill_paint != null) {
				g.setPaint(fill_paint);
				g.fill(edgeShape);
			}
			Paint draw_paint = edgePaintFunction.getDrawPaint(e);
			if (draw_paint != null) {
				g.setPaint(draw_paint);
				g.draw(edgeShape);
			}

			float scalex = (float) g.getTransform().getScaleX();
			float scaley = (float) g.getTransform().getScaleY();
			// see if arrows are too small to bother drawing
			if (scalex < .3 || scaley < .3)
				return;

			if (edgeArrowPredicate.evaluate(e)) {

				Shape destVertexShape = vertexShapeFunction.getShape((Vertex) e
						.getEndpoints().getSecond());
				AffineTransform xf = AffineTransform.getTranslateInstance(x2,
						y2);
				destVertexShape = xf.createTransformedShape(destVertexShape);

				arrowHit = viewTransformer.transform(destVertexShape)
						.intersects(deviceRectangle);
				if (arrowHit) {

					AffineTransform at;
					if (edgeShape instanceof GeneralPath)
						at = getArrowTransform((GeneralPath) edgeShape,
								destVertexShape);
					else
						at = getArrowTransform(new GeneralPath(edgeShape),
								destVertexShape);
					if (at == null)
						return;

					new_stroke = new BasicStroke(1.0f);
					old_stroke = g.getStroke();
					if (new_stroke != null)
						g.setStroke(new_stroke);

					Shape arrow = edgeArrowFunction.getArrow(e);
					arrow = at.createTransformedShape(arrow);
					g.setPaint(edgePaintFunction.getFillPaint(e));
					g.fill(arrow);
					g.setPaint(edgePaintFunction.getFillPaint(e));
					g.draw(arrow);
					if (new_stroke != null)
						g.setStroke(old_stroke);
				}
			}
			// use existing paint for text if no draw paint specified
			if (draw_paint == null)
				g.setPaint(oldPaint);
			String label = edgeStringer.getLabel(e);
			if (label != null) {
				labelEdge(g, e, label, x1, x2, y1, y2);
			}

			// restore old paint
			g.setPaint(oldPaint);
		}

		// restore paint and stroke
		if (new_stroke != null)
			g.setStroke(old_stroke);

	}

	@Override
	protected void drawSimpleEdge(Graphics2D g, Edge e, int x1, int y1, int x2,
			int y2) {
		Pair endpoints = e.getEndpoints();
		Vertex v1 = (Vertex) endpoints.getFirst();
		Vertex v2 = (Vertex) endpoints.getSecond();
		boolean isLoop = v1.equals(v2);
		Shape s2 = vertexShapeFunction.getShape(v2);
		Shape edgeShape = edgeShapeFunction.getShape(e);

		boolean edgeHit = true;
		boolean arrowHit = true;
		Rectangle deviceRectangle = null;
		if (screenDevice != null) {
			Dimension d = screenDevice.getSize();
			if (d.width <= 0 || d.height <= 0) {
				d = screenDevice.getPreferredSize();
			}
			deviceRectangle = new Rectangle(0, 0, d.width, d.height);
		}

		AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

		if (isLoop) {
			// this is a self-loop. scale it is larger than the vertex
			// it decorates and translate it so that its nadir is
			// at the center of the vertex.
			Rectangle2D s2Bounds = s2.getBounds2D();
			xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
			if (deviceRectangle != null) {
				double height = deviceRectangle.getHeight();
				double width = deviceRectangle.getWidth();
				double distToCenter = Math.sqrt((x1 - width / 2)
						* (x1 - width / 2) + (y1 - height / 2)
						* (y1 - height / 2));

				xform.translate(
						-(width / 2 - x1) * edgeShape.getBounds2D().getWidth()
								/ 2 / distToCenter, -(height / 2 - y1)
								* edgeShape.getBounds2D().getHeight() / 2
								/ distToCenter);
			} else {
				xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
			}
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

		edgeHit = viewTransformer.transform(edgeShape).intersects(
				deviceRectangle);

		if (edgeHit == true) {

			Paint oldPaint = g.getPaint();

			// get Paints for filling and drawing
			// (filling is done first so that drawing and label use same Paint)
			Paint fill_paint = edgePaintFunction.getFillPaint(e);
			if (fill_paint != null) {
				g.setPaint(fill_paint);
				g.fill(edgeShape);
			}
			Paint draw_paint = edgePaintFunction.getDrawPaint(e);
			if (draw_paint != null) {
				g.setPaint(draw_paint);
				g.draw(edgeShape);
			}

			float scalex = (float) g.getTransform().getScaleX();
			float scaley = (float) g.getTransform().getScaleY();
			// see if arrows are too small to bother drawing
			if (scalex < .3 || scaley < .3)
				return;

			if (edgeArrowPredicate.evaluate(e)) {

				BasicStroke new_stroke = new BasicStroke(1.0f);
				Stroke old_stroke = g.getStroke();
				if (new_stroke != null)
					g.setStroke(new_stroke);

				Shape destVertexShape = vertexShapeFunction.getShape((Vertex) e
						.getEndpoints().getSecond());
				AffineTransform xf = AffineTransform.getTranslateInstance(x2,
						y2);
				destVertexShape = xf.createTransformedShape(destVertexShape);

				arrowHit = viewTransformer.transform(destVertexShape)
						.intersects(deviceRectangle);
				if (arrowHit) {

					AffineTransform at;
					if (edgeShape instanceof GeneralPath)
						at = getArrowTransform((GeneralPath) edgeShape,
								destVertexShape);
					else
						at = getArrowTransform(new GeneralPath(edgeShape),
								destVertexShape);
					if (at == null)
						return;
					Shape arrow = edgeArrowFunction.getArrow(e);
					arrow = at.createTransformedShape(arrow);
					g.setPaint(edgePaintFunction.getFillPaint(e));
					g.fill(arrow);
					g.setPaint(edgePaintFunction.getFillPaint(e));
					g.draw(arrow);
				}
				if (old_stroke != null) {
					g.setStroke(old_stroke);
				}
			}
			// use existing paint for text if no draw paint specified
			if (draw_paint == null)
				g.setPaint(oldPaint);
			String label = edgeStringer.getLabel(e);
			if (label != null) {
				labelEdge(g, e, label, x1, x2, y1, y2);
			}

			// restore old paint
			g.setPaint(oldPaint);
		}
	}

	/**
	 * Paints <code>e</code>, whose endpoints are at <code>(x1,y1)</code> and
	 * <code>(x2,y2)</code>, on the graphics context <code>g</code>. Uses the
	 * paint and stroke specified by this instance's
	 * <code>EdgeColorFunction</code> and <code>EdgeStrokeFunction</code>,
	 * respectively. (If the paint is unspecified, the existing paint for the
	 * graphics context is used; the same applies to stroke.) The details of the
	 * actual rendering are delegated to <code>drawSelfLoop</code> or
	 * <code>drawSimpleEdge</code>, depending on the type of the edge. Note that
	 * <code>(x1, y1)</code> is the location of e.getEndpoints.getFirst() and
	 * <code>(x2, y2)</code> is the location of e.getEndpoints.getSecond().
	 * 
	 */
	public void paintInhibitionEdge(Graphics g, InhibitionEdge i, int x, int y,
			int x1, int y1, int x2, int y2) {

		// don't draw edge if either incident vertex is not drawn
		Pair endpoints = i.getEndpoints();
		Vertex v = (Vertex) endpoints.getFirst();
		Edge e = (Edge) endpoints.getSecond();
		Vertex v1 = (Vertex) e.getEndpoints().getFirst();
		Vertex v2 = (Vertex) e.getEndpoints().getSecond();
		if (!vertexIncludePredicate.evaluate(v)
				|| !vertexIncludePredicate.evaluate(v1)
				|| !vertexIncludePredicate.evaluate(v2))
			return;

		Graphics2D g2d = (Graphics2D) g;

		BasicStroke new_stroke = new BasicStroke(1.0f);
		Stroke old_stroke = g2d.getStroke();
		if (new_stroke != null)
			g2d.setStroke(new_stroke);

		drawSimpleInhibitionEdge(g2d, i, x, y, x1, y1, x2, y2);

		// restore paint and stroke
		if (new_stroke != null)
			g2d.setStroke(old_stroke);
	}

	private void drawSimpleInhibitionEdge(Graphics2D g, InhibitionEdge i,
			int x, int y, int x1, int y1, int x2, int y2) {
		Pair endpoints = i.getEndpoints();
		Edge e = (Edge) endpoints.getSecond();
		Vertex v1 = (Vertex) e.getEndpoints().getFirst();
		Vertex v2 = (Vertex) e.getEndpoints().getSecond();

		double dist12 = Math
				.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		boolean isLoop = v1.equals(v2);
		float endX, endY;
		float finalX, finalY;
		if (isLoop) {
			double distToInhibition = Math.sqrt((x1 - x) * (x1 - x) + (y1 - y)
					* (y1 - y));
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
			AffineTransform.getRotateInstance(Math.atan2(20, dist12), x1, y1)
					.transform(pt, 0, pt, 0, 1); // specifying to use
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
		AffineTransform xform = AffineTransform.getTranslateInstance(finalX,
				finalY);
		float dx = finalX - x;
		float dy = finalY - y;
		float thetaRadians = (float) Math.atan2(dy, dx);
		xform.rotate(thetaRadians + Math.PI / 2);
		xform.scale(4, 0.0);
		endShape = xform.createTransformedShape(endShape);

		Paint oldPaint = g.getPaint();

		// get Paints for filling and drawing
		// (filling is done first so that drawing and label use same Paint)
		Paint fill_paint = edgePaintFunction.getFillPaint(e);
		if (fill_paint != null) {
			g.setPaint(fill_paint);
			g.fill(edgeShape);
			g.fill(endShape);
		}
		Paint draw_paint = edgePaintFunction.getDrawPaint(e);
		if (draw_paint != null) {
			g.setPaint(draw_paint);
			g.draw(edgeShape);
			g.draw(endShape);
		}

		// restore old paint
		g.setPaint(oldPaint);
	}

	@Override
	public AffineTransform getArrowTransform(GeneralPath edgeShape,
			Shape vertexShape) {
		GeneralPath path = new GeneralPath(edgeShape);
		float[] seg = new float[6];
		Point2D p1 = null;
		Point2D p2 = null;
		AffineTransform at = new AffineTransform();
		AffineTransform firstAt = new AffineTransform();
		// when the PathIterator is done, switch to the line-subdivide
		// method to get the arrowhead closer.
		boolean out = false;
		boolean found = false;
		boolean firstFound = false;
		for (PathIterator i = path.getPathIterator(null, 1); !i.isDone(); i
				.next()) {
			int ret = i.currentSegment(seg);
			if (ret == PathIterator.SEG_MOVETO) {
				p2 = new Point2D.Float(seg[0], seg[1]);
			} else if (ret == PathIterator.SEG_LINETO) {
				p1 = p2;
				p2 = new Point2D.Float(seg[0], seg[1]);
				if (vertexShape.contains(p2)) {
					if (out) {
						at = getArrowTransform(new Line2D.Float(p1, p2),
								vertexShape);
						found = true;
						break;
					} else if (!firstFound) {
						firstAt = getArrowTransform(new Line2D.Float(p1, p2),
								vertexShape);
						firstFound = true;
					}
				} else {
					out = true;
				}
			}
		}
		if (found) {
			return at;
		} else {
			return firstAt;
		}
	}

}
