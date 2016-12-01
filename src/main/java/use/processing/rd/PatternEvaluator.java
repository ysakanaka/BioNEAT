package use.processing.rd;

import java.util.ArrayList;

import com.google.common.collect.Table;

import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import use.processing.bead.Bead;
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
				if (results[i][j]) match += pattern[i][j]?RDConstants.matchBonus:RDConstants.matchPenalty;
			}
		}
		return match;
	}
	
	/**
	 * Implements a normalized version of Hellinger's distance on the distribution of high conc spots.
	 * We use hbin*vbin to descretize the whole arena + 1 bins for wrong area. If a bin contain both pattern
	 * and empty, we only count the part corresponding to the pattern (i.e. no relaxation of constraints).
	 * @param conc
	 * @param pattern
	 * @return
	 */
	public static double hellingerDistance(float conc[][], boolean[][] pattern){
		double res = 0.0;
		int patternSize = RDConstants.horizontalBins*RDConstants.verticalBins;
		double totalgood = 0; //pattern bin
		double[] goodConc;
		double totalbad = 0; //the rest
		
		goodConc = new double[patternSize];
		
		for(int i = 0; i<conc.length; i++){
			for(int j=0; j<conc[i].length;j++){
		    	if(conc[i][j]>RDConstants.cutOff){
		    		if(pattern[i][j]){
		    			int hpos = (i*RDConstants.horizontalBins)/pattern.length;
		    			int vpos = (i*RDConstants.verticalBins)/pattern.length;
		    			totalgood+= conc[i][j];
		    			goodConc[hpos+vpos*RDConstants.horizontalBins] = conc[i][j];
		    			
		    		} else {
		    			totalbad+= conc[i][j];
		    		}
		    	}
		    	
			}
		}
		if(totalgood+totalbad <= 0) {
			return 1.0;
		}
        double totalconc = totalgood + totalbad;
		double inter = Math.sqrt((double)totalbad/totalconc);
		res = inter*inter;
		for(int i=0; i<goodConc.length; i++){
		  inter =  Math.sqrt(goodConc[i]/totalconc)-Math.sqrt(1.0/(double)patternSize);
		  res += inter*inter;
		}
		return Math.sqrt(res/2.0);
	}
	
	public static boolean[][] detectBeads(int sizex, int sizey, Table<Integer,Integer,ArrayList<Bead>> beads){
		boolean[][] res = new boolean[sizex][sizey];
		for(int i = 0; i<sizex; i++){
			for(int j=0; j<sizey;j++){
				res[i][j] = (beads.get(i, j)!= null);
			}
		}
		return res;
	}
	
	public static boolean[][] detectGlue(float conc[][]){
		boolean[][] res = new boolean[conc.length][conc[0].length];
		for(int i = 0; i<conc.length; i++){
			for(int j=0; j<conc[i].length;j++){
				res[i][j] = (conc[i][j]>RDConstants.cutOff);
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
			avgdist += distance(pattern,detectBeads(syst.conc[0].length, syst.conc[0][0].length,syst.beadsOnSpot));
		}
		return avgdist/(double)RDConstants.trials;
	}
	
	public static double hellingerDistanceRandomDistribution(boolean[][] pattern){
		System.out.println("Evaluating distance of random bead distribution");
		double avgdist = 0.0;
		OligoGraph<SequenceVertex,String> g = GraphMaker.fromReactionNetwork(RDLibrary.rdstart);
		for (int i = 0; i<RDConstants.trials; i++){
			RDSystem syst = new RDSystem();
			syst.os = new OligoSystem<String>(g, new PadiracTemplateFactory(g));
			syst.init(false);
			for(int j=0; j<RDConstants.maxTimeEval; j++) syst.update();
			avgdist += hellingerDistance(syst.conc[RDConstants.glueIndex],pattern);
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
			for(int j=0; j<RDConstants.maxTimeEval; j++) syst.update();
			boolean[][] positions = (RDConstants.useGlueAsTarget?PatternEvaluator.detectGlue(syst.conc[RDConstants.glueIndex])
					:PatternEvaluator.detectBeads(pattern.length, pattern[0].length,syst.beadsOnSpot));
			avgdist += matchOnPattern(pattern,positions);
		}
		return avgdist/(double)RDConstants.trials;
	}
}
