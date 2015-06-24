package use.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import use.oligomodel.OligoSystemComplex;
import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;

public abstract class AbstractMathFitnessFunction extends AbstractFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int nInputs = 1;

	double minInputValue = 1;
	double maxInputValue = 50;
	int nTests = 10;

	String INPUT_SEQUENCE = "a";
	String OUTPUT_SEQUENCE = "b";
	String REPORTER_OUTPUT_SEQUENCE = "Reporter b";

	@Override
	public AbstractFitnessResult minFitness() {
		return new FitnessResult(true);
	}

	@Override
	public AbstractFitnessResult evaluate(ReactionNetwork network) {
		try {
			// input sequence is protected
			network.getNodeByName(INPUT_SEQUENCE).protectedSequence = true;

			// output sequence is reported
			network.getNodeByName(OUTPUT_SEQUENCE).reporter = true;

			ArrayList<double[]> tests = getInputValues(minInputValue, maxInputValue, nTests, nInputs);
			double[] actualOutputs = new double[nTests];

			Map<String, Double> sequencesLastTest = new HashMap<String, Double>();
			for (int i = 0; i < tests.size(); i++) {
				double[] inputs = tests.get(i);

				// Yannick's idea: set init concentration as last test's stable
				// state.
				for (Node node : network.nodes) {
					if (sequencesLastTest.containsKey(node.name)) {
						node.initialConcentration = sequencesLastTest.get(node.name);
					}
				}
				network.getNodeByName(INPUT_SEQUENCE).initialConcentration = inputs[0];
				OligoSystemComplex oligoSystem = new OligoSystemComplex(network);
				Map<String, double[]> timeSeries = oligoSystem.calculateTimeSeries(30);
				for (String sequence : timeSeries.keySet()) {
					double[] outputTimeSeries = timeSeries.get(sequence);
					double value = outputTimeSeries[outputTimeSeries.length - 1];
					sequencesLastTest.put(sequence, value);
					if (sequence.equals(REPORTER_OUTPUT_SEQUENCE)) {
						actualOutputs[i] = value;
					}
				}
			}
			return calculateFitnessResult(tests, actualOutputs);

		} catch (Exception e) {
			return minFitness();
		}
	}

	protected abstract FitnessResult calculateFitnessResult(ArrayList<double[]> tests, double[] actualOutputs);

	private static ArrayList<double[]> getInputValues(double minInputValue, double maxInputValue, int nTests, int nInputs) {
		ArrayList<double[]> result = new ArrayList<double[]>();
		double step = (maxInputValue - minInputValue) / (nTests - 1);
		double[] inputs = new double[nInputs];
		getInputs(0, step, nTests, minInputValue, inputs, result);
		return result;
	}

	private static void getInputs(int firstPos, double step, int n, double min, double[] inputs, ArrayList<double[]> result) {
		for (int i = 0; i < n; i++) {
			inputs[firstPos] = min + i * step;
			if (firstPos == inputs.length - 1) {
				result.add(inputs.clone());
			} else {
				getInputs(firstPos + 1, step, n, min, inputs, result);
			}
		}
	}

}
