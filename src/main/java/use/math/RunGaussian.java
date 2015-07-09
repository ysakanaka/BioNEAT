package use.math;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;

import reactionnetwork.Library;
import use.oligomodel.PlotFactory;
import erne.AbstractFitnessResult;
import erne.Evolver;
import erne.FitnessDisplayer;

public class RunGaussian {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		GaussianFitnessFunction fitnessFunction = new GaussianFitnessFunction();
		Evolver evolver = new Evolver(500, 200, Library.startingMath, fitnessFunction, null, new FitnessDisplayer() {

			@Override
			public JPanel drawVisualization(AbstractFitnessResult fitness) {
				FitnessResult fitnessResult = (FitnessResult) fitness;
				if (!fitnessResult.minFitness) {
					double[] targetOutputs = new double[fitnessResult.inputs.length];
					for (int k = 0; k < targetOutputs.length; k++) {
						if (GaussianFitnessFunction.logScale) {
							targetOutputs[k] = GaussianFitnessFunction.targetCoeff[0]
									* Math.exp((-Math.pow(Math.log(fitnessResult.inputs[k]) - GaussianFitnessFunction.targetCoeff[1], 2))
											/ (2 * Math.pow(GaussianFitnessFunction.targetCoeff[2], 2)));
						} else {
							targetOutputs[k] = GaussianFitnessFunction.targetCoeff[0]
									* Math.exp((-Math.pow(fitnessResult.inputs[k] - GaussianFitnessFunction.targetCoeff[1], 2))
											/ (2 * Math.pow(GaussianFitnessFunction.targetCoeff[2], 2)));
						}

					}
					Map<String, double[]> timeSeries = new HashMap<String, double[]>();
					timeSeries.put("Actual outputs", fitnessResult.actualOutputs);
					timeSeries.put("Fitted outputs", fitnessResult.targetOutputs);
					timeSeries.put("Target outputs", targetOutputs);
					double[] xData = new double[fitnessResult.inputs.length];
					for (int k = 0; k < xData.length; k++) {
						xData[k] = fitnessResult.inputs[k];
					}
					return new PlotFactory().createTimeSeriesPanel(timeSeries, xData, GaussianFitnessFunction.logScale);
				} else {
					return new JPanel();
				}
			}
		});
		evolver.evolve();
	}
}
