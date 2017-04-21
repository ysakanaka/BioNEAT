package erne.algorithm.nsgaII;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import erne.AbstractLexicographicFitnessResult;
import erne.Individual;
import erne.Population;
import reactionnetwork.ReactionNetwork;

public class NSGAIIPopulation extends Population {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1966721383358670350L;
	
	public NSGAIIPopulation(int size, ReactionNetwork initNetwork){
		super(size,initNetwork);
	}

	@Override
	public Individual[] reproduction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkRestart() {
		// No paramter to set, so void
		
	}
	
	/**
	 * Algorithm from Deb et al.
	 * @param pop
	 * @return
	 */
	protected ArrayList<HashSet<Individual>> fastNonDominatedSort(Individual[] pop){
		ArrayList<HashSet<Individual>> sortedPopulation = new ArrayList<HashSet<Individual>>();
		HashMap<Individual,HashSet<Individual>> dominatedIndividuals = new HashMap<Individual,HashSet<Individual>>(); // solutions that are dominated by p
		HashMap<Individual,Integer> dominationCount = new HashMap<Individual,Integer>();
		
		for(int i = 0; i<pop.length; i++){
			Individual p = pop[i];
			dominatedIndividuals.put(p, new HashSet<Individual>());
			dominationCount.put(p, 0);
			AbstractLexicographicFitnessResult fitP = (AbstractLexicographicFitnessResult) p.getFitnessResult();
			List<Double> fullP = fitP.getFullFitness();
			for(int j = 0; j<pop.length; j++){
				if(j == i) continue;
				Individual q = pop[j];
				AbstractLexicographicFitnessResult fitQ = (AbstractLexicographicFitnessResult) q.getFitnessResult();
				List<Double> fullQ = fitQ.getFullFitness();
				if(fullP.size()>fullQ.size()){
					
				}
			}
		}
		
		return sortedPopulation;
	}
	
	

}
