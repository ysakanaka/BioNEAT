package erne.visual;

import java.io.Serializable;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.category.DefaultCategoryDataset;

import erne.Individual;
import erne.Population;
import erne.PopulationDisplayer;

public class CMAESPopulationDisplayer implements PopulationDisplayer, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5284182745980845321L;
	private DefaultCategoryDataset bestEvers = new DefaultCategoryDataset();

	@Override
	public JPanel createPopulationPanel(Population population) {
		//int perGen = population.getAllPopulations().get(0).length;
		updateBestEvers(population);
		
		ChartPanel chartPanel = new ChartPanel(ChartFactory.createLineChart("Best aggregated fitness over time", "Evaluations", "Fitness", bestEvers));
		JPanel ret = new JPanel();
		ret.add(chartPanel);
		return ret;
	}

	@Override
	public JPanel createFitnessPanel(Population population) {
		System.out.println("WARNING: CMAESPopulationDisplayer: dummy createFitnessPanel");
		return new JPanel();
	}
	
	protected void updateBestEvers(Population pop){
		if(pop.getTotalGeneration() > bestEvers.getColumnCount()){
			for(int i = bestEvers.getColumnCount();i<pop.getTotalGeneration(); i++){
				Individual[] popThisTime = pop.getAllPopulations().get(i);
				double possibleBest = popThisTime[0].getFitnessResult().getFitness();
				for(int j = 1; j<popThisTime.length; j++){
					double test = popThisTime[j].getFitnessResult().getFitness();
					if(possibleBest < test) possibleBest = test;
				}
				bestEvers.addValue(possibleBest, "", ""+((i+1)*popThisTime.length));
			}
		}
	}

}
