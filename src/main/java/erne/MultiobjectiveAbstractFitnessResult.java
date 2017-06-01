package erne;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import erne.util.FastNonDominatedSorter;

public abstract class MultiobjectiveAbstractFitnessResult extends AbstractFitnessResult {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int rank;
	
	public abstract List<Double> getFullFitness();
	
	public int getRank(){
		return rank;
	}
	
	public void setRank(int r){
		rank = r;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("");
		sb.append("rank: "+rank+"\t");
		Iterator<Double> fullFit = getFullFitness().iterator();
		int index = 1;
		while(fullFit.hasNext()){
			sb.append("fitness "+index+": "+fullFit.next()+"\t");
			index++; 
		}
		return sb.toString();
	}
	
	public double getFitness(){
		return Math.min(1.0/((double)rank),getFullFitness().iterator().next()); //people with a finess of zero get zero
	}
	
	public static void main(String[] args){
		
		//performBasicTest(true);
		 performTest(2,true,true);
	}
	
	public static Individual[] testPopulation(){
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
		
		return pop;
	}
	
	public static  ArrayList<HashSet<Individual>> performBasicTest(boolean verbose){
		 return performTest(1,false,verbose);
	}
	
	public static  ArrayList<HashSet<Individual>> performTest(int gen, boolean elitism, boolean verbose){
		 ArrayList<HashSet<Individual>> sortedPop = new ArrayList<HashSet<Individual>>();
		 FastNonDominatedSorter sorter = new FastNonDominatedSorter(elitism);
		for(int i = 0; i<gen; i++){
			Individual[] pop = testPopulation();
		
		     sortedPop = sorter.fastNonDominatedSort(pop);
		     if(verbose) {
		    	 System.out.println("Found "+sortedPop.size()+" fronts.");
		    	 for (int j =0; j<sortedPop.size(); j++){
		    		 System.out.println("\tFront "+j);
		    		 for(Individual indiv : sortedPop.get(j)){
		    			 System.out.println(indiv.getFitnessResult().toString());
		    		 }
		    	 }
		     }
		 
		}
		 return sortedPop;
	}
	
	public static double getIthFitness(int i, AbstractFitnessResult fit){
		if(MultiobjectiveAbstractFitnessResult.class.isAssignableFrom(fit.getClass())){
			MultiobjectiveAbstractFitnessResult trueFit = (MultiobjectiveAbstractFitnessResult) fit;
			if(i < trueFit.getFullFitness().size()) return trueFit.getFullFitness().get(i);
		} else if (i == 0){
			return fit.getFitness();
		}
		
		return -1;
	}
	
	public static class MultiobjectiveFitnessComparator implements Comparator<MultiobjectiveAbstractFitnessResult> {

		@Override
		public int compare(MultiobjectiveAbstractFitnessResult o1, MultiobjectiveAbstractFitnessResult o2) {
			
			// We assume that, if the list of values are of different size, all missing values are 0
			List<Double> l1 = o1.getFullFitness();
			List<Double> l2 = o2.getFullFitness();
			int length = Math.min(l1.size(), l2.size());
			
			int potentialBest = 0;
			
			for(int i = 0; i<length; i++){
				double d1 = l1.get(i);
				double d2 = l2.get(i);
				if (Math.abs(d1 - d2) > Constants.comparisonThreshold){
					if(potentialBest == 0){
						potentialBest = (int) Math.signum(d1-d2);
					} else if (potentialBest != (int) Math.signum(d1-d2)){ // dominates on multiple aspects
						return 0; //not comparable
					}
					
				}
			}
			
			
			return potentialBest;
		}
		
	}
	
	
	
	public static MultiobjectiveFitnessComparator defaultComparator = new  MultiobjectiveFitnessComparator();

}
