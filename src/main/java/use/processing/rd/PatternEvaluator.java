package use.processing.rd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.google.common.collect.Table;

import edu.uci.ics.jung.graph.util.Pair;
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
		    			int vpos = (j*RDConstants.verticalBins)/pattern.length;
		    			totalgood+= (RDConstants.patternHellinger?RDConstants.cutOff:conc[i][j]);
		    			goodConc[hpos+vpos*RDConstants.horizontalBins] += (RDConstants.patternHellinger?RDConstants.cutOff:conc[i][j]);
		    			
		    		} else {
		    			totalbad+= (RDConstants.patternHellinger?RDConstants.cutOff:conc[i][j]);
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
	
	public static float[][] detectBeadsAsFloat(int sizex, int sizey, Table<Integer,Integer,ArrayList<Bead>> beads){
		float[][] res = new float[sizex][sizey];
		for(int i = 0; i<sizex; i++){
			for(int j=0; j<sizey;j++){
				res[i][j] = 0.0f;
				if(beads.get(i, j)!= null){
				  for(Bead b: beads.get(i, j)){
					  if(b.getParent() != null) res[i][j]= RDConstants.cutOff + 1.0f;
				  }
				}
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
	
	//TODO: bad copy/pastes. I should use a generic interface "DistanceFunction" and just plug it in
	//TODO: main issue: Hellinger has a different signature than the rest... All are wrong
	//TODO: best would be float[][] float[][]
	
	public static double distanceRandomDistribution(boolean[][] pattern){
		System.out.println("Evaluating distance of random bead distribution");
		double avgdist = 0.0;
		OligoGraph<SequenceVertex,String> g = GraphMaker.fromReactionNetwork(RDLibrary.rdstart);
		for (int i = 0; i<RDConstants.reEvaluation; i++){
			RDSystem syst = new RDSystem();
			syst.setNetwork(RDLibrary.rdstart);
			syst.setOS(new OligoSystem<String>(g, new PadiracTemplateFactory(g)));
			syst.init(false);
			avgdist += distance(pattern,detectBeads(syst.conc[0].length, syst.conc[0][0].length,syst.beadsOnSpot));
		}
		return avgdist/(double)RDConstants.reEvaluation;
	}
	
	public static double hellingerDistanceRandomDistribution(boolean[][] pattern){
		System.out.println("Evaluating distance of random bead distribution");
		double avgdist = 0.0;
		OligoGraph<SequenceVertex,String> g = GraphMaker.fromReactionNetwork(RDLibrary.rdstart);
		for (int i = 0; i<RDConstants.reEvaluation; i++){
			RDSystem syst = new RDSystem();
			syst.setNetwork(RDLibrary.rdstart);
			syst.setOS(new OligoSystem<String>(g, new PadiracTemplateFactory(g)));
			syst.init(false);
			for(int j=0; j<RDConstants.maxTimeEval; j++) syst.update();
			avgdist += hellingerDistance(syst.conc[RDConstants.glueIndex],pattern);
		}
		return avgdist/(double)RDConstants.reEvaluation;
	}
	
		
	public static double matchRandomDistribution(boolean[][] pattern){
		System.out.println("Evaluating distance of random bead distribution");
		double avgdist = 0.0;
		OligoGraph<SequenceVertex,String> g = GraphMaker.fromReactionNetwork(RDLibrary.rdstart);
		for (int i = 0; i<RDConstants.reEvaluation; i++){
			RDSystem syst = new RDSystem();
			syst.setNetwork(RDLibrary.rdstart);
			syst.setOS(new OligoSystem<String>(g, new PadiracTemplateFactory(g)));
			syst.init(false);
			for(int j=0; j<RDConstants.maxTimeEval; j++) syst.update();
			boolean[][] positions = (RDConstants.useGlueAsTarget?PatternEvaluator.detectGlue(syst.conc[RDConstants.glueIndex])
					:PatternEvaluator.detectBeads(pattern.length, pattern[0].length,syst.beadsOnSpot));
			avgdist += matchOnPattern(pattern,positions);
		}
		return avgdist/(double)RDConstants.reEvaluation;
	}
	
	public static int[][] getDistanceMatrix(boolean[][] pattern){
		int[][] dists = new int[pattern.length][pattern[0].length];
		HashSet<Pair<Integer>> toExploreNext = new HashSet<Pair<Integer>>();
		
		for(int i = 0; i< pattern.length; i++){
			for(int j=0; j<pattern[0].length; j++){
				if(pattern[i][j]){
					//dists[i][j] = 1;
					toExploreNext.add(new Pair<Integer>(i,j));
				}
				
			}
		}
		
		if(toExploreNext.isEmpty()) return dists;
		
		//Not the most efficient, which would be dynamic programming, but good enough since we do it once
		for(int i = 0; i< pattern.length; i++){
			for(int j=0; j<pattern[0].length; j++){
				Iterator<Pair<Integer>> it = toExploreNext.iterator();
				int val = RDConstants.useEuclDistance?distEucl(new Pair<Integer>(i,j),it.next()):distInf(new Pair<Integer>(i,j),it.next());
				while(it.hasNext()){
					val = Math.min(val, RDConstants.useEuclDistance?distEucl(new Pair<Integer>(i,j),it.next()):distInf(new Pair<Integer>(i,j),it.next()));
					if (val == 0) break;
				}
				dists[i][j] = val;
			}
		}
		
		return dists;
	}
	
	protected static int distEucl(Pair<Integer> p1,Pair<Integer> p2){
		return (int) Math.floor(Math.sqrt((p1.getFirst()-p2.getFirst())*(p1.getFirst()-p2.getFirst())
				+(p1.getSecond()-p2.getSecond())*(p1.getSecond()-p2.getSecond())));
	}
	
	protected static int distInf(Pair<Integer> p1,Pair<Integer> p2){
		return (int) Math.min(Math.abs(p1.getFirst()-p2.getFirst()),Math.abs(p1.getSecond()-p2.getSecond()));
	}
}
