package use.math.step;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.math3.special.Erf;

import erne.AbstractFitnessResult;
import erne.FitnessDisplayer;
import model.PlotFactory;
import use.math.FitnessResult;

public class StepFitnessDisplayer implements FitnessDisplayer {

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
				int offset = (StepFitnessFunction.hysteresis && k >= targetOutputs.length/2)?StepFitnessFunction.hystCoeff.length/2:0;
				double[] coeffs = (StepFitnessFunction.hysteresis?(StepFitnessFunction.diff?StepFitnessFunction.hystdiffCoeff:StepFitnessFunction.hystCoeff):StepFitnessFunction.targetCoeff);
				targetOutputs[k] = coeffs[0+offset]*(0.5+0.5*Erf.erf((fitnessResult.inputs[k]-coeffs[1+offset])/coeffs[3+offset]))+coeffs[2+offset];
			}
			Map<String, double[]> timeSeries = new HashMap<String, double[]>();
			timeSeries.put("Actual outputs", fitnessResult.actualOutputs);
			timeSeries.put("Fitted outputs", fitnessResult.targetOutputs);
			timeSeries.put("Target outputs", targetOutputs);
			double[] xData = new double[fitnessResult.inputs.length];
			for (int k = 0; k < xData.length; k++) {
				xData[k] = fitnessResult.inputs[k];
			}
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.add(new PlotFactory().createTimeSeriesPanel(timeSeries, xData, false, "Input concentration", "Output concentration"));
			panel.add(new JLabel(Arrays.toString(fitnessResult.actualFittingParams)));
			return panel;
		} else {
			return new JPanel();
		}
	}

}
