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

			ReactionNetwork network = Library.dummyNetwork;
			AbstractMathFitnessFunction.setSaveSimulation(true);
			FitnessResult fitnessResult = (FitnessResult) new GaussianFitnessFunction().evaluate(network);
//			FitnessResult fitnessResult = new FitnessResult(true);
			System.out.println(fitnessResult);
			window.displayEvaluationResults(network, fitnessResult);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
