package use.math;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import cluster.Cluster;
import reactionnetwork.Library;
import reactionnetwork.visual.RNVisualizationViewerFactory;
import use.oligomodel.PlotFactory;
import xy.reflect.ui.ReflectionUI;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import erne.Population;
import erne.PopulationInfo;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.rules.AddActivation;
import erne.mutation.rules.AddInhibition;
import erne.mutation.rules.AddNode;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import erne.speciation.Species;
import gui.Main;
import gui.WrapLayout;

public class Run {

	public static Main window;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new Main();
					window.getMainForm().setVisible(true);
					Cluster.bindProgressBar(window.getProgressBar());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		ReflectionUI reflectionUI = new ReflectionUI();

		Population population = new Population(500, Library.startingMath);
		population.setFitnessFunction(new GaussianFitnessFunction());
		population.setMutator(new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] { new DisableTemplate(1),
				new MutateParameter(90), new AddNode(2), new AddActivation(2), new AddInhibition(5) }))));
		for (int i = 0; i < 10000; i++) {
			if (i == 0) {
				population.resetPopulation();
			} else {
				population.evolve();
			}
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			window.getTabHistory().addTab("Gen " + i, null, scrollPane, null);

			JPanel panelGeneration = new JPanel();
			scrollPane.setViewportView(panelGeneration);
			panelGeneration.setLayout(new BorderLayout(0, 0));
			PopulationInfo populationInfo = population.getPopulationInfo(i);
			panelGeneration.add(reflectionUI.createObjectForm(populationInfo), BorderLayout.NORTH);

			JPanel panelSpecies = new JPanel();
			panelSpecies.setLayout(new WrapLayout());
			panelSpecies.setSize(new Dimension(300, 1));
			panelGeneration.add(panelSpecies, BorderLayout.CENTER);
			RNVisualizationViewerFactory factory = new RNVisualizationViewerFactory();

			Species[] species = populationInfo.getSpecies();

			for (int j = 0; j < species.length; j++) {
				FitnessResult fitnessResult = (FitnessResult) species[j].getBestIndividual().getFitnessResult();
				System.out.println("Species " + species[j].getName() + " Fitness " + fitnessResult.getFitness());
				System.out.println(species[j].getBestIndividual().getNetwork());
				VisualizationViewer<String, String> vv = factory.createVisualizationViewer(species[j].getBestIndividual().getNetwork());

				JPanel panelSpecie = new JPanel();
				panelSpecie.setLayout(new BorderLayout(0, 0));
				panelSpecie.add(new JLabel("Species " + species[j].getName()), BorderLayout.NORTH);
				panelSpecie.add(vv, BorderLayout.CENTER);
				if (!fitnessResult.minFitness) {
					Map<String, double[]> timeSeries = new HashMap<String, double[]>();
					timeSeries.put("Actual outputs", fitnessResult.actualOutputs);
					timeSeries.put("Target outputs", fitnessResult.targetOutputs);
					double[] xData = new double[fitnessResult.inputs.length];
					for (int k = 0; k < xData.length; k++) {
						xData[k] = fitnessResult.inputs[k];
					}
					panelSpecie.add(new PlotFactory().createTimeSeriesPanel(timeSeries, xData, true), BorderLayout.SOUTH);
				}
				panelSpecies.add(panelSpecie);
			}
		}

	}
}
