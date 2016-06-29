package erne;

public class SimpleFitnessResult extends AbstractFitnessResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected double fitness = 0.0;
	
	public SimpleFitnessResult() {}
	
	public SimpleFitnessResult(double value){
		fitness = value;
	}
	
	public void setFitness(double value){
		fitness = value;
	}
	
	@Override
	public double getFitness() {
		return fitness;
	}

}
