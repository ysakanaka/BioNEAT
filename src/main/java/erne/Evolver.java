package erne;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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

import reactionnetwork.ReactionNetwork;
import reactionnetwork.visual.RNVisualizationViewerFactory;
import xy.reflect.ui.ReflectionUI;
import common.Static;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import erne.algorithm.bioNEAT.BioNEATPopulationFactory;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.rules.AddActivation;
import erne.mutation.rules.AddInhibition;
import erne.mutation.rules.AddNode;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import erne.mutation.rules.TogglePseudoTemplate;
import erne.speciation.Species;
import erne.util.Serializer;
import erne.visual.BioNEATPopulationDisplayer;
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
	private PopulationDisplayer popDisplayer;
	private transient String resultDirectory;
	private transient PopulationFactory popFactory;

	public static final int DEFAULT_POP_SIZE = 50;
	public static final int MAX_GENERATIONS = 200;
	public static final Mutator DEFAULT_MUTATOR = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {
			new DisableTemplate(1), new MutateParameter(90), new AddNode(2), new AddActivation(2), new AddInhibition(5), new TogglePseudoTemplate(5) })));
	public static final FitnessDisplayer DEFAULT_FITNESS_DISPLAYER = new DefaultFitnessDisplayer();
	public static final PopulationDisplayer DEFAULT_POPULATION_DISPLAYER = new BioNEATPopulationDisplayer();
	public static final PopulationFactory DEFAULT_POPULATION_FACTORY = new BioNEATPopulationFactory();

	private transient boolean readerMode = false;
	private transient boolean noGUI = !hasGUI(); //there IS a GUI
	private static ReflectionUI reflectionUI = new ReflectionUI();
	
	protected transient String extraConfig;
	
	
	public void setGUI(boolean gui){
		this.noGUI = !gui;
	}

	// Reader mode
	public void setReader(String resultDirectory) {
		this.resultDirectory = resultDirectory;
		this.readerMode = true;
		this.noGUI = false;
	}
	
	// In case we want to save an extra file
	public void setExtraConfig(String conf){
		extraConfig = conf;
	}

	public Evolver(ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction) throws IOException {
		init(DEFAULT_POP_SIZE, MAX_GENERATIONS, startingNetwork, DEFAULT_POPULATION_FACTORY, fitnessFunction, DEFAULT_MUTATOR, DEFAULT_FITNESS_DISPLAYER, DEFAULT_POPULATION_DISPLAYER);
	}

	public Evolver(ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction, FitnessDisplayer fitnessDisplayer)
			throws IOException {
		init(DEFAULT_POP_SIZE, MAX_GENERATIONS, startingNetwork, DEFAULT_POPULATION_FACTORY, fitnessFunction, DEFAULT_MUTATOR, fitnessDisplayer, DEFAULT_POPULATION_DISPLAYER);
	}
	
	public Evolver(int popSize, int maxGenerations, ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction,
			Mutator mutator, FitnessDisplayer fitnessDisplayer) throws IOException {
		init(popSize, maxGenerations, startingNetwork, DEFAULT_POPULATION_FACTORY, fitnessFunction, mutator, fitnessDisplayer, DEFAULT_POPULATION_DISPLAYER);
	}

	public Evolver(int popSize, int maxGenerations, ReactionNetwork startingNetwork, PopulationFactory popFactory, AbstractFitnessFunction fitnessFunction,
			Mutator mutator, FitnessDisplayer fitnessDisplayer, PopulationDisplayer popDisplayer) throws IOException {
		init(popSize, maxGenerations, startingNetwork, popFactory, fitnessFunction, mutator, fitnessDisplayer, popDisplayer);
	}
	
	protected void init(int popSize, int maxGenerations, ReactionNetwork startingNetwork, PopulationFactory popFactory, AbstractFitnessFunction fitnessFunction,
			Mutator mutator, FitnessDisplayer fitnessDisplayer, PopulationDisplayer popDisplayer){
		this.popSize = popSize;
		this.maxGenerations = maxGenerations;
		this.startingNetwork = startingNetwork;
		this.fitnessFunction = fitnessFunction;
		this.mutator = mutator;
		this.fitnessDisplayer = fitnessDisplayer;
		this.popDisplayer = popDisplayer;
		this.popFactory = popFactory;
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
			if(extraConfig != null){
				PrintWriter fileOut = new PrintWriter(resultDirectory + "/extraConfig");
				fileOut.write(extraConfig);
				fileOut.close();
			}
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
			population = popFactory.createPopulation(popSize, startingNetwork);
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
	
	/**
	 * In case of crash, if the population is clean, we can restart
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void resumeEvolve(String resultDir) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		this.resultDirectory = resultDir;
		
		model.Constants.numberOfPoints = erne.Constants.maxEvalTime;
		
			System.out.println("ERNe version: " + Serializer.deserialize(resultDirectory + "/version"));
			Evolver savedEvolver = (Evolver) Serializer.deserialize(resultDirectory + "/evolver");
			System.out.println("Inspect this object for evolution details: " + savedEvolver);
			
			//We also have to set the parameters EXACTLY as they were
			//But so far, extra parameters should be dealt with elsewhere (since we do not know where they come from)
			
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
		
			population = (Population) Serializer.deserialize(resultDirectory + "/population");
		
			population.setFitnessFunction(fitnessFunction);
			if (mutator == null) {
				mutator = DEFAULT_MUTATOR;
			}
			population.setMutator(mutator);
		for (int i = 0; i < (population.getTotalGeneration()); i++) {
			if (!noGUI) displayPopulation(i, population);
			System.out.println("Processing generation " + i);
		}
		Population.nextIndivId.set(population.getTotalGeneration()*popSize);
		
		//Check the population parameter
		population.checkRestart();
		int startValue = population.getTotalGeneration();
		for (int i = startValue; i < maxGenerations; i++) {
			System.out.println("Processing generation " + i);
			
					population.evolve();
				
				Serializer.serialize(resultDirectory + "/population", population);
			

			if (!noGUI) displayPopulation(i, population); //pop size just increased
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

		//SpeciesPlotFactory speciesFactory = new SpeciesPlotFactory();
		//JPanel populationVisualPanel = speciesFactory.createSpeciesPanel(population.getSpeciesByGenerations());
		window.getPanelPopulation().removeAll();
		//window.getPanelPopulation().add(speciesVisualPanel, BorderLayout.CENTER);
		window.getPanelPopulation().add(popDisplayer.createPopulationPanel(population));
		window.getPanelPopulation().revalidate();

		window.getPanelFitness().removeAll();
		//window.getPanelFitness().add(
		//		speciesFactory.createSpeciesFitnessPanel(population.getSpeciesByGenerations(),
		//				speciesFactory.getSpeciesColors((ChartPanel) populationVisualPanel)));
		window.getPanelFitness().add(popDisplayer.createFitnessPanel(population));
		window.getPanelFitness().revalidate();

	}
	
	protected void windowDisplay(){
		
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
	
	public static double[] getBestFitnessOverTime(String resultDirectory) throws ClassNotFoundException, IOException{
		double[] ret;
		Population population;
		
		population = (Population) Serializer.deserialize(resultDirectory + "/population");
		ret = new double[population.getTotalGeneration()];
		for(int i = 0; i<population.getTotalGeneration(); i++){
			PopulationInfo info = population.getPopulationInfo(i);
			ret[i] = info.getBestIndividual().getFitnessResult().getFitness();
		}
		
		return ret;
	}
	
	public static Individual getBestEver(String resultDirectory) throws ClassNotFoundException, IOException{
		Individual ret = null;
		Population population;
		
		population = (Population) Serializer.deserialize(resultDirectory + "/population");
		for(int i = 0; i<population.getTotalGeneration(); i++){
			PopulationInfo info = population.getPopulationInfo(i);
			Individual temp = info.getBestIndividual();
			if(ret == null || ret.getFitnessResult().getFitness() < temp.getFitnessResult().getFitness()) ret = temp;
		}
		return ret;
	}
	
	public static Individual[] getBestOverTime(String resultDirectory) throws ClassNotFoundException, IOException{
		
		Population population;
		
		population = (Population) Serializer.deserialize(resultDirectory + "/population");
		Individual[] ret = new Individual[population.getTotalGeneration()];
		for(int i = 0; i<population.getTotalGeneration(); i++){
			PopulationInfo info = population.getPopulationInfo(i);
			ret[i] = info.getBestIndividual();
		}
		return ret;
	}
	
	public static Individual getBestLastGen(String resultDirectory)throws ClassNotFoundException, IOException{
		Individual ret = null;
		Population population;
		
		population = (Population) Serializer.deserialize(resultDirectory + "/population");
		
			PopulationInfo info = population.getPopulationInfo(population.getTotalGeneration()-1);
			ret = info.getBestIndividual();
			
		return ret;
	}
}
