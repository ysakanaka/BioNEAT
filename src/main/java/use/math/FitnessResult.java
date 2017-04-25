package use.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.Static;
import erne.MultiobjectiveAbstractFitnessResult;

public class FitnessResult extends MultiobjectiveAbstractFitnessResult {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double[] inputs;
	public double[] targetOutputs;
	public double[] actualOutputs;
	public double[] targetFittingParams;
	public double[] actualFittingParams;
	public ArrayList<double[]> tests;
	protected Double[] listFits = new Double[3];
	private boolean fitnessIsSet = false;

	public boolean minFitness = false;
	
	public static double firstStepReward =   100000;
	public static double secondStepReward = 1000000;
	public static double cutOff = 0.9; //in percent

	public FitnessResult(boolean minFitness) {
		this.minFitness = minFitness;
	}

	public void setFitnesses(){
		if(fitnessIsSet) return;
		if (minFitness){
			for(int i = 0; i<listFits.length; i++){
				listFits[i] = 0.0;
			}
			return;
		}
		double result1 = 0.0;
		if (targetFittingParams != null) {
			
			for (int i = 0; i < targetFittingParams.length; i++) {
				result1 += Math.pow(Math.abs(10 * (actualFittingParams[i] - targetFittingParams[i]) / targetFittingParams[i]),2);
			}
			listFits[0] = 1.0/ Math.max(result1, 1.0);
			
			
		
			double result2 = 0.0;
			for (int i = 0; i < actualOutputs.length; i++) {
				double error = Math.abs(actualOutputs[i] - targetOutputs[i]);
				result2 += Math.pow(error, 2);
			}
			listFits[1] =  1.0/ Math.max(result2, 1.0);
			
			
		    listFits[2]= Math.pow(getSDV(actualOutputs),2);
		} else {
			for(int i = 0; i<listFits.length; i++){
				listFits[i] = 0.0;
			}
		}
		fitnessIsSet = true;
	}
	
	@Override
	public double getFitness() {
		if (minFitness){
			return 0;
		}
		
		

		return listFits[2]*(firstStepReward*listFits[0]+(listFits[0]>cutOff?secondStepReward*listFits[1]:0.0));
	}

	private double getSDV(double[] actualOutputs) {
		double mean = 0;
		for (int i = 0; i < actualOutputs.length; i++) {
			mean += actualOutputs[i];
		}
		mean /= actualOutputs.length;
		double sdv = 0;
		for (int i = 0; i < actualOutputs.length; i++) {
			sdv += Math.pow(actualOutputs[i] - mean, 2);
		}
		return Math.sqrt(sdv / actualOutputs.length);
	}

	@Override
	public String toString() {
		if(!fitnessIsSet) return "Not available yet";
		if (minFitness)
			return "MIN_FITNESS";
		StringBuilder builder = new StringBuilder();
		double fitness = getFitness();
		builder.append(fitness);

		if (listFits[0] < cutOff){
			builder.append(" didn't make the cut");
			return  builder.toString();
		}
		builder.append(" > cufoff ("+firstStepReward*cutOff+")");
		builder.append(" Outputs[target]:");
		for (int i = 0; i < actualOutputs.length; i++) {
			builder.append(Static.df2.format(actualOutputs[i]) + "[" + Static.df2.format(targetOutputs[i]) + "] ");
		}
		
		if (targetFittingParams != null) {
			builder.append("Fitting params[target]: ");
			for (int j = 0; j < targetFittingParams.length; j++) {
				builder.append(Static.df4.format(actualFittingParams[j]) + "[" + Static.df4.format(targetFittingParams[j]) + "] ");
			}
		}
		builder.append("SDV: " + Static.df4.format(getSDV(actualOutputs)));
		return builder.toString();
	}

	@Override
	public List<Double> getFullFitness() {
		this.getFitness(); //To be sure to set everything.
		ArrayList<Double> fits = new ArrayList(Arrays.asList(listFits));
		
		return fits;
	}
}
