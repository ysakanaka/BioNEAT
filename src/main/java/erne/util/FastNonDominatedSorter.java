package erne.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import erne.Individual;
import erne.MultiobjectiveAbstractFitnessResult;

public class FastNonDominatedSorter {
	
	protected HashMap<Individual,HashSet<Individual>> dominatedIndividuals = new HashMap<Individual,HashSet<Individual>>(); // solutions that are dominated by p
	protected HashMap<Individual,Integer> dominationCount = new HashMap<Individual,Integer>();
	protected boolean elitism = false; //check if we should store all previous individuals
	
	public FastNonDominatedSorter(){
		
	}
	
	public FastNonDominatedSorter(boolean elitism){
		this.elitism = elitism;
	}
	
	protected void computeDomination(Individual[] pop){
		if(!elitism){
			dominatedIndividuals = new HashMap<Individual,HashSet<Individual>>(); // solutions that are dominated by p
			dominationCount = new HashMap<Individual,Integer>();
			}
		ArrayList<Individual> allPreviousIndividuals = new ArrayList<Individual>(dominatedIndividuals.keySet());
		
		
		for(int i = 0; i<pop.length; i++){
				Individual p = pop[i];
				dominatedIndividuals.put(p, new HashSet<Individual>());
				dominationCount.put(p, 0);
				allPreviousIndividuals.add(i,p); //we enforce the same order
		}
		
		
		for(int i = 0; i<pop.length; i++){
			Individual p = pop[i];
			MultiobjectiveAbstractFitnessResult fitP = (MultiobjectiveAbstractFitnessResult) p.getFitnessResult();
			for(int j = i+1; j<allPreviousIndividuals.size(); j++){
				Individual q = allPreviousIndividuals.get(j);
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
		 
		}
		
	}
	
	/**
	 * Algorithm from Deb et al.
	 * @param pop
	 * @return
	 */
	public ArrayList<HashSet<Individual>> fastNonDominatedSort(Individual[] pop){
		ArrayList<HashSet<Individual>> sortedPopulation = new ArrayList<HashSet<Individual>>();
		sortedPopulation.add(new HashSet<Individual>()); //First front
		HashMap<Individual,HashSet<Individual>> tempDominatedIndividuals = new HashMap<Individual,HashSet<Individual>>(); // solutions that are dominated by p
		HashMap<Individual,Integer> tempDominationCount = new HashMap<Individual,Integer>();
		
		computeDomination(pop);
		
		for(Individual p : dominatedIndividuals.keySet()){
			
			tempDominatedIndividuals.put(p, dominatedIndividuals.get(p));
			tempDominationCount.put(p, dominationCount.get(p));
		}
		
		for(Individual p : dominatedIndividuals.keySet()){
			
			MultiobjectiveAbstractFitnessResult fitP = (MultiobjectiveAbstractFitnessResult) p.getFitnessResult();
			
			
			//p has been compared to everyone
			if(tempDominationCount.get(p)==0){
				fitP.setRank(1);
				if(sortedPopulation.get(0).contains(p)) System.err.println("WARNING: duplicated individual");
				sortedPopulation.get(0).add(p); // p is on the first front
			}
		}
		
		int counter = 0;
		while(true){
			HashSet<Individual> nextFront = new HashSet<Individual>();
			for(Individual p : sortedPopulation.get(counter)){
				for(Individual q : tempDominatedIndividuals.get(p)){
					tempDominationCount.put(q,tempDominationCount.get(q)-1);
					if(tempDominationCount.get(q) == 0){
						if(nextFront.contains(q)) System.err.println("WARNING: duplicated individual");
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
}
