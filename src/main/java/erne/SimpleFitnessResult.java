package erne;

public class SimpleFitnessResult extends AbstractFitnessResult {

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
