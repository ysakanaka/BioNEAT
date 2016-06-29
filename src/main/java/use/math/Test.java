package use.math;




import reactionnetwork.Library;
import reactionnetwork.ReactionNetwork;
import use.math.gaussian.GaussianFitnessFunction;


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
