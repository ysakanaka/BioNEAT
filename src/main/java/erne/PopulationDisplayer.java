package erne;

import javax.swing.JPanel;

public interface PopulationDisplayer {
	
	public JPanel createPopulationPanel(Population population);
	public JPanel createFitnessPanel(Population population);

}
