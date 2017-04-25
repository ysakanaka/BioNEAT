package erne.algorithm.nsgaII;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import erne.Constants;
import erne.Individual;
import erne.MultiobjectiveAbstractFitnessResult;
import erne.Population;
import erne.PopulationInfo;
import erne.speciation.Species;
import erne.util.FastNonDominatedSorter;
import reactionnetwork.ReactionNetwork;

public class NSGAIIPopulation extends Population {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1966721383358670350L;
	
	protected boolean superElitist;
	protected transient FastNonDominatedSorter sorter;
	
	public NSGAIIPopulation(int size, ReactionNetwork initNetwork, boolean superElitist){
		super(size,initNetwork);
		this.superElitist = superElitist;
		sorter = new FastNonDominatedSorter(superElitist);
	}
	
	@Override
	public Individual[] resetPopulation() throws InterruptedException, ExecutionException {
		Individual[] pop = super.resetPopulation();
		
		sorter.fastNonDominatedSort(pop); //Setting correctly the ranks
		return pop;
	}

	@Override
	public Individual[] reproduction() {
		
		
		Individual[] nextGenParents = getNextParents();
		Individual[] nextGen = new Individual[nextGenParents.length];
		
		for(int i = 0; i<nextGen.length; i++){
			nextGen[i] = mutator.mutate(nextGenParents[i].clone());
		}
		
		return nextGen;
	}
	
	public PopulationInfo getPopulationInfo(int i) {
		Individual[] indivs = populations.get(i);
		ArrayList<HashSet<Individual>> sortedBy = (new FastNonDominatedSorter(false)).fastNonDominatedSort(indivs);
		Species[] species = new Species[sortedBy.get(0).size()];
		int index = 0;
		for(Individual ind : sortedBy.get(0)){
			species[index] = new Species(ind);
			species[index].individuals = new ArrayList<Individual>(Arrays.asList(new Individual[]{ind}));
			index++;
		}
		
		
		return new PopulationInfo(populations.get(i), species);
	}

	@Override
	public void checkRestart() {
		// No paramter to set, so void
		//We should check that the last gen is correctly ranked
		sorter = new FastNonDominatedSorter(superElitist);
		ArrayList<Individual> indivs = new ArrayList<Individual>();
		for(int i = 0; i<populations.size();i++){
			//add everybody so far
			indivs.addAll(Arrays.asList(populations.get(i)));
		}
		Individual[] indivArray = new Individual[indivs.size()];
		indivArray = indivs.toArray(indivArray);
		sorter.fastNonDominatedSort(indivArray);
	}
	
	
	/**
	 * Based on previous generations, determine the solutions that should be propagated.
	 * WARNING: original individuals are returned. Should not be modified
	 * @return
	 */
	public Individual[] getNextParents(){
		Individual[] rt; //population to check: new gen + previous gen if available (for elitism)
		Individual[] qt = populations.get(populations.size()-1); //current generation
		
		if (populations.size() <= 1){
			if(superElitist) sorter.fastNonDominatedSort(qt); //init
			return qt;
		}
		Individual[] pt;
		
		if(superElitist){
			pt = new Individual[]{}; //already saved in the sorter
		} else {
			pt = populations.get(populations.size()-2); //previous generation
		}
		
		int sizeRt = qt.length + pt.length;
		rt = new Individual[sizeRt];
		for(int i = 0;i<sizeRt;i++){
			rt[i]=(i<qt.length?qt[i]:pt[i-qt.length]);
		}
		
		ArrayList<HashSet<Individual>> allParetoFronts = sorter.fastNonDominatedSort(rt);
		Individual[] nextParents = new Individual[qt.length];
		
		int counter = 0;
		int rank = 0;
		while(rank < allParetoFronts.size() && counter + allParetoFronts.get(rank).size()<qt.length){
			 //HashMap<Individual, Double> nextRank = computingCrowding(allParetoFronts.get(rank));
			for(Individual ind : allParetoFronts.get(rank)){
				nextParents[counter] = ind;
				counter++;
			}
			 rank++;
			 
		}
		if(counter<qt.length){
			
			//Just in case something strange happened
			if (rank >= allParetoFronts.size()){
				System.err.println("WARNING: less elements in previous population than expected. Padding with base individuals. Rank: "+rank);
				while(counter < qt.length){
					nextParents[counter] = initIndividual.clone();
					counter++;
				}
			} else {
			
			//Now, we need to fill the reminder.
			HashMap<Individual, Double> nextCrowd = computingCrowding(allParetoFronts.get(rank));
			ArrayList<Individual> sortedAtFrontI = new ArrayList<Individual>(allParetoFronts.get(rank));
			sortedAtFrontI.sort(new CrowdingFitnessComparator(nextCrowd));
			java.util.Iterator<Individual> it = sortedAtFrontI.iterator();
			
			while(counter < qt.length){
				nextParents[counter] = it.hasNext()?it.next():initIndividual.clone(); //By def, reaching here the sum should be more, but still
				counter++;
			}
			
			}
		}
		
		return nextParents;
	}
	
	public static HashMap<Individual, Double> computingCrowding(Collection<Individual> individuals){
		HashMap<Individual,Double> crowdingAssignment = new HashMap<Individual,Double>();
		ArrayList<Individual> indivToSort = new ArrayList<Individual>(individuals); //Not sorted yet
		int maxIndex = indivToSort.size();
		if(maxIndex <= 0) return null;
		
		int numberOfObjectives = 0; 
		
		for(int i = 0; i< maxIndex; i++){
			Individual indiv = indivToSort.get(i);
			crowdingAssignment.put(indiv, 0.0);
			
			int nObjs;
			if(MultiobjectiveAbstractFitnessResult.class.isAssignableFrom(indiv.getFitnessResult().getClass())){
				nObjs = ((MultiobjectiveAbstractFitnessResult) indiv.getFitnessResult()).getFullFitness().size();
			} else {
				nObjs = 1;
			}
			numberOfObjectives = Math.max(numberOfObjectives,nObjs);
		}
			
		for(int i = 0; i<numberOfObjectives; i++){
			Individual[] sortedForObjectiveI = new Individual[maxIndex];
			sortedForObjectiveI = indivToSort.toArray(sortedForObjectiveI); // just in case
			Arrays.sort(sortedForObjectiveI,new MultiobjectiveSingleObjectiveFitnessComparator(i)); // we always sort from the same point to avoid partially sorted arrays
			crowdingAssignment.put(sortedForObjectiveI[0], Double.MAX_VALUE);
			crowdingAssignment.put(sortedForObjectiveI[maxIndex - 1], Double.MAX_VALUE); //not crowded
			double fmin =  MultiobjectiveAbstractFitnessResult.getIthFitness(i, sortedForObjectiveI[0].getFitnessResult());
			double fmax =  MultiobjectiveAbstractFitnessResult.getIthFitness(i, sortedForObjectiveI[maxIndex -1].getFitnessResult());
			for(int j = 1; j < maxIndex -1; j++){
				double val = crowdingAssignment.get(sortedForObjectiveI[j]);
				if (val < Double.MAX_VALUE){
					val += ( MultiobjectiveAbstractFitnessResult.getIthFitness(i, sortedForObjectiveI[j+1].getFitnessResult())
							- MultiobjectiveAbstractFitnessResult.getIthFitness(i, sortedForObjectiveI[j-1].getFitnessResult()))
							/(fmax - fmin);
					crowdingAssignment.put(sortedForObjectiveI[j], val);
				}
			}
		}
		
		
		return crowdingAssignment;
	}
	
	public static void main(String[] args){
		
		 ArrayList<HashSet<Individual>> sortedPop = MultiobjectiveAbstractFitnessResult.performBasicTest(true);
		 
		 
		 Individual[] sortedForObjectiveI = new Individual[sortedPop.get(0).size()];
		 sortedForObjectiveI = sortedPop.get(0).toArray(sortedForObjectiveI); // just in case
		 Arrays.sort(sortedForObjectiveI,new MultiobjectiveSingleObjectiveFitnessComparator(0));
		 
		 System.out.println("Sort by first objective");
		 for(int i = 0; i<sortedForObjectiveI.length; i++){
			 System.out.println( MultiobjectiveAbstractFitnessResult.getIthFitness(0, sortedForObjectiveI[i].getFitnessResult())
					 +" "+sortedForObjectiveI[i].getFitnessResult().toString());
		 }
		 
		 
		 HashMap<Individual,Double> crowdingAssignment = computingCrowding(sortedPop.get(0));
		 System.out.println("Crowding:");
		 for(int i = 0; i<sortedForObjectiveI.length; i++){
			 System.out.println(crowdingAssignment.get(sortedForObjectiveI[i]));
		 }
	}
	
	
	
public static class MultiobjectiveSingleObjectiveFitnessComparator implements Comparator<Individual> {
		
		private int index;
		
		public MultiobjectiveSingleObjectiveFitnessComparator(int index){
			this.index = index;
		}
		
		
		@Override
		public int compare(Individual o1, Individual o2) {
			
			int potentialBest = 0;
			
			double d1 = MultiobjectiveAbstractFitnessResult.getIthFitness(index, o1.getFitnessResult());
			double d2 = MultiobjectiveAbstractFitnessResult.getIthFitness(index, o2.getFitnessResult());;
			if (Math.abs(d1 - d2) > Constants.comparisonThreshold){
				
				potentialBest = (int) Math.signum(d1-d2);
				
			}
			
			return potentialBest;
		}
		
	}

public static class CrowdingFitnessComparator implements Comparator<Individual> {
	
	private HashMap<Individual,Double> crowdingAssignment;
	
	public CrowdingFitnessComparator(HashMap<Individual,Double> crowdingAssignment){
		this.crowdingAssignment = crowdingAssignment;
	}
	
	
	@Override
	public int compare(Individual o1, Individual o2) {
		
		if(MultiobjectiveAbstractFitnessResult.class.isAssignableFrom(o1.getFitnessResult().getClass()) &&
				MultiobjectiveAbstractFitnessResult.class.isAssignableFrom(o2.getFitnessResult().getClass())){
			MultiobjectiveAbstractFitnessResult f1 = (MultiobjectiveAbstractFitnessResult) o1.getFitnessResult();
			MultiobjectiveAbstractFitnessResult f2 = (MultiobjectiveAbstractFitnessResult) o2.getFitnessResult();
			if(f1.getRank() == f2.getRank()){
				//For algorithm improvement, "crowding" is reprensented by it inverse, so we need to invert the comparison
				//Hence the -1
				return -1 * (int) Math.signum(crowdingAssignment.get(o1)-crowdingAssignment.get(o2)); 

			}
			return (int) Math.signum(f1.getRank()-f2.getRank());
		}
		
		return (int) Math.signum(o1.getFitnessResult().getFitness()-o2.getFitnessResult().getFitness());
	}
	
}

}
