package use.processing.parallel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import erne.Evolver;
import erne.Individual;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessFunctionIbuki;
import use.processing.rd.RDPatternFitnessResultIbuki;

/**
 * This class has for main purpose to compare the fitness a given individual would have with regard to multiple
 * fitness functions, not only the one it was evolved with.
 * @author naubertkato
 *
 */
public class MultiFitnessReTester {
	
	public static int maxGen = 100;
	public static int nFitness = 3;
	
	public static void main (String[] args) throws IOException{
		boolean[][] target=RDPatternFitnessResultIbuki.getTopLine();
		RDConstants.evalRandomDistance=false;
		  RDConstants.defaultRandomFitness = 0.045; //A good approx of the actual value
		  RDConstants.matchPenalty=-0.1;
		  RDConstants.populationSize=10;
		  RDConstants.maxGeneration=100;
		  RDConstants.maxTimeEval=2000;
		  RDConstants.weightDisableTemplate=3;

		  RDConstants.maxNodes=16;
		  RDFitnessFunctionIbuki fitnessFunction=new RDFitnessFunctionIbuki(target);
		  //RDPatternFitnessResultIbuki.weightExponential = 1.0/Math.E;
		  StringBuilder sb = new StringBuilder();
		File folder = new File(args[0]);
		double[][] allBests;
		if(folder.isDirectory()){
		File[] files = folder.listFiles();
		allBests = new double[files.length][];
		
		for(int i=0; i<files.length; i++){
			if(files[i].isDirectory()){
				
				try{
					Individual a = Evolver.getBestEver(files[i].getAbsolutePath());
					
					//for now we hardcode ibuki 1 2 3
					allBests[i] = new double[nFitness+1];
					allBests[i][0] = a.getFitnessResult().getFitness();
					RDPatternFitnessResultIbuki.weightExponential = 0.5;
					allBests[i][1] = fitnessFunction.evaluate(a.getNetwork()).getFitness();
					RDPatternFitnessResultIbuki.weightExponential = 1.0/Math.E;
					allBests[i][2] = fitnessFunction.evaluate(a.getNetwork()).getFitness();
					RDPatternFitnessResultIbuki.weightExponential = 2.0/3.0;
					allBests[i][3] = fitnessFunction.evaluate(a.getNetwork()).getFitness();
				} catch(Exception e){
					System.err.println("Warning: could not read");
					allBests[i] = new double[nFitness+1];
				}
				System.out.println("Done with evo "+files[i].getCanonicalPath()+" "+Arrays.toString(allBests[i]));
				sb.append(files[i].getCanonicalPath()+" ");
				for (int j = 0; j<allBests[i].length; j++){
					sb.append(allBests[i][j]+" ");
				}
				sb.append("\n");
			}
		}
		} else {
			System.err.println("Not a folder");
			return;
		}
		
		System.out.println("Done with loading");
		
		
		PrintWriter fileOut = new PrintWriter(folder.getAbsolutePath() + "/multifit.dat");
		fileOut.write(sb.toString());
		fileOut.close();
	}

}
