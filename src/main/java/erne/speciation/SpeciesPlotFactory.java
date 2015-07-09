package erne.speciation;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import erne.Individual;
import reactionnetwork.Library;

public class SpeciesPlotFactory {
	public JPanel createSpeciesPanel(ArrayList<Species[]> speciesByGeneration) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < speciesByGeneration.size(); i++) {
			Species[] species = speciesByGeneration.get(i);
			for (int j = 0; j < species.length; j++) {
				dataset.addValue(species[j].individuals.size(), "Species " + species[j].representative.getId(),
						"Generation " + String.valueOf(i));
			}
		}

		JFreeChart chart = createChart(dataset);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		return chartPanel;
	}

	private JFreeChart createStackedAreaChart(String title, String categoryAxisLabel, String valueAxisLabel, CategoryDataset dataset,
			PlotOrientation orientation, boolean legend, boolean tooltips, boolean urls) {

		ParamChecks.nullNotPermitted(orientation, "orientation");
		CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
		categoryAxis.setCategoryMargin(0.0);
		categoryAxis.setTickLabelsVisible(false);
		ValueAxis valueAxis = new NumberAxis(valueAxisLabel);

		StackedAreaRenderer renderer = new StackedAreaRenderer();
		if (tooltips) {
			renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
		}
		if (urls) {
			renderer.setBaseItemURLGenerator(new StandardCategoryURLGenerator());
		}

		CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
		plot.setOrientation(orientation);
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
		ChartFactory.getChartTheme().apply(chart);
		return chart;

	}

	private JFreeChart createChart(CategoryDataset dataset) {

		final JFreeChart chart = createStackedAreaChart("", // chart
															// title
				"Generation", // domain axis label
				"Species Population", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, false);

		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setForegroundAlpha(0.5f);
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);

		// change the auto tick unit selection to integer units only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		return chart;

	}

	public static void main(final String[] args) {

		JPanel panel = new JPanel();
		JFrame frame = new JFrame("");
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1500, 600);
		ArrayList<Species[]> speciesByGeneration = new ArrayList<Species[]>();
		Species[] species = new Species[3];
		species[0] = new Species(new Individual(Library.startingMath));
		for (int i = 0; i < 25; i++) {
			species[0].individuals.add(new Individual(Library.startingMath));
		}
		species[1] = new Species(new Individual(Library.startingMath));
		for (int i = 0; i < 10; i++) {
			species[1].individuals.add(new Individual(Library.startingMath));
		}
		species[2] = new Species(new Individual(Library.startingMath));
		for (int i = 0; i < 15; i++) {
			species[2].individuals.add(new Individual(Library.startingMath));
		}
		speciesByGeneration.add(species);

		species = new Species[4];
		species[0] = new Species(speciesByGeneration.get(0)[0].representative);
		for (int i = 0; i < 10; i++) {
			species[0].individuals.add(new Individual(Library.startingMath));
		}
		species[1] = new Species(speciesByGeneration.get(0)[1].representative);
		for (int i = 0; i < 15; i++) {
			species[1].individuals.add(new Individual(Library.startingMath));
		}
		species[2] = new Species(speciesByGeneration.get(0)[2].representative);
		for (int i = 0; i < 15; i++) {
			species[2].individuals.add(new Individual(Library.startingMath));
		}
		species[3] = new Species(new Individual(Library.startingMath));
		for (int i = 0; i < 10; i++) {
			species[3].individuals.add(new Individual(Library.startingMath));
		}
		speciesByGeneration.add(species);

		species = new Species[3];
		species[0] = new Species(speciesByGeneration.get(1)[1].representative);
		for (int i = 0; i < 15; i++) {
			species[0].individuals.add(new Individual(Library.startingMath));
		}
		species[1] = new Species(speciesByGeneration.get(1)[2].representative);
		for (int i = 0; i < 20; i++) {
			species[1].individuals.add(new Individual(Library.startingMath));
		}
		species[2] = new Species(speciesByGeneration.get(1)[3].representative);
		for (int i = 0; i < 15; i++) {
			species[2].individuals.add(new Individual(Library.startingMath));
		}
		speciesByGeneration.add(species);

		species = new Species[3];
		species[0] = new Species(speciesByGeneration.get(2)[0].representative);
		for (int i = 0; i < 15; i++) {
			species[0].individuals.add(new Individual(Library.startingMath));
		}
		species[1] = new Species(speciesByGeneration.get(2)[1].representative);
		for (int i = 0; i < 20; i++) {
			species[1].individuals.add(new Individual(Library.startingMath));
		}
		species[2] = new Species(speciesByGeneration.get(1)[0].representative);
		for (int i = 0; i < 15; i++) {
			species[2].individuals.add(new Individual(Library.startingMath));
		}
		speciesByGeneration.add(species);

		panel.add(new SpeciesPlotFactory().createSpeciesPanel(speciesByGeneration));
		frame.setVisible(true);
		;

	}
}
