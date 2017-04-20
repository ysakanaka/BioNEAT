package use.processing.rd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import cluster.Cluster;
import erne.AbstractFitnessResult;
import erne.Evolver;
import erne.Individual;
import reactionnetwork.ReactionNetwork;
import utils.ResultFilesManipulator;


public class RDEvaluateAgencyOnRun {
	
	static boolean debug = false;
	static int maxDebugEval = 8;
	
	public static void performEvaluation(File folder){
		ArrayList<ArrayList<ReactionNetwork>> bestOverTime = new ArrayList<ArrayList<ReactionNetwork>>();
		double[][] fits = null;
		RDAgencyFitnessFunction fitness = new RDAgencyFitnessFunction();
		if(folder.isDirectory()){
			
				for(File f: folder.listFiles()){
					if (f.isDirectory()){
						Individual[] bestIndiv = null;
						try {
							bestIndiv = Evolver.getBestOverTime(f.getAbsolutePath());
							bestOverTime.add(new ArrayList<ReactionNetwork>());
						} catch (ClassNotFoundException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for(int i = 0; i<(debug?maxDebugEval:bestIndiv.length); i++){
							bestOverTime.get(bestOverTime.size()-1).add(bestIndiv[i].getNetwork());
						}
					}
				}
			
		
		} else {
			System.err.println("Not a folder");
			return;
		}
		if (bestOverTime != null){
			fits = new double[bestOverTime.size()][];
			Cluster.start();
			for (int index = 0; index < bestOverTime.size(); index++) {
				try {
					
					Map<ReactionNetwork, AbstractFitnessResult> results = 
							Cluster.evaluateFitness(fitness, bestOverTime.get(index)); //Parallel evaluation.
					
					fits[index] = new double[bestOverTime.get(index).size()];
					for(int i = 0; i<bestOverTime.get(index).size(); i++){
						try {
							fits[index][i] = results.get(bestOverTime.get(index).get(i)).getFitness();
							if (debug) System.out.println(fits[index][i]+" "+results.get(bestOverTime.get(index).get(i)));
						} catch (NullPointerException e){
							if (debug) System.out.println(results.get(bestOverTime.get(index).get(i)));
							fits[index][i] = 0;
						}
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				} 
			}
			Cluster.stop();
			System.out.println("===== DONE =====");
			//System.out.println(Arrays.toString(fits));
			ResultFilesManipulator.writeResults(fits, "agency", folder);
		}
	}
	
	public static void main(String[] args){
		File folder = new File(args[0]); // top folder from a run
		performEvaluation(folder);
	}

}
