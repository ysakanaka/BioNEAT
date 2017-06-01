package erne;

import java.util.ArrayList;
import java.util.List;

public class SimpleMultiobjectiveFitnessResult extends MultiobjectiveAbstractFitnessResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4562383501964375633L;
	ArrayList<Double> fullFitness;
	
	public SimpleMultiobjectiveFitnessResult(ArrayList<Double> fullFitness){
		this.fullFitness = fullFitness;
	}
	
	@Override
	public List<Double> getFullFitness() {
		
		return fullFitness;
	}

	
	public static void main(String[] args){
		ArrayList<Double> a1 = new ArrayList<Double>();
		ArrayList<Double> a2 = new ArrayList<Double>();
		
		a1.add(9.0);
		a1.add(7.0);
		a1.add(8.0);
		a2.add(7.0);
		a2.add(9.0);
		a2.add(9.0);
		
		SimpleMultiobjectiveFitnessResult f1 = new SimpleMultiobjectiveFitnessResult(a1);

		SimpleMultiobjectiveFitnessResult f2 = new SimpleMultiobjectiveFitnessResult(a2);
		
		System.out.println(MultiobjectiveAbstractFitnessResult.defaultComparator.compare(f1, f2));
	}
	
}
