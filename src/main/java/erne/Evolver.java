package erne;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import erne.util.Serializer;
import gui.Main;
import gui.WrapLayout;

public class Evolver implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static transient Main window;
	private int popSize;
	private int maxGenerations;
	private ReactionNetwork startingNetwork;
	private AbstractFitnessFunction fitnessFunction;
	private Mutator mutator;
	private FitnessDisplayer fitnessDisplayer;
	private transient String resultDirectory;

	private static final int DEFAULT_POP_SIZE = 50;
	private static final int MAX_GENERATIONS = 200;
	private static final Mutator DEFAULT_MUTATOR = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {
			new DisableTemplate(1), new MutateParameter(90), new AddNode(2), new AddActivation(2), new AddInhibition(5) })));
	private static final FitnessDisplayer DEFAULT_FITNESS_DISPLAYER = new DefaultFitnessDisplayer();

	private transient boolean readerMode = false;
	private static ReflectionUI reflectionUI = new ReflectionUI();

	// Reader mode
	public void setReader(String resultDirectory) {
		this.resultDirectory = resultDirectory;
		this.readerMode = true;
	}

	public Evolver(ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction) throws IOException {
		this(DEFAULT_POP_SIZE, MAX_GENERATIONS, startingNetwork, fitnessFunction, DEFAULT_MUTATOR, DEFAULT_FITNESS_DISPLAYER);
	}

	public Evolver(ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction, FitnessDisplayer fitnessDisplayer)
			throws IOException {
		this(DEFAULT_POP_SIZE, MAX_GENERATIONS, startingNetwork, fitnessFunction, DEFAULT_MUTATOR, fitnessDisplayer);
	}

	public Evolver(int popSize, int maxGenerations, ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction,
			Mutator mutator, FitnessDisplayer fitnessDisplayer) throws IOException {
		this.popSize = popSize;
		this.maxGenerations = maxGenerations;
		this.startingNetwork = startingNetwork;
		this.fitnessFunction = fitnessFunction;
		this.mutator = mutator;
		this.fitnessDisplayer = fitnessDisplayer;
	}

	private String createResultDirectory() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		String datetime = dateFormat.format(date);
		String resultDirectory = "result/" + fitnessFunction.getClass().getSimpleName() + "_" + datetime;
		return (new File(resultDirectory).mkdirs()) ? resultDirectory : null;
	}

	public void evolve() throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		if (readerMode) {
			System.out.println("ERNe version: " + Serializer.deserialize(resultDirectory + "/version"));
			Evolver savedEvolver = (Evolver) Serializer.deserialize(resultDirectory + "/evolver");
			System.out.println("Inspect this object for evolution details: " + savedEvolver);
		} else {
			if ((resultDirectory = createResultDirectory()) == null) {
				System.out.println("Cannot create result directory");
				return;
			}
			Serializer.serialize(resultDirectory + "/version", Version.version);
			Serializer.serialize(resultDirectory + "/evolver", this);
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new Main();
					if (!readerMode) {
						window.getMainForm().setVisible(true);
					}
					Cluster.bindProgressBar(window.getProgressBar());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Population population;
		if (readerMode) {
			population = (Population) Serializer.deserialize(resultDirectory + "/population");
		} else {
			population = new Population(popSize, startingNetwork);
			population.setFitnessFunction(fitnessFunction);
			if (mutator == null) {
				mutator = DEFAULT_MUTATOR;
			}
			population.setMutator(mutator);
		}

		for (int i = 0; i < (readerMode ? population.getTotalGeneration() : maxGenerations); i++) {
			System.out.println("Processing generation " + i);
			if (!readerMode) {
				if (i == 0) {
					population.resetPopulation();
				} else {
					population.evolve();
				}
				Serializer.serialize(resultDirectory + "/population", population);
			}

			displayPopulation(i, population);
		}
		System.out.println("Done!");
		if (readerMode) {
			window.getMainForm().setVisible(true);
		}
	}

	private void displayPopulation(int generation, Population population) {
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		window.getTabHistory().addTab("Gen " + generation, null, scrollPane, null);

		JPanel panelGeneration = new JPanel();
		scrollPane.setViewportView(panelGeneration);
		panelGeneration.setLayout(new BorderLayout(0, 0));
		PopulationInfo populationInfo = population.getPopulationInfo(generation);
		panelGeneration.add(reflectionUI.createObjectForm(populationInfo), BorderLayout.SOUTH);

		JPanel panelSpecies = new JPanel();
		panelSpecies.setLayout(new WrapLayout());
		panelSpecies.setSize(new Dimension(300, 1));
		panelGeneration.add(panelSpecies, BorderLayout.NORTH);
		RNVisualizationViewerFactory factory = new RNVisualizationViewerFactory();

		Species[] species = populationInfo.getSpecies();

		for (int j = 0; j < species.length; j++) {
			FitnessResult fitnessResult = (FitnessResult) species[j].getBestIndividual().getFitnessResult();
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
