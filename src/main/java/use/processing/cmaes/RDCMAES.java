package use.processing.cmaes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import erne.AbstractFitnessFunction;
import optimizers.cmaes.CMAEvolutionStrategy;
import optimizers.cmaes.fitness.IObjectiveFunction;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessFunctionIbuki;
import use.processing.rd.RDPatternFitnessResultIbuki;

public class RDCMAES  implements IObjectiveFunction, Runnable{
	
	public int seed = -1;
	public int popSize = 50;
	public CMAEvolutionStrategy cma;
	public String properties = System.getProperty("user.dir")+"/CMAES.config";
	
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
	
	

	public static void main(String[] args) {
		
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
			boolean[][] target = RDPatternFitnessResultIbuki.getCenterLine();
			AbstractFitnessFunction fit = generateFitnessFunction(target);
			RDCMAES rdcmaes = new RDCMAES(reac, fit);
			rdcmaes.run();
		}
	}
	
	public static void initGlobalParams(){
		RDPatternFitnessResultIbuki.weightExponential = 0.1; //good candidate so far: 0.1 0.1
		  RDConstants.matchPenalty=-0.1;
		
		  RDConstants.reEvaluation = 10;
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
	
	protected void initCMA(){
		if(seed == -1){
			seed = (int) System.currentTimeMillis(); 
		}
		cma.readProperties(properties);
		cma = new CMAEvolutionStrategy();
		cma.setDimension(baseGenome.length);
		
		cma.parameters.setPopulationSize(popSize);
		cma.setInitialStandardDeviation(optimizer.Constants.sigma);
		cma.options.stopFitness = 0.01D; //Stops if reaches 99%
		cma.setInitialX(baseGenome);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		String datetime = dateFormat.format(date);
		cma.options.outputFileNamesPrefix += "_"+datetime;
	}

	@Override
	public void run() {
		cma.writeToDefaultFilesHeaders(0);
		
	}

	@Override
	public boolean isFeasible(double[] arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double valueOf(double[] arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
