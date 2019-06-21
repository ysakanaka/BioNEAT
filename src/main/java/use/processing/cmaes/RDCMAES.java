package use.processing.cmaes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cluster.Cluster;
import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;
import erne.Evolver;
import erne.Population;
import erne.algorithm.EvolutionaryAlgorithm;
import erne.algorithm.cmaes.CMAESBuilder;
import optimizers.cmaes.CMAEvolutionStrategy;
import optimizers.cmaes.fitness.IObjectiveFunction;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessDisplayer;
import use.processing.rd.RDFitnessFunctionIbuki;
import use.processing.rd.RDPatternFitnessResultIbuki;

public class RDCMAES  implements IObjectiveFunction, Runnable{
	
	public int popSize = 25;
	public CMAEvolutionStrategy cma;
	public String properties = System.getProperty("user.dir")+"/CMAES.config";
	public boolean outputFiles = true;
	
	protected double[] baseGenome; // values of the parameters in structure: stability + concentration, i.e. the same as in BioNEAT
	protected ReactionNetwork structure;
	protected AbstractFitnessFunction fitnessFunction;
	protected ArrayList<Node> orderedNodes = new ArrayList<Node>(); //so that we keep the same order when trying to interpret the genome
	protected ArrayList<Connection> orderedConnections = new ArrayList<Connection>();
	
	public RDCMAES(ReactionNetwork structure, AbstractFitnessFunction fitnessFunction){
		this.structure = structure.clone();
		this.fitnessFunction = fitnessFunction;
		initGenome();
		initCMA();
	}
	
	

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
		
		ReactionNetwork reac = null;
		
		if(args.length >= 1){
			Gson gson = new GsonBuilder().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
					.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
			BufferedReader in;
			
			try {
				in = new BufferedReader(new FileReader(args[0]));
				reac = gson.fromJson(in, ReactionNetwork.class);
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}
			}
		
		if(reac != null){
			initGlobalParams();
			RDPatternFitnessResultIbuki.width = 0.2; //Only for center line
			boolean[][] target = RDPatternFitnessResultIbuki.getTPattern();
			AbstractFitnessFunction fit = generateFitnessFunction(target);
			//Legacy
			RDCMAES rdcmaes = new RDCMAES(reac, fit);
			rdcmaes.run();
			//With new interface
			//EvolutionaryAlgorithm algorithm = new CMAESBuilder().buildAlgorithm();
			//Evolver evolver = new Evolver(RDConstants.populationSize, RDConstants.maxGeneration, reac,
			//		fit, new RDFitnessDisplayer(), algorithm);
			//evolver.setGUI(true);
			//evolver.setExtraConfig(RDConstants.configsToString());
			//evolver.evolve();
		}
	}
	
	public static void initGlobalParams(){
		RDPatternFitnessResultIbuki.weightExponential = 0.1; //good candidate so far: 0.1 0.1
		  RDConstants.matchPenalty=-0.2;
		
		  RDConstants.reEvaluation = 5; //10
		  RDConstants.evalRandomDistance = false;
		  RDConstants.defaultRandomFitness = 0.0;
		  RDConstants.maxTimeEval = 4000;
		  RDConstants.maxBeads = 500;
		  RDConstants.useMedian = true; 
	}
	
	public static AbstractFitnessFunction generateFitnessFunction(boolean[][] target){
		return new RDFitnessFunctionIbuki(target);
	}
	
	public double[] getBaseGenome(){
		return baseGenome;
	}
	
	public ReactionNetwork getStructure(){
		return structure; // may not be in sync with genome
	}
	
	protected void initGenome(){
		//first, find the parameter space: 1 for each signaling strand + 1 for each template
		int size = 0;
		for(Node n : structure.nodes){
			if(n.type == Node.SIMPLE_SEQUENCE){ 
				size ++;
				orderedNodes.add(n);
			}
		}
		for(Connection c : structure.connections){
			if(c.enabled){
				size++;
				orderedConnections.add(c);
			}
		}
		baseGenome = new double[size];
		//then, we set the values in the genome
		for(int i = 0; i<orderedNodes.size(); i++) baseGenome[i] = orderedNodes.get(i).parameter;
		for(int i = 0; i<orderedConnections.size(); i++) baseGenome[orderedNodes.size()+i] = orderedConnections.get(i).parameter;
	}
	
	public ReactionNetwork networkFromGenome(double[] genome){
		ReactionNetwork r = structure.clone();
		for(int i = 0; i<orderedNodes.size(); i++){
			r.getNodeByName(orderedNodes.get(i).name).parameter = genome[i];
		}
		for(int i = 0; i<orderedConnections.size(); i++){
			r.getConnectionByEnds(orderedConnections.get(i).from, orderedConnections.get(i).to).parameter = genome[orderedNodes.size()+i];
		}
		return r;
	}
	
	protected void initCMA(){
		cma = new CMAEvolutionStrategy();
		cma.readProperties(properties);
		
		cma.setDimension(baseGenome.length);
		
		cma.parameters.setPopulationSize(popSize);
		//cma.setInitialStandardDeviation(optimizer.Constants.sigma);
		//cma.options.stopFitness = 0.01D; //Stops if reaches 99%
		cma.setInitialX(baseGenome);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		String datetime = dateFormat.format(date);
		cma.options.outputFileNamesPrefix += "_"+datetime;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		if (outputFiles) cma.writeToDefaultFilesHeaders(0);
		double[] fitness = this.cma.init();
		Cluster.start();
		 while (this.cma.stopConditions.getNumber() == 0)
	    {
		int nbResample = 0;
		double[][] pop = this.cma.samplePopulation();
		ArrayList<ReactionNetwork> networks = new ArrayList<ReactionNetwork>();
		for (int i = 0; i < pop.length; i++)
		{
		  while (!isFeasible(pop[i])) {
		    pop[i] = this.cma.resampleSingle(i);
		    nbResample++;
		    if (nbResample % 50 == 0) {
		      System.out.println("Looping in the resample");
		
		    }
		  }
		  networks.add(networkFromGenome(pop[i]));
        }
		
		Map<ReactionNetwork, AbstractFitnessResult> fitnesses;
		try {
			fitnesses = Population.evaluateFitness(fitnessFunction, (List<ReactionNetwork>) networks.clone());
			for (int i = 0; i < pop.length; i++) {
				fitness[i] = 1.0/fitnesses.get(networks.get(i)).getFitness(); //We want to increase, no decrease
				// System.out.println("Indiv " + i + " Fitness: " +
				// individuals[i].getFitnessResult());
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
 
        this.cma.updateDistribution(fitness);
 
        if (outputFiles) this.cma.writeToDefaultFiles();
       int outmod = 5;
       if (this.cma.getCountIter() % (15* outmod) == 1L) {
         String output = this.cma.getPrintAnnotation();
          System.out.println(output);
        }
  
        if (this.cma.getCountIter() % outmod == 1L) {
         String output = this.cma.getPrintLine();
          System.out.println(output);
          
        }
	 }
		 //Out of the main loop
     String output = this.cma.getPrintLine();
     System.out.println(output);
 
     output = "Terminated due to\n";
     for (String s : this.cma.stopConditions.getMessages())
       output = output + "  " + s + "\n";
    output = output + "best function value " + this.cma.getBestFunctionValue() + " at evaluation " + this.cma.getBestEvaluationNumber();
    System.out.println(output);
    if (outputFiles) this.cma.writeToDefaultFiles(1);
	Cluster.stop();
	}

	@Override
	public boolean isFeasible(double[] arg0) {
		for(int i=0; i<arg0.length; i++){
			if(arg0[i] > (i>orderedNodes.size()?200.0:1000.0) || arg0[i] < 0.1) return false;
		}
		return true;
	}

	@Override
	public double valueOf(double[] arg0) {
		
		double match = fitnessFunction.evaluate(networkFromGenome(arg0)).getFitness();
		return match; //We are trying to maximize
	}

}
