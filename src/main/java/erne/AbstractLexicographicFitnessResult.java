package erne;

import java.util.Comparator;
import java.util.List;

import use.processing.rd.RDConstants;

public abstract class AbstractLexicographicFitnessResult extends AbstractFitnessResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int rank;
	
	public abstract List<Double> getFullFitness();
	
	public int getRank(){
		return rank;
	}
	
	public static class LexicographicFitnessComparator implements Comparator<AbstractLexicographicFitnessResult> {

		@Override
		public int compare(AbstractLexicographicFitnessResult o1, AbstractLexicographicFitnessResult o2) {
			
			// We assume that, if the list of values are of different size, all missing values are 0
			List<Double> l1 = o1.getFullFitness();
			List<Double> l2 = o2.getFullFitness();
			int length = Math.min(l1.size(), l2.size());
			
			for(int i = 0; i<length; i++){
				double d1 = l1.get(i);
				double d2 = l2.get(i);
				if (Math.abs(d1 - d2) > RDConstants.comparisonThreshold){
					return (int) Math.signum(d1-d2);
				}
			}
			
			
			return l1.size()-l2.size();
		}
		
	}
	

}
