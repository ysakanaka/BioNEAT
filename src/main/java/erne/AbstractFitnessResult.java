package erne;

import java.io.Serializable;
import java.util.Comparator;

public abstract class AbstractFitnessResult implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public abstract double getFitness();

	public String toString() {
		return String.valueOf(this.getFitness());
	}
	
	public static class AbstractFitnessResultComparator implements Comparator<AbstractFitnessResult> {

		@Override
		public int compare(AbstractFitnessResult o1, AbstractFitnessResult o2) {
			
			if(o1.getFitness() < o2.getFitness()) return -1;
			if(o1.getFitness() == o2.getFitness()) return 0;
			return 1; // I could right it in one formula, but I am afraid of rounding
		}
		
	}
}
