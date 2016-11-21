package use.processing.rd;

import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;
import utils.RDLibrary;

public class PatternEvaluator {
	
	/**
	 * Implements the Hamming distance between results and pattern. Set as double for convenience.
	 * @param results
	 * @return
	 */
	public static double distance(boolean[][] pattern, boolean[][] results){
		double dist = 0.0;
		if(results.length != pattern.length){
			System.err.println("Incompatible result size. Result and pattern must be the same size.");
			return Double.MAX_VALUE;
		}
		for(int i= 0; i<results.length; i++){
			if(results[i].length != pattern[i].length){
				System.err.println("Incompatible result size. Result and pattern must be the same size.");
				return Double.MAX_VALUE;
			}
			for (int j=0; j<results[i].length; j++){
				if (results[i][j]^pattern[i][j]) dist += 1.0;
			}
		}
		return dist;
	}
	
	public static double matchOnPattern(boolean[][] pattern, boolean[][] results){
		double match = 0.0;
		if(results.length != pattern.length){
			System.err.println("Incompatible result size. Result and pattern must be the same size.");
			return Double.MAX_VALUE;
		}
		for(int i= 0; i<results.length; i++){
			if(results[i].length != pattern[i].length){
				System.err.println("Incompatible result size. Result and pattern must be the same size.");
				return Double.MAX_VALUE;
			}
			for (int j=0; j<results[i].length; j++){
				if (results[i][j]&&pattern[i][j]) match += 1.0;
			}
		}
		return match;
	}
	
	public static boolean[][] detectBeads(float[][] concBead){
		boolean[][] res = new boolean[concBead.length][];
		for(int i = 0; i<concBead.length; i++){
			res[i] = new boolean[concBead[i].length];
			for(int j=0; j<concBead[i].length;j++){
				res[i][j] = concBead[i][j]>0.0;
			}
		}
		return res;
	}
	
	public static double distanceRandomDistribution(boolean[][] pattern){
		System.out.println("Evaluating distance of random bead distribution");
		double avgdist = 0.0;
		OligoGraph<SequenceVertex,String> g = GraphMaker.fromReactionNetwork(RDLibrary.rdstart);
		for (int i = 0; i<RDConstants.trials; i++){
			RDSystem syst = new RDSystem();
			syst.os = new OligoSystem<String>(g, new PadiracTemplateFactory(g));
			syst.init(false);
			avgdist += distance(pattern,detectBeads(syst.conc[syst.os.total+syst.os.inhTotal]));
		}
		return avgdist/(double)RDConstants.trials;
	}
	
	public static double matchRandomDistribution(boolean[][] pattern){
		System.out.println("Evaluating distance of random bead distribution");
		double avgdist = 0.0;
		OligoGraph<SequenceVertex,String> g = GraphMaker.fromReactionNetwork(RDLibrary.rdstart);
		for (int i = 0; i<RDConstants.trials; i++){
			RDSystem syst = new RDSystem();
			syst.os = new OligoSystem<String>(g, new PadiracTemplateFactory(g));
			syst.init(false);
			avgdist += matchOnPattern(pattern,detectBeads(syst.conc[syst.os.total+syst.os.inhTotal]));
		}
		return avgdist/(double)RDConstants.trials;
	}
}
