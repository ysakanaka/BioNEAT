package use.ready.polishFlag;

import java.io.IOException;

import erne.AbstractFitnessResult;
import erne.SimpleFitnessResult;
import use.math.FitnessResult;
import use.ready.AbstractReadyFitnessFunction;
import use.ready.export.ReadyExporter;
import use.ready.export.ReadyRunner;

public class PolishFlagFitness extends AbstractReadyFitnessFunction {
	
	public static double minDelta = 1.0; //nM
	public String input = "";

	public PolishFlagFitness(){}
	
	public PolishFlagFitness(String input){
		this.input = input;
	}
	
	@Override
	protected AbstractFitnessResult calculateFitnessResult(String path, String[] inputNmes) {
		
		String[] inputNames ;
		
		if(!this.input.equals("")){
			inputNames= new String[] {this.input};
		} else {
			inputNames = inputNmes;
		}
		
		double[][] conc;
		SimpleFitnessResult fit = new SimpleFitnessResult();
		double distance = 0.0;
		
		for (int i=0; i<inputNames.length; i++){
			double maxConc;
			double minConc;
			try {
				conc = ReadyRunner.concForSpecies(path, ReadyRunner.simu, inputNames[i], ReadyExporter.maxSteps/ReadyExporter.interval);
				minConc = conc[0][0];
				maxConc = conc[0][0];
				for (int j=0; j<conc.length; j++){
					for (int k=0; k<conc[0].length;k++){
						if (minConc > conc[j][k]) minConc = conc[j][k];
						if (maxConc < conc[j][k]) maxConc = conc[j][k];
					}
				}
				if((maxConc - minConc) < minDelta)  return minFitness();
				//If not, we have a difference
				for (int j=0; j<conc.length/2; j++){
					for (int k=0; k<conc[0].length;k++){
						distance += (maxConc - conc[j][k]);
					}
				}
				for (int j=conc.length/2; j<conc.length; j++){
					for (int k=0; k<conc[0].length;k++){
						distance += (conc[j][k] - minConc);
					}
				}
				distance /= (maxConc - minConc); //Normalization
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		fit.setFitness(1.0/distance);
		
		return fit;
	}

}
