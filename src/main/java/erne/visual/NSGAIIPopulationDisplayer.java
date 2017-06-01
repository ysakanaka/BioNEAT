package erne.visual;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import erne.Individual;
import erne.MultiobjectiveAbstractFitnessResult;
import erne.Population;
import erne.PopulationDisplayer;
import erne.SimpleMultiobjectiveFitnessResult;
import erne.algorithm.nsgaII.NSGAIIPopulationFactory;
import reactionnetwork.Library;
import use.math.FitnessResult;

public class NSGAIIPopulationDisplayer implements PopulationDisplayer, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DefaultXYDataset dataset = new DefaultXYDataset();
	private DefaultCategoryDataset bestEvers = new DefaultCategoryDataset();

	@Override
	public JPanel createPopulationPanel(Population population) {
		int perGen = population.getAllPopulations().get(0).length;
		updateBestEvers(population);
		
		ChartPanel chartPanel = new ChartPanel(ChartFactory.createLineChart("Best aggregated fitness over time", "Evaluations", "Fitness", bestEvers));
		JPanel ret = new JPanel();
		ret.add(chartPanel);
		return ret;
	}

	@Override
	public JPanel createFitnessPanel(Population population) {
		int perGen = population.getAllPopulations().get(0).length;
		//MultiDataScatterPlot panel = new MultiDataScatterPlot(getData(perGen, population), perGen ,new NumberAxis("Param 1"),new NumberAxis("Param 2"));
		
		updateDataset(population);
		
		JPanel ret = new JPanel();
		ChartPanel chartPanel = new ChartPanel(ChartFactory.createScatterPlot("Pareto solutions", "Param1","Param 2", dataset));
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 800));
        chartPanel.setMinimumSize(new java.awt.Dimension(500, 300));
		ret.add(chartPanel);
		return ret;
	}
	
	protected float[][] getData(int perGen, Population pop){
		float[][] allData = new float[2][pop.getTotalGeneration()+1];
		
		for(int i = 0; i<allData.length; i++){
			int gen = i/perGen;
			int indiv = i%perGen;
			Individual individ = pop.getAllPopulations().get(gen)[indiv];
			allData[0][i] = (float) MultiobjectiveAbstractFitnessResult.getIthFitness(0,individ.getFitnessResult());
			allData[1][i] = (float) MultiobjectiveAbstractFitnessResult.getIthFitness(1,individ.getFitnessResult());
		}
		
		return allData;
	}
	
	protected void updateDataset(Population pop){
		
		if(pop.getTotalGeneration() > dataset.getSeriesCount()){
			for(int i = dataset.getSeriesCount(); i<pop.getTotalGeneration(); i++){
				double[][] data = new double[2][pop.getAllPopulations().get(i).length];
				
				for(int j = 0; j<data[0].length; j++){
					Individual individ = pop.getAllPopulations().get(i)[j];
					data[0][j] =  MultiobjectiveAbstractFitnessResult.getIthFitness(0,individ.getFitnessResult());
					data[1][j] =  MultiobjectiveAbstractFitnessResult.getIthFitness(1,individ.getFitnessResult());
				}
				dataset.addSeries(i, data);
			}
		}
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
	
	public static void main(String[] args){
		int popSize = 10;
		
		JFrame frame = new JFrame("Scatterplot test");
		NSGAIIPopulationDisplayer displayer = new NSGAIIPopulationDisplayer();
		Population pop = (new NSGAIIPopulationFactory()).createPopulation(popSize, Library.dummyNetwork);
		
		for(int i = 0; i<popSize; i++){
			ArrayList<Double> fullFit = new ArrayList<Double>();
			fullFit.add(0.1*i);
			fullFit.add(10.0 - 0.1*i);
			SimpleMultiobjectiveFitnessResult result = new SimpleMultiobjectiveFitnessResult(fullFit);
			
			pop.getAllPopulations().get(0)[i] = new Individual(Library.dummyNetwork);
			pop.getAllPopulations().get(0)[i].setFitnessResult(result);
		}
		
		frame.add(displayer.createFitnessPanel(pop));
		frame.pack();
		frame.setVisible(true);
	}

}
