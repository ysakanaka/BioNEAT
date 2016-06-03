package use.ready;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;
import erne.SimpleFitnessResult;
import model.OligoGraph;
import model.chemicals.SequenceVertex;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import use.math.FitnessResult;
import use.oligomodel.OligoSystemComplex;
import use.ready.beads.Bead;
import use.ready.eqwriter.FullEqsWriter;
import use.ready.eqwriter.Utils;
import use.ready.export.ReadyExporter;
import use.ready.export.ReadyRunner;
import utils.MyPair;

public abstract class AbstractReadyFitnessFunction extends AbstractFitnessFunction  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1967113185076519511L;
	
	int nBeads = 50;
	double initPulse = 10.0;
	static Random rand = new Random();
	double defaultBeadSize = 0.03;
	public static String simulationPath; //static because shared by everyone
	public static String simulationName = "simu";
	public static AtomicInteger currentSimuNumber = new AtomicInteger(0);
	//public String simulationName; //should be unique
	private static boolean saveSimulation = false;
	public static Map<Double, Map<String, double[]>> simulationResults;
	
	@Override
	public AbstractFitnessResult minFitness() {
		return new SimpleFitnessResult(0.0);
	}
	
	@Override
	public AbstractFitnessResult evaluate(ReactionNetwork network) {
		
		// calculate inhK using 7_6 rules
		for (Node node : network.nodes) {
			//TODO: dirty hack
			if (node.reporter){
				node.reporter= false;
			}
			if (node.type == Node.INHIBITING_SEQUENCE) {
				for (Node n1 : network.nodes) {
					for (Node n2 : network.nodes) {
						if (node.name.equals("I" + n1.name + n2.name)) {
							node.parameter = (double) 1 / 100 * Math.exp((Math.log(n1.parameter) + Math.log(n2.parameter)) / 2);
						}
					}
				}
			}
		}
					
		
		if (doSaveSimulation()) {
			simulationResults = new TreeMap<Double, Map<String, double[]>>();
		}
		
		
		OligoSystemComplex oligoSystem = new OligoSystemComplex(network);
		
		ArrayList<Bead> beads = getBeads(network, oligoSystem);
		
		HashMap<String,Boolean> diffusing = getDiffusing(network, oligoSystem);
		
		setInitConditions(beads,diffusing);
		
		String[] enzymes = {"pol", "poldispl", "nick", "exo", "exoinhib"};
		OligoGraph<SequenceVertex,String> g = oligoSystem.getGraph();
		String[] inputNames = new String[g.getVertexCount()+g.getEdgeCount()];
		for(int i=0; i<g.getVertexCount();i++){
			inputNames[i] = Utils.idToString(i); //The easy part
			diffusing.put(inputNames[i], true);
		}
		int counter = g.getVertexCount();
		int i = 0;
		Iterator<String> it = g.getEdges().iterator();
		while(it.hasNext()){
			String t = it.next();
			inputNames[i+g.getVertexCount()] = Utils.idToString(counter);
			i++;
			counter += FullEqsWriter.eqSize(g,t);
		}
		int currentSimu = currentSimuNumber.incrementAndGet();
		try {
			ReadyRunner.doReadySimulation(simulationPath+simulationName+currentSimu+"/", ReadyExporter.allInOneReadyExport(g,beads,enzymes,diffusing));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return calculateFitnessResult(simulationPath+simulationName+currentSimu+"/", inputNames);
	}
	
	public static boolean doSaveSimulation() {
		return saveSimulation;
	}
	
	protected abstract AbstractFitnessResult calculateFitnessResult(String path, String[] inputNames);

	protected ArrayList<Bead> getBeads(ReactionNetwork network, OligoSystemComplex oligo){
		ArrayList<Bead> beads = new ArrayList<Bead>();
		if (ReadyReactionNetwork.class.isAssignableFrom(network.getClass())){
			ReadyReactionNetwork realnetwork = (ReadyReactionNetwork) network;
			for (Set<Connection> cs: realnetwork.templateOnBeads){
				if(cs.size() <= 0) continue;
				int size = cs.size();
				Iterator<Connection> itc = cs.iterator();
				while(itc.hasNext()){
					if (!itc.next().enabled) size--;
					
				}
				if (size <= 0) continue; // Only disabled templates..
				
				MyPair<String,Double>[] sp1 = (MyPair<String,Double>[]) new MyPair[size];
				itc = cs.iterator();
				int index = 0;
				while(itc.hasNext()){
					Connection conn = itc.next();
					if (!conn.enabled) continue;
					sp1[index] = new MyPair<String,Double>(Utils.idToString(Utils.getEquivIndex(oligo, conn)),conn.parameter);
					index++;
				}
				for( int i = 0; i<nBeads; i++){
					beads.add(new Bead(rand.nextDouble(),rand.nextDouble(),defaultBeadSize, sp1));
				}
			}
		} else {
			//only one bead type
			ArrayList<Connection> cs = network.connections;
			if (cs.size()<=0) return beads;
			MyPair<String,Double>[] sp1 = (MyPair<String,Double>[]) new MyPair[cs.size()];
			for (int i=0; i<sp1.length; i++){
				sp1[i] = new MyPair<String,Double>(Utils.idToString(Utils.getEquivIndex(oligo, cs.get(i))),cs.get(i).parameter);
			}
			for( int i = 0; i<nBeads; i++){
				beads.add(new Bead(rand.nextDouble(),rand.nextDouble(),defaultBeadSize, sp1));
			}
		}
		return beads;
	}
	
	protected HashMap<String,Boolean> getDiffusing(ReactionNetwork network, OligoSystemComplex oligo){
		HashMap<String, Boolean> diff = new HashMap<String,Boolean>();
		for (Node n : network.nodes){
			if (oligo.getEquiv().containsKey(n.name)) diff.put(Utils.idToString(Utils.getEquivIndex(oligo, n)),true);
		}
		/*for (Connection c :network.connections){
			if (c.enabled){
				
			}
		}*/ //For now, templates are all attached to beads. TODO: should that change?
		return diff;
	}
	
	protected void setInitConditions(ArrayList<Bead> beads, HashMap<String,Boolean> diffusing){
		ArrayList<MyPair<String,Double>> initConditions = new ArrayList<MyPair<String,Double>>();
		for (String s : diffusing.keySet()){
			if(diffusing.get(s)){
				initConditions.add(new MyPair<String,Double>(s,initPulse));
			}
		}
		MyPair<String,Double>[] sp1 = (MyPair<String,Double>[]) new MyPair[initConditions.size()];
		sp1 = initConditions.toArray(sp1);
		beads.add(new Bead(0.5,0.5,1.0,sp1)); //initConditions
	}
	
}
