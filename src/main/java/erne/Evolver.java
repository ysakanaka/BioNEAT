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

import cluster.Cluster;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.visual.RNVisualizationViewerFactory;
import xy.reflect.ui.ReflectionUI;
import common.Static;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import erne.algorithm.EvolutionaryAlgorithm;
import erne.algorithm.bioNEAT.BioNEATBuilder;
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
	private transient Population population;
	private transient boolean saveEvo = true; 

	public static final int DEFAULT_POP_SIZE = 50;
	public static final int MAX_GENERATIONS = 200;
	
	public static final FitnessDisplayer DEFAULT_FITNESS_DISPLAYER = new DefaultFitnessDisplayer();
	
	
	public static final EvolutionaryAlgorithm DEFAULT_ALGORITHM = new BioNEATBuilder().buildAlgorithm();

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
	
	public void setSaveEvo(boolean bool){
		saveEvo = bool;
	}

	public Evolver(ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction) throws IOException {
		init(DEFAULT_POP_SIZE, MAX_GENERATIONS, startingNetwork, fitnessFunction, DEFAULT_FITNESS_DISPLAYER, DEFAULT_ALGORITHM);
	}

	public Evolver(ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction, FitnessDisplayer fitnessDisplayer)
			throws IOException {
		init(DEFAULT_POP_SIZE, MAX_GENERATIONS, startingNetwork, fitnessFunction, fitnessDisplayer, DEFAULT_ALGORITHM);
	}
	

	public Evolver(int popSize, int maxGenerations, ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction,
			 FitnessDisplayer fitnessDisplayer, EvolutionaryAlgorithm algorithm) throws IOException {
		init(popSize, maxGenerations, startingNetwork, fitnessFunction, fitnessDisplayer, algorithm);
	}
	
	protected void init(int popSize, int maxGenerations, ReactionNetwork startingNetwork, AbstractFitnessFunction fitnessFunction,
		 FitnessDisplayer fitnessDisplayer, EvolutionaryAlgorithm algorithm){
		this.popSize = popSize;
		this.maxGenerations = maxGenerations;
		this.startingNetwork = startingNetwork;
		this.fitnessFunction = fitnessFunction;
		this.mutator = algorithm.getMutator();
		this.fitnessDisplayer = fitnessDisplayer;
		this.popDisplayer = algorithm.getPopulationDisplayer();
		this.popFactory = algorithm.getPopulationFactory();
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
		} else if(saveEvo) {
			
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
						Cluster.bindProgressBar(window.getProgressBar());
					}
					//
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
		}
		double time0 = System.currentTimeMillis();
		if (readerMode) {
			population = (Population) Serializer.deserialize(resultDirectory + "/population");
		} else {
			
			population = popFactory.createPopulation(popSize, startingNetwork);
			population.setFitnessFunction(fitnessFunction);
			if (mutator == null) {
				mutator = DEFAULT_ALGORITHM.getMutator();
			}
			population.setMutator(mutator);
		}
		double time1 = System.currentTimeMillis();
		System.out.println("Population init time: "+(time1-time0));

		for (int i = 0; i < (readerMode ? population.getTotalGeneration() : maxGenerations); i++) {
			if(Constants.debug) System.out.println("Processing generation " + i);
			time0 = System.currentTimeMillis();
			if (!readerMode) {
				if (i == 0) {
					population.resetPopulation();
				} else {
					population.evolve();
				}
				time1 = System.currentTimeMillis();
				if (saveEvo) Serializer.serialize(resultDirectory + "/population", population);
				double time2 = System.currentTimeMillis();
				if(Constants.debug)System.out.println("Population evaluation time: "+(time1-time0));
				if(Constants.debug)System.out.println("Population serialization time: "+(time2-time1));
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
	 * 
	 * TODO: a lot of duplicated code with normal start. Should be factored.
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
						Cluster.bindProgressBar(window.getProgressBar());
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
				mutator = DEFAULT_ALGORITHM.getMutator();
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
				
					//We may not want to override
				    if(saveEvo) Serializer.serialize(resultDirectory + "/population", population);
			

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
	
	public static boolean hasGUI() {
		return hasGUI("global.config");
	}
	
	public static boolean hasGUI(String file){
		File config = new File(file);
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
	
	public static Individual getBestEver(Population population){
		Individual ret = null;
		for(int i = 0; i<population.getTotalGeneration(); i++){
			PopulationInfo info = population.getPopulationInfo(i);
			Individual temp = info.getBestIndividual();
			if(ret == null || ret.getFitnessResult().getFitness() < temp.getFitnessResult().getFitness()) ret = temp;
		}
		return ret;
	}
	
	public static Individual getBestEver(String resultDirectory) throws ClassNotFoundException, IOException{
		
		Population population;
		
		population = (Population) Serializer.deserialize(resultDirectory + "/population");
		return getBestEver(population);
	}
	
	public Individual getBestEver() {
		Individual ret = null;
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
