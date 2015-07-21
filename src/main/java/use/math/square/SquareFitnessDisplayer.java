package use.math.square;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import use.math.FitnessResult;
import use.oligomodel.PlotFactory;
import erne.AbstractFitnessResult;
import erne.FitnessDisplayer;

public class SquareFitnessDisplayer implements FitnessDisplayer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public JPanel drawVisualization(AbstractFitnessResult fitness) {
		FitnessResult fitnessResult = (FitnessResult) fitness;
		if (!fitnessResult.minFitness) {
			double[] targetOutputs = new double[fitnessResult.inputs.length];
			for (int k = 0; k < targetOutputs.length; k++) {
				targetOutputs[k] = SquareFitnessFunction.targetCoeff[0]*Math.pow(fitnessResult.inputs[k], 2);
			}
			Map<String, double[]> timeSeries = new HashMap<String, double[]>();
			timeSeries.put("Actual outputs", fitnessResult.actualOutputs);
			timeSeries.put("Fitted outputs", fitnessResult.targetOutputs);
			timeSeries.put("Target outputs", targetOutputs);
			double[] xData = new double[fitnessResult.inputs.length];
			for (int k = 0; k < xData.length; k++) {
				xData[k] = fitnessResult.inputs[k];
			}
			return new PlotFactory().createTimeSeriesPanel(timeSeries, xData, false);
		} else {
			return new JPanel();
		}
	}

}
