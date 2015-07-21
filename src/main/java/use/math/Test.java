package use.math;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import common.Static;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import reactionnetwork.Library;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.visual.RNVisualizationViewerFactory;
import use.math.gaussian.GaussianFitnessFunction;
import use.oligomodel.PlotFactory;

public class Test {

	public static void main(String[] args) {
		try {
			TestGUI window = new TestGUI();
			window.frame.setVisible(true);

			ReactionNetwork network = Library.oldGaussian;
			AbstractMathFitnessFunction.saveSimulation = true;
			FitnessResult fitnessResult = (FitnessResult) new GaussianFitnessFunction().evaluate(network);
			System.out.println(fitnessResult);
			RNVisualizationViewerFactory factory = new RNVisualizationViewerFactory();
			VisualizationViewer<String, String> vv = factory.createVisualizationViewer(network);
			window.panelTopology.add(vv, BorderLayout.CENTER);

			window.txtrTest.setText(Static.gson.toJson(network));

			Map<String, double[]> timeSeries = new HashMap<String, double[]>();
			JPanel timeSeriesPanel;
			PlotFactory plotFactory = new PlotFactory();
			if (!fitnessResult.minFitness) {
				double[] targetOutputs = new double[fitnessResult.inputs.length];
				for (int k = 0; k < targetOutputs.length; k++) {
					targetOutputs[k] = GaussianFitnessFunction.targetCoeff[0]
							* Math.exp((-Math.pow(Math.log(fitnessResult.inputs[k]) - GaussianFitnessFunction.targetCoeff[1], 2))
									/ (2 * Math.pow(GaussianFitnessFunction.targetCoeff[2], 2)));
				}
				timeSeries.put("Actual outputs", fitnessResult.actualOutputs);
				timeSeries.put("Fitted outputs", fitnessResult.targetOutputs);
				timeSeries.put("Target outputs", targetOutputs);
				double[] xData = new double[fitnessResult.inputs.length];
				for (int k = 0; k < xData.length; k++) {
					xData[k] = fitnessResult.inputs[k];
				}
				timeSeriesPanel = plotFactory.createTimeSeriesPanel(timeSeries, xData, true);
				window.tabbedPane.addTab("Matching", null, timeSeriesPanel, null);
			}
			for (Double input : AbstractMathFitnessFunction.simulationResults.keySet()) {
				timeSeries = AbstractMathFitnessFunction.simulationResults.get(input);
				timeSeriesPanel = plotFactory.createTimeSeriesPanel(timeSeries);
				window.tabbedPane.addTab(Static.df2.format(input), null, timeSeriesPanel, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
