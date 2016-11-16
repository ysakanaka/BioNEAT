package erne;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import common.Static;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.rules.AddActivation;
import erne.mutation.rules.AddInhibition;
import erne.mutation.rules.AddNode;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import erne.mutation.rules.TogglePseudoTemplate;
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

	public static final int DEFAULT_POP_SIZE = 50;
	public static final int MAX_GENERATIONS = 200;
	private static final Mutator DEFAULT_MUTATOR = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {
			new DisableTemplate(1), new MutateParameter(90), new AddNode(2), new AddActivation(2), new AddInhibition(5), new TogglePseudoTemplate(5) })));
	public static final FitnessDisplayer DEFAULT_FITNESS_DISPLAYER = new DefaultFitnessDisplayer();

	private transient boolean readerMode = false;
	private transient boolean noGUI = !hasGUI(); //there IS a GUI
	private static ReflectionUI reflectionUI = new ReflectionUI();
	
	public void setGUI(boolean gui){
		this.noGUI = !gui;
	}

	// Reader mode
	public void setReader(String resultDirectory) {
		this.resultDirectory = resultDirectory;
		this.readerMode = true;
		this.noGUI = false;
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
		model.Constants.numberOfPoints = erne.Constants.maxEvalTime;
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
		if (!noGUI){

		EventQueue.invokeLater(new Runnable() {
			public void run() {
			
				try {
					window = new Main();
					if (!readerMode && window != null) {
						window.getMainForm().setVisible(true);
					}
					//Cluster.bindProgressBar(window.getProgressBar());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
		}
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

			if (!noGUI) displayPopulation(i, population);
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
			AbstractFitnessResult fitnessResult = species[j].getBestIndividual().getFitnessResult();
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
	
	public static boolean hasGUI(){
		File config = new File("global.config");
		System.out.println(config.getAbsolutePath());
		if(!config.exists()){
			System.out.println("Couldn't find a configuration file");
			return false;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(config));
			String s;
			while((s = br.readLine()) != null){
				String[] sp = s.split("\\s*=\\s*");
				if(sp[0].equalsIgnoreCase("GUI")){
					br.close();
					return Boolean.parseBoolean(sp[1]);
				}
			}
			System.out.println("No GUI setting. Assuming false");
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
