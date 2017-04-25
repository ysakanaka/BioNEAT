package erne.visual;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartColor;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;

public class MultiDataScatterPlot extends FastScatterPlot {
	
	private static Paint[] colors = ChartColor.createDefaultPaintArray();
	
	protected int popSize= Integer.MAX_VALUE;
	
	protected float[][] myData;
	protected ValueAxis domainAxis;
	protected ValueAxis rangeAxis;
	
	 public MultiDataScatterPlot(float[][] data, int popSize,
             ValueAxis domainAxis, ValueAxis rangeAxis) {
		 super(data, domainAxis, rangeAxis);
		 this.popSize = popSize;
		 this.domainAxis = domainAxis;
		 this.rangeAxis = rangeAxis;
	 }
	 
	 @Override
	 public void render(Graphics2D g2, Rectangle2D dataArea,
             PlotRenderingInfo info, CrosshairState crosshairState) {
		 if (myData != null) {
	            for (int i = 0; i < myData[0].length; i++) {
	                float x = myData[0][i];
	                float y = myData[1][i];
	                
	                g2.setPaint(colors[(i/popSize)%colors.length]);
	                //int transX = (int) (xx + ww * (x - domainMin) / domainLength);
	                //int transY = (int) (yy - hh * (y - rangeMin) / rangeLength);
	                int transX = (int) this.domainAxis.valueToJava2D(x, dataArea,
	                        RectangleEdge.BOTTOM);
	                int transY = (int) this.rangeAxis.valueToJava2D(y, dataArea,
	                        RectangleEdge.LEFT);
	                g2.fillRect(transX, transY, 1000, 1000);
	            }
	        }
	 }

}
