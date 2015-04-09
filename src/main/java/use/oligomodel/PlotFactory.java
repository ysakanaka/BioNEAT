package use.oligomodel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PlotFactory {
	public JPanel createTimeSeriesPanel(Map<String, double[]> timeSeries) {

		XYDataset dataset = createDataset(timeSeries);
		JFreeChart chart = ChartFactory.createXYLineChart("", "Time",
				"Concentration", dataset, PlotOrientation.VERTICAL, true,
				false, false);
		final ChartPanel chartPanel = new ChartPanel(chart);
		XYPlot plot = (XYPlot) chart.getPlot();
		// plot.setDomainGridlinesVisible(false);
		// plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(Color.white);
		BasicStroke stroke = new BasicStroke(2);
		for (int i = 0; i < timeSeries.size(); i++) {
			plot.getRenderer().setSeriesStroke(i, stroke);
		}
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));

		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
		ret.add(chartPanel);
		return ret;
	}

	private XYDataset createDataset(Map<String, double[]> timeSeries) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (String seriesName : timeSeries.keySet()) {
			XYSeries series = new XYSeries(seriesName);
			double[] data = timeSeries.get(seriesName);
			for (int i = 0; i < data.length; i++) {
				series.add(i, data[i]);
			}
			dataset.addSeries(series);
		}
		return dataset;
	}
}
