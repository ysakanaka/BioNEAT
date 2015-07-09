package erne;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.jfree.chart.ChartPanel;

import reactionnetwork.ReactionNetwork;
import reactionnetwork.visual.RNVisualizationViewerFactory;
import use.math.FitnessResult;
import xy.reflect.ui.ReflectionUI;
import cluster.Cluster;
import common.Static;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.rules.AddActivation;
import erne.mutation.rules.AddInhibition;
import erne.mutation.rules.AddNode;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import erne.speciation.Species;
import erne.speciation.SpeciesPlotFactory;
import gui.Main;
import gui.WrapLayout;

public class Evolver {
	public static Main window;
	private int popSize;
	private int maxGenerations;
	private ReactionNetwork startingNetwork;
	private AbstractFitnessFunction fitnessFunction;
	private Mutator mutator;
	private FitnessDisplayer fitnessDisplayer;

	public Evolver(int popSize, int maxGenerations, ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction,
			Mutator mutator, FitnessDisplayer fitnessDisplayer) {
		this.popSize = popSize;
		this.maxGenerations = maxGenerations;
		this.startingNetwork = startingNetwork;
		this.fitnessFunction = fitnessFunction;
		this.mutator = mutator;
		this.fitnessDisplayer = fitnessDisplayer;
	}

	public void evolve() throws InterruptedException, ExecutionException {
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

		Population population = new Population(popSize, startingNetwork);
		population.setFitnessFunction(fitnessFunction);
		if (mutator == null) {
			mutator = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] { new DisableTemplate(1),
					new MutateParameter(90), new AddNode(2), new AddActivation(2), new AddInhibition(5) })));
		}
		population.setMutator(mutator);

		for (int i = 0; i < maxGenerations; i++) {
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
			panelGeneration.add(reflectionUI.createObjectForm(populationInfo), BorderLayout.SOUTH);

			JPanel panelSpecies = new JPanel();
			panelSpecies.setLayout(new WrapLayout());
			panelSpecies.setSize(new Dimension(300, 1));
			panelGeneration.add(panelSpecies, BorderLayout.NORTH);
			RNVisualizationViewerFactory factory = new RNVisualizationViewerFactory();

			Species[] species = populationInfo.getSpecies();

			for (int j = 0; j < species.length; j++) {
				FitnessResult fitnessResult = (FitnessResult) species[j].getBestIndividual().getFitnessResult();
				System.out.println("Species " + species[j].getName() + " Fitness " + fitnessResult.getFitness());
				System.out.println(species[j].getBestIndividual().getNetwork());
				VisualizationViewer<String, String> vv = factory.createVisualizationViewer(species[j].getBestIndividual().getNetwork());

				JPanel panelSpecie = new JPanel();
				panelSpecie.setLayout(new BorderLayout(0, 0));
				panelSpecie.add(
						new JLabel("Species " + species[j].getName() + " Fitness: "
								+ Static.df4.format(species[j].getBestIndividual().getFitnessResult().getFitness())), BorderLayout.NORTH);
				panelSpecie.add(vv, BorderLayout.CENTER);
				panelSpecie.add(fitnessDisplayer.drawVisualization(fitnessResult), BorderLayout.SOUTH);
				panelSpecies.add(panelSpecie);
			}

			SpeciesPlotFactory speciesFactory = new SpeciesPlotFactory();
			JPanel speciesVisualPanel = speciesFactory.createSpeciesPanel(population.getSpeciesByGenerations());
			window.getPanelSpecies().removeAll();
			window.getPanelSpecies().add(speciesVisualPanel, BorderLayout.CENTER);
			window.getPanelSpecies().revalidate();

			window.getPanelFitness().removeAll();
			window.getPanelFitness().add(
					speciesFactory.createSpeciesFitnessPanel(population.getSpeciesByGenerations(),
							speciesFactory.getSpeciesColors((ChartPanel) speciesVisualPanel)));
			window.getPanelFitness().revalidate();
		}
	}
}
