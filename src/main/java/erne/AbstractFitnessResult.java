package erne;

import java.io.Serializable;

public abstract class AbstractFitnessResult implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public abstract double getFitness();

	public String toString() {
		return String.valueOf(this.getFitness());
	}
}
