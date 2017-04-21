package pruning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cluster.Cluster;
import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;
import erne.Population;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import use.math.AbstractMathFitnessFunction;
import use.math.FitnessResult;
import use.math.TestGUI;
import use.math.step.StepFitnessFunction;

/**
 * Defines the Pruner class which implements a generic pruning algorithm.
 * For the sake of genericity, possible operations are passed through the interface.
 * Note that we do not enforce that a graph is smaller AFTER applying a given operation.
 * (User beware). TODO we should at least provide a warning though.
 * 
 * The algorithm works breath-first: from the starting graph, all possibility for each operation
 * are explored and the fitness of the resulting graph is evaluated. Only graph whose fitness is
 * above a given threshold (compered to the ORIGINAL fitness) are saved. We then perform another
 * round of removal (again, comparing fitness to the ORIGINAL one), and so on until either we have
 * expended the evaluation budget or there is no valid operation left (i.e. the set of graph whose
 * fitness is above the threshold is empty at the end of the round).
 * 
 * We then return the set of valid graphs in the lexicographic order (size,fitness) (small first, best first).
 * @author naubertkato
 *
 */
public class Pruner {
	
	protected ReactionNetwork original;
	protected AbstractFitnessResult baseFitness;
	protected AbstractFitnessFunction f;
	
	public int maxEvals = 1000;
	public double threshold = 0.85; // expressed in percent
	public ArrayList<PruningRule> removalRules;
	
	protected ArrayList<ReactionNetwork> lastRound; //reaction networks making the cut last time
	protected HashMap<ReactionNetwork,AbstractFitnessResult> goodOnes = new HashMap<ReactionNetwork,AbstractFitnessResult>();
	protected HashSet<ReactionNetwork> seenSoFar = new HashSet<ReactionNetwork>();
	
	public Pruner(ReactionNetwork original, AbstractFitnessResult baseFitness, AbstractFitnessFunction f, ArrayList<PruningRule> removalRules){
		init(original, baseFitness,f,removalRules, false);
	}
	
	public Pruner(ReactionNetwork original, AbstractFitnessResult baseFitness, AbstractFitnessFunction f, ArrayList<PruningRule> removalRules, boolean checkFitness){
		init(original, baseFitness, f, removalRules, checkFitness);
	}
	
	protected void init(ReactionNetwork original, AbstractFitnessResult baseFitness, 
			AbstractFitnessFunction f, ArrayList<PruningRule> removalRules, boolean checkFitness){
		this.original = original;
		this.baseFitness = baseFitness;
		this.f = f;
		this.removalRules = removalRules;
		if(checkFitness){
			AbstractFitnessResult test = f.evaluate(original);
			if(baseFitness.getFitness() != test.getFitness()){
				System.err.println("WARNING: Pruner: provided fitness is different than evaluated fitness ("+baseFitness.getFitness()
				+" vs "+test.getFitness());
			}
		}
		
		lastRound = new ArrayList<ReactionNetwork>();
		lastRound.add(original.clone());
		seenSoFar.add(lastRound.get(0));
		goodOnes.put(lastRound.get(0),baseFitness); //It is good by definition
	}
	
	protected void performPruningOnCurrentSet(){
		ReactionNetworkPruningIterator it = new ReactionNetworkPruningIterator(lastRound,removalRules);
		List<ReactionNetwork> networks = new LinkedList<ReactionNetwork>();
		while(it.hasNext() && maxEvals >0){
			ReactionNetwork rn = it.next();
			if(!seenSoFar.contains(rn)){
				seenSoFar.add(rn);
				maxEvals--;
				networks.add(rn);
			}
		}
		try {
			Map<ReactionNetwork,AbstractFitnessResult> res = Population.evaluateFitness(f, networks); //And I should do something with it
			lastRound = new ArrayList<ReactionNetwork>();
			for(ReactionNetwork rn : res.keySet()){
				if((baseFitness.getFitness()-res.get(rn).getFitness())<threshold){
					lastRound.add(rn);
					goodOnes.put(rn,res.get(rn));
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<ReactionNetwork,AbstractFitnessResult> prune(){
		while(maxEvals > 0){
			performPruningOnCurrentSet();
		}
		return goodOnes;
	}
	
	/**
	 * Iterates through potential reaction networks.
	 * There is no guaranty preventing duplicates
	 * @author naubertkato
	 *
	 */
	public class ReactionNetworkPruningIterator implements Iterator<ReactionNetwork> {

		protected ArrayList<ReactionNetwork> lastRound;
		protected ArrayList<PruningRule> rules;
		protected int ruleIndex = -1;
		protected PruningRuleIterator currentIterator;
		
		@SuppressWarnings("unchecked")
		public ReactionNetworkPruningIterator(ArrayList<ReactionNetwork> lastRound,ArrayList<PruningRule> rules){
			this.lastRound = (ArrayList<ReactionNetwork>) lastRound.clone();
			this.rules = (ArrayList<PruningRule>) rules.clone();
			
			if(!this.lastRound.isEmpty()){
				ReactionNetwork first = this.lastRound.get(0);
				if(!this.rules.isEmpty()){
					ruleIndex = 0;
					currentIterator = this.rules.get(ruleIndex).getIterator(first);
				}
				
			}
		}
		
		@Override
		public boolean hasNext() {
			if(ruleIndex>=rules.size()-1&&lastRound.size()<=1) return currentIterator.hasNext(); //
			return (ruleIndex>=0)&&(!(lastRound.isEmpty())||currentIterator.hasNext());
		}

		@Override
		public ReactionNetwork next() {
			if(!currentIterator.hasNext()){
				
			
				//Ok, so we are done with this rule, moving on
				ruleIndex++;
				if(ruleIndex>=rules.size()){ //we did everything on this indiv, moving on
					lastRound.remove(0);
					ruleIndex = 0;
					currentIterator = rules.get(ruleIndex).getIterator(lastRound.get(0));
				} else { //still some more rules we can apply
				currentIterator = rules.get(ruleIndex).getIterator(lastRound.get(0));
				}
			}
			return currentIterator.next(); //Note: we may have seen this individual already (multiple possible paths)
		}
		
	}
	
	public static void main(String[] args) throws FileNotFoundException{
		Gson gson = new GsonBuilder().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
				.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
		BufferedReader in = new BufferedReader(new FileReader(args[0]));
		ReactionNetwork rn = gson.fromJson(in, ReactionNetwork.class);
		StepFitnessFunction f = new StepFitnessFunction();
		//System.out.println(rn);  //Check: ok
		ArrayList<PruningRule> rules = new ArrayList<PruningRule>();
		rules.add(new TemplatePruningRule());
		
		
		model.Constants.numberOfPoints = erne.Constants.maxEvalTime;
		erne.Constants.maxEvalClockTime = 3000;
		
		AbstractFitnessResult r = f.evaluate(rn);
		System.out.println(r);
		Pruner p = new Pruner(rn,r,f,rules);
		p.maxEvals = 1000;
		
		StepFitnessFunction.hysteresis = true;
		StepFitnessFunction.diff = false;
		
		if(StepFitnessFunction.hysteresis) f.nTests *= 2;
		
		Map<ReactionNetwork,AbstractFitnessResult> res = p.prune();
		ReactionNetwork smallest = rn;
		for(ReactionNetwork pruned : res.keySet()){
			if(pruned.getNEnabledConnections() <= smallest.getNEnabledConnections()){
				if(res.get(pruned).getFitness()>res.get(smallest).getFitness()){
					smallest = pruned;
				}
			}
		}
		
		TestGUI window = new TestGUI();
		window.frame.setVisible(true);
		AbstractMathFitnessFunction.setSaveSimulation(true);
		window.displayEvaluationResults(smallest, (FitnessResult) f.evaluate(smallest));
	}
	
}
