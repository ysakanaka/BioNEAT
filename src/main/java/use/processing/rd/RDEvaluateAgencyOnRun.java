package use.processing.rd;

import java.io.File;
import java.io.IOException;

import erne.Evolver;
import erne.Individual;
import reactionnetwork.ReactionNetwork;


public class RDEvaluateAgencyOnRun {
	
	static boolean debug = false;
	
	public static void main(String[] args){
		File folder = new File(args[0]); // top folder from a run
		Individual[] bestOverTime = null;
		RDAgencyFitnessFunction fitness = new RDAgencyFitnessFunction();
		if(folder.isDirectory()){
			try {
				bestOverTime = Evolver.getBestOverTime(folder.getAbsolutePath());
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		} else {
			System.err.println("Not a folder");
			return;
		}
		if (bestOverTime != null){
			for(int i = 0; i<bestOverTime.length; i++){
				ReactionNetwork network = bestOverTime[i].getNetwork();
				System.out.println(fitness.evaluate(network)); //TODO Olaf
			}
			
		}
	}

}
