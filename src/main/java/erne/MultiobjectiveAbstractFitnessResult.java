package erne;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import use.processing.rd.RDConstants;

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
				if (Math.abs(d1 - d2) > RDConstants.comparisonThreshold){
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
