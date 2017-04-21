package erne.visual;

import java.io.Serializable;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import erne.Population;
import erne.PopulationDisplayer;
import erne.algorithm.bioNEAT.BioNEATPopulation;
import erne.speciation.SpeciesPlotFactory;

public class BioNEATPopulationDisplayer implements PopulationDisplayer, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5301934788448751791L;
	protected transient SpeciesPlotFactory speciesFactory = new SpeciesPlotFactory();
	protected transient ChartPanel populationPanel = null;
	
	@Override
	public JPanel createPopulationPanel(Population population) {
		
		if(BioNEATPopulation.class.isAssignableFrom(population.getClass())){
			populationPanel = (ChartPanel) speciesFactory.createSpeciesPanel(((BioNEATPopulation) population).getSpeciesByGenerations());
		}
		return populationPanel;
	}

	@Override
	public JPanel createFitnessPanel(Population population) {
		JPanel fitnessVisualPanel = null;
		if(BioNEATPopulation.class.isAssignableFrom(population.getClass())){
			fitnessVisualPanel = speciesFactory.createSpeciesFitnessPanel(((BioNEATPopulation)population).getSpeciesByGenerations(),speciesFactory.getSpeciesColors(populationPanel));
		}
		return fitnessVisualPanel;
	}

}
