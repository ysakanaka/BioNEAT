package erne;

public abstract class AbstractFitnessResult {
	public abstract double getFitness();

	public String toString() {
		return String.valueOf(this.getFitness());
	}

	public boolean isSatisfied() {
		return false;
	}
}
