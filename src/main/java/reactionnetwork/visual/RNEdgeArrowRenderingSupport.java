package reactionnetwork.visual;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeArrowRenderingSupport;

public class RNEdgeArrowRenderingSupport extends
		BasicEdgeArrowRenderingSupport<String, String> {
	@Override
	public AffineTransform getArrowTransform(RenderContext<String, String> rc,
			Shape edgeShape, Shape vertexShape) {
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
						at = getArrowTransform(rc, new Line2D.Float(p1, p2),
								vertexShape);
						found = true;
						break;
					} else if (!firstFound) {
						firstAt = getArrowTransform(rc,
								new Line2D.Float(p1, p2), vertexShape);
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
