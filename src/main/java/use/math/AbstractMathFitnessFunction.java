package use.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

	double minInputValue = 0.1;
	double maxInputValue = 100;
	int nTests = 11;

	String INPUT_SEQUENCE = "a";
	String OUTPUT_SEQUENCE = "b";
	String REPORTER_OUTPUT_SEQUENCE = "Reporter b";

	static boolean saveSimulation = false;

	public static Map<Double, Map<String, double[]>> simulationResults;

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

			// calculate inhK using 7_6 rules
			for (Node node : network.nodes) {
				if (node.type == Node.INHIBITING_SEQUENCE) {
					for (Node n1 : network.nodes) {
						for (Node n2 : network.nodes) {
							if (node.name.equals("I" + n1.name + n2.name)) {
								node.parameter = (double) 1 / 100 * Math.exp((Math.log(n1.parameter) + Math.log(n2.parameter)) / 2);
							}
						}
					}
				}
			}

			// get list of input values for different tests
			ArrayList<double[]> tests = getInputs(minInputValue, maxInputValue, nTests, nInputs);

			double[] actualOutputs = new double[nTests];

			Map<String, Double> sequencesLastTest = new HashMap<String, Double>();

			if (saveSimulation) {
				simulationResults = new TreeMap<Double, Map<String, double[]>>();
			}

			boolean minFitness = false;
			for (int i = 0; i < tests.size(); i++) {
				double[] inputs = tests.get(i);

				// Yannick's idea: set init concentration as last test's stable
				// state.
				for (Node node : network.nodes) {
					if (sequencesLastTest.containsKey(node.name)) {
						if (!node.name.equals(INPUT_SEQUENCE)) {
							node.initialConcentration = sequencesLastTest.get(node.name);
						}
					} else {
						node.initialConcentration = 10;
					}
				}
				network.getNodeByName(INPUT_SEQUENCE).initialConcentration = inputs[0];
				OligoSystemComplex oligoSystem = new OligoSystemComplex(network);
				Map<String, double[]> timeSeries = oligoSystem.calculateTimeSeries(30);

				// System could not reach stable time before 1000 minutes
				if (timeSeries.entrySet().iterator().next().getValue().length >= 1000) {
					minFitness = true;
					// Stop evaluation if we don't need to store timeseries
					if (!saveSimulation) {
						return minFitness();
					}
				}

				if (saveSimulation) {
					simulationResults.put(inputs[0], timeSeries);

					// if we have enough timeseries and fitness should be
					// minimum
					if (i == tests.size() - 1 && minFitness) {
						return minFitness();
					}
				}

				for (String sequence : timeSeries.keySet()) {
					double[] outputTimeSeries = timeSeries.get(sequence);
					double value = outputTimeSeries[outputTimeSeries.length - 1];
					sequencesLastTest.put(sequence, value);
					if (sequence.equals(REPORTER_OUTPUT_SEQUENCE)) {
						actualOutputs[i] = value;
					}
				}
			}
			FitnessResult result = calculateFitnessResult(tests, actualOutputs);
			double[] inputs = new double[tests.size()];
			for (int i = 0; i < tests.size(); i++) {
				inputs[i] = tests.get(i)[0];
			}
			result.inputs = inputs;
			return result;

		} catch (Exception e) {
			return minFitness();
		}
	}

	protected abstract FitnessResult calculateFitnessResult(ArrayList<double[]> tests, double[] actualOutputs);

	protected ArrayList<double[]> getInputs(double minInputValue, double maxInputValue, int nTests, int nInputs) {
		return getInputValues(minInputValue, maxInputValue, nTests, nInputs);
	}

	protected static ArrayList<double[]> getLogScaleInputValues(double minInputValue, double maxInputValue, int nTests, int nInputs) {
		ArrayList<double[]> result = new ArrayList<double[]>();
		double step = (Math.log(maxInputValue) - Math.log(minInputValue)) / (nTests - 1);
		double[] inputs = new double[nInputs];
		getInputs(0, step, nTests, Math.log(minInputValue), inputs, result);
		for (double[] results : result) {
			for (int i = 0; i < results.length; i++) {
				results[i] = Math.exp(results[i]);
			}
		}
		return result;
	}

	protected static ArrayList<double[]> getInputValues(double minInputValue, double maxInputValue, int nTests, int nInputs) {
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
