package erne.algorithm.nsgaII;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import erne.AbstractLexicographicFitnessResult;
import erne.Individual;
import erne.MultiobjectiveAbstractFitnessResult;
import erne.Population;
import erne.SimpleMultiobjectiveFitnessResult;
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
	protected static ArrayList<HashSet<Individual>> fastNonDominatedSort(Individual[] pop){
		ArrayList<HashSet<Individual>> sortedPopulation = new ArrayList<HashSet<Individual>>();
		sortedPopulation.add(new HashSet<Individual>()); //First front
		HashMap<Individual,HashSet<Individual>> dominatedIndividuals = new HashMap<Individual,HashSet<Individual>>(); // solutions that are dominated by p
		HashMap<Individual,Integer> dominationCount = new HashMap<Individual,Integer>();
		
		for(int i = 0; i<pop.length; i++){
			Individual p = pop[i];
			dominatedIndividuals.put(p, new HashSet<Individual>());
			dominationCount.put(p, 0);
		}
		
		for(int i = 0; i<pop.length; i++){
			Individual p = pop[i];
			MultiobjectiveAbstractFitnessResult fitP = (MultiobjectiveAbstractFitnessResult) p.getFitnessResult();
			for(int j = i+1; j<pop.length; j++){
				Individual q = pop[j];
				MultiobjectiveAbstractFitnessResult fitQ = (MultiobjectiveAbstractFitnessResult) q.getFitnessResult();
				Integer comp = MultiobjectiveAbstractFitnessResult.defaultComparator.compare(fitP,fitQ);
				if(comp < 0) {
					dominationCount.put(p, dominationCount.get(p)+1);
					dominatedIndividuals.get(q).add(p);
				} else if(comp > 0){
					dominationCount.put(q, dominationCount.get(q)+1);
					dominatedIndividuals.get(p).add(q);
				}
			}
			
			//p has been compared to everyone
			if(dominationCount.get(p)==0){
				fitP.setRank(1);
				sortedPopulation.get(0).add(p); // p is on the first front
			}
		}
		
		int counter = 0;
		while(true){
			HashSet<Individual> nextFront = new HashSet<Individual>();
			for(Individual p : sortedPopulation.get(counter)){
				for(Individual q : dominatedIndividuals.get(p)){
					dominationCount.put(q,dominationCount.get(q)-1);
					if(dominationCount.get(q) == 0){
						nextFront.add(q);
						MultiobjectiveAbstractFitnessResult fitQ = (MultiobjectiveAbstractFitnessResult) q.getFitnessResult();
						fitQ.setRank(counter+2); //starts at 2, first rank is for the best front
					}
				}
			}
			if(nextFront.isEmpty()) break;
			sortedPopulation.add(nextFront);
			counter++;
		}
		
		return sortedPopulation;
	}
	
	public static void main(String[] args){
		int nFitness = 100;
		int nObjectives = 3;
		int maxFitness = 10;
		Random rand = new Random();
		
		Individual[] pop = new Individual[nFitness];
		
		for(int i = 0; i<nFitness; i++){
			pop[i] = new Individual(null); //Don't really care
			ArrayList<Double> res = new ArrayList<Double>();
			for(int j = 0; j<nObjectives; j++){
				res.add((double)rand.nextInt(maxFitness));
			}
			pop[i].setFitnessResult(new SimpleMultiobjectiveFitnessResult(res));
		}
		
		
		 ArrayList<HashSet<Individual>> sortedPop = fastNonDominatedSort(pop);
		 System.out.println("Found "+sortedPop.size()+" fronts.");
		 for (int i =0; i<sortedPop.size(); i++){
			 System.out.println("\tFront "+i);
			 for(Individual indiv : sortedPop.get(i)){
				 System.out.println(indiv.getFitnessResult().toString());
			 }
		 }
	}

}
