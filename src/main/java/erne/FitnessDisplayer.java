package erne;

import java.io.Serializable;

import javax.swing.JPanel;

public interface FitnessDisplayer extends Serializable {
	public JPanel drawVisualization(AbstractFitnessResult fitnessResult);
}
