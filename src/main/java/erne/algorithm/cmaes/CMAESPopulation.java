package erne.algorithm.cmaes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import erne.Individual;
import erne.Population;
import optimizers.cmaes.CMAEvolutionStrategy;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;

public class CMAESPopulation extends Population {

	protected transient CMAEvolutionStrategy cma; //TODO: sort this mess out
	protected double[] baseGenome;
	protected ArrayList<Node> orderedNodes = new ArrayList<Node>(); //so that we keep the same order when trying to interpret the genome
	protected ArrayList<Connection> orderedConnections = new ArrayList<Connection>();
	public String properties = System.getProperty("user.dir")+"/CMAES.config";
	
	
	public CMAESPopulation(int size, ReactionNetwork initNetwork) {
		super(size, initNetwork);
		initGenome();
		//initCMA(); //Now part of reset population, which is the first thing done by the evolver
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5293301928117798787L;

	@Override
	public Individual[] reproduction() {
		updateCMADistribution();
		return getCMAESSampledPopulation();
	}

	
	protected Individual[] getCMAESSampledPopulation(){
		double[][] pop = cma.samplePopulation();
		Individual[] indivs = new Individual[pop.length];
		for(int i = 0; i<pop.length; i++){
			int resample = 0;
			 while (!isFeasible(pop[i])) {
				    pop[i] = cma.resampleSingle(i);
				    resample++;
				    if(resample % 50 == 0) System.out.println("WARNING: CMAES population: numerous resampling.");
			 }
			indivs[i] = new Individual(networkFromGenome(pop[i]));
		}
		return indivs;
	}
	
	@Override
	public void checkRestart() {
		// TODO hard restart, should save parameters from cma instead
		// Should check that the parameters are correctly set for CMAES.
		initCMA();
	}
	
	@Override
	public Individual[] resetPopulation() throws InterruptedException, ExecutionException {
		initCMA();
		Individual[] individuals = getCMAESSampledPopulation();
		populations.set(getTotalGeneration()-1, individuals);
		evaluateFitness();
		
		return individuals;
	}
	
	protected void initGenome(){
		//first, find the parameter space: 1 for each signaling strand + 1 for each template
		int size = 0;
		ReactionNetwork structure = initIndividual.getNetwork();
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
	
	public double[] getBaseGenome(){
		return baseGenome;
	}
	
	public ReactionNetwork networkFromGenome(double[] genome){
		ReactionNetwork r = initIndividual.getNetwork().clone();
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
		
		cma.parameters.setPopulationSize(populations.get(0).length);
		cma.setInitialStandardDeviation(optimizer.Constants.sigma);
		cma.options.stopFitness = 0.01D; //Stops if reaches 99%
		cma.setInitialX(baseGenome);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		String datetime = dateFormat.format(date);
		cma.options.outputFileNamesPrefix += "_"+datetime;
		cma.writeToDefaultFilesHeaders(0);
		cma.init();
	}
	
	public boolean isFeasible(double[] arg0) {
		for(int i=0; i<arg0.length; i++){
			if(arg0[i] > (i>orderedNodes.size()?200.0:1000.0) || arg0[i] < 0.1) return false;
		}
		return true;
	}
	
	protected void updateCMADistribution(){
		Individual[] pop = populations.get(getTotalGeneration()-1);
		double[] fitness = new double[pop.length];
		for(int i = 0; i<pop.length; i++){
			fitness[i] = 1.0/(0.01+pop[i].getFitnessResult().getFitness()); //we want to maximize, not minimize
		}
		cma.updateDistribution(fitness);
	}
	
	public CMAEvolutionStrategy getCMA(){
		return cma;
	}

}
