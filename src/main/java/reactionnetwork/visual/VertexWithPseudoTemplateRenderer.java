package reactionnetwork.visual;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.BasicVertexRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class VertexWithPseudoTemplateRenderer extends BasicVertexRenderer<String, String> {
	
	private Paint superFill = Color.cyan;
	
	protected RNGraph graph;
	
	public VertexWithPseudoTemplateRenderer(RNGraph graph){
		this.graph = graph;
	}
	
	@Override
	protected void paintIconForVertex(RenderContext<String,String> rc, String v, Layout<String,String> layout) {
        GraphicsDecorator g = rc.getGraphicsContext();
        boolean vertexHit = true;
        // get the shape to be rendered
        Shape shape = rc.getVertexShapeTransformer().apply(v);
        
        Point2D p = layout.apply(v);
        p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        float x = (float)p.getX();
        float y = (float)p.getY();
        
        
        
        
        // create a transform that translates to the location of
        // the vertex to be rendered
        AffineTransform xform = AffineTransform.getTranslateInstance(x,y);
        // transform the vertex shape with xtransform
        shape = xform.createTransformedShape(shape);
        
       
        
        vertexHit = vertexHit(rc, shape);
            //rc.getViewTransformer().transform(shape).intersects(deviceRectangle);

        if (vertexHit) {
        	if(rc.getVertexIconTransformer() != null) {
        		Icon icon = rc.getVertexIconTransformer().apply(v);
        		if(icon != null) {
        			paintShapeForVertex(rc, v, shape);
           			g.draw(icon, rc.getScreenDevice(), shape, (int)x, (int)y);

        		} else {
        			paintShapeForVertex(rc, v, shape);
        		}
        	} else {
        		paintShapeForVertex(rc, v, shape);
        	}
        	if(graph.hasPseudoTemplate(v)){
        		
        		AffineTransform reduce = AffineTransform.getScaleInstance(0.2, 0.2);
        		Shape other=null;
        		try {
					other = xform.createInverse().createTransformedShape(shape);
				} catch (NoninvertibleTransformException e) {
					e.printStackTrace();
				}
				other = reduce.createTransformedShape(other);
        		paintInnerGraph(rc,v,xform.createTransformedShape(other));
        	}
        }
    }
	
	
	
	protected void paintInnerGraph(RenderContext<String,String> rc, String v, Shape shape){
		if(shape==null){
			return;
		}
		
		AffineTransform bform = AffineTransform.getTranslateInstance(15, 15);
		Shape b = bform.createTransformedShape(shape);
		GraphicsDecorator g = rc.getGraphicsContext();
		Paint oldPaint = g.getPaint();
        Paint drawPaint = rc.getVertexDrawPaintTransformer().apply(v);
        Stroke oldStroke = g.getStroke();
    	Stroke stroke = rc.getVertexStrokeTransformer().apply(v);
    	if(stroke != null) {
    		g.setStroke(stroke);
    	}
		
        	
        	g.setPaint(superFill);
        	
    		//AffineTransform wtrans = AffineTransform.getScaleInstance(0.2, 0.2);
    		//Shape other = wtrans.createTransformedShape(shape);
    		
    		g.fill(b);
    		g.setPaint(drawPaint);
    		
    		g.draw(b);
    		int x = shape.getBounds().x;
    		int y = shape.getBounds().y;
    		
    		g.drawArc(x+10, y, 20, 20, 180, 60);
        
		
		g.setPaint(oldPaint);
    	g.setStroke(oldStroke);
	}

}
