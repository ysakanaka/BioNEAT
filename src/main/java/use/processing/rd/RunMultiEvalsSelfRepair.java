package use.processing.rd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

import javax.swing.JFrame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import reactionnetwork.visual.RNVisualizationViewerFactory;
import use.processing.bead.Bead;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;
import utils.RDLibrary;

public class RunMultiEvalsSelfRepair {
	
	public static int maxEvals = 4000;
	public static int repairTime = 3000;
	
	public static int totalRetry = 100;
	
	public static String outputSuffix = "_Repair_reeval.dat";
	
	public static ReactionNetwork reac = null; 
	
	public static void main(String[] args) throws FileNotFoundException {
		if(args.length >= 1){
			String outputfilename = args[0].split("\\.")[0]+outputSuffix;//we don't want the graph or txt extension
		Gson gson = new GsonBuilder().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
				.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
		BufferedReader in;
		
		try {
			in = new BufferedReader(new FileReader(args[0]));
			reac = gson.fromJson(in, ReactionNetwork.class);
			 
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		RDConstants.maxBeads = 500;
		RDPatternFitnessResultIbuki.width = 0.3;
		  RDPatternFitnessResultIbuki.weightExponential = 0.1;
		  RDConstants.matchPenalty=-0.1;
		  StringBuilder sb = new StringBuilder("");
		  RDFitnessResult fitness;
		  
		  boolean[][] target = RDPatternFitnessResultIbuki.getCenterLine();
		  for(int i = 0; i<totalRetry;i++){
		  RDSystem system = new RDSystem();
		  setTestGraph(system);
		  system.init(true); //with full power, because we are sequential
		  for(int j = 0; j<maxEvals; j++) system.update();
		  fitness = new RDPatternFitnessResultIbuki(system.conc,target,system.beadsOnSpot,0.0);
		  sb.append(fitness+"\t");
		  removeChunckCenter(system);
		  fitness = new RDPatternFitnessResultIbuki(system.conc,target,system.beadsOnSpot,0.0);
		  sb.append(fitness+"\t");
		  for(int j = 0; j<repairTime; j++) system.update();
		  fitness = new RDPatternFitnessResultIbuki(system.conc,target,system.beadsOnSpot,0.0);
		  sb.append(fitness+"\n");
		  }
		  
		  PrintWriter fileOut = new PrintWriter(outputfilename);
			fileOut.write(sb.toString());
			fileOut.close();
		}
		System.exit(0);
	}

	
	protected static void removeChunckBottom(RDSystem system){
		 int minPos = (int) Math.round(system.conc[0].length*(1.0-RDPatternFitnessResultIbuki.width)/2.0);
		  int maxPos = (int) Math.round(system.conc[0].length*(1.0+RDPatternFitnessResultIbuki.width)/2.0);
		  for (int x = minPos; x <=maxPos ; x++){
			    for (int y = (int) Math.round(system.conc[0].length*(1.0-RDPatternFitnessResultIbuki.width))
			    		; y < system.conc[0][x].length; y++){
			    	for(int i =  RDConstants.speciesOffset; i<system.conc.length; i++) system.conc[i][x][y] = 0;
			    	if(system.beadsOnSpot.get(x, y) != null){
			    	  for(Bead b: system.beadsOnSpot.get(x, y)){
			    		  b.setParent(null);
			    		  b.setPosition((float)(Bead.rand.nextDouble()*RDConstants.wsize),(float)(Bead.rand.nextDouble()*RDConstants.hsize));
			            //todo: I should check where I put them...
			    	  }
			    	  system.beadsOnSpot.get(x, y).removeAll(system.beadsOnSpot.get(x, y));
			    	}
		  }
		}
	}
	
	protected static void removeChunckTop(RDSystem system){
		 int minPos = (int) Math.round(system.conc[0].length*(1.0-RDPatternFitnessResultIbuki.width)/2.0);
		  int maxPos = (int) Math.round(system.conc[0].length*(1.0+RDPatternFitnessResultIbuki.width)/2.0);
		  for (int x = minPos; x <=maxPos ; x++){
			    for (int y = 0; y< (int) Math.round(system.conc[0].length*(RDPatternFitnessResultIbuki.width)); y++){
			    	for(int i =  RDConstants.speciesOffset; i<system.conc.length; i++) system.conc[i][x][y] = 0;
			    	if(system.beadsOnSpot.get(x, y) != null){
			    	  for(Bead b: system.beadsOnSpot.get(x, y)){
			    		  b.setParent(null);
			    		  b.setPosition((float)(Bead.rand.nextDouble()*RDConstants.wsize),(float)(Bead.rand.nextDouble()*RDConstants.hsize));
			            //todo: I should check where I put them...
			    	  }
			    	  system.beadsOnSpot.get(x, y).removeAll(system.beadsOnSpot.get(x, y));
			    	}
		  }
		}
	}
	
	protected static void removeChunckCenter(RDSystem system){
		 int minPos = (int) Math.round(system.conc[0].length*(1.0-RDPatternFitnessResultIbuki.width)/2.0);
		  int maxPos = (int) Math.round(system.conc[0].length*(1.0+RDPatternFitnessResultIbuki.width)/2.0);
		  for (int x = minPos; x <=maxPos ; x++){
			    for (int y = minPos; y< maxPos; y++){
			    	for(int i =  RDConstants.speciesOffset; i<system.conc.length; i++) system.conc[i][x][y] = 0;
			    	if(system.beadsOnSpot.get(x, y) != null){
			    	  for(Bead b: system.beadsOnSpot.get(x, y)){
			    		  b.setParent(null);
			    		  b.setPosition((float)(Bead.rand.nextDouble()*RDConstants.wsize),(float)(Bead.rand.nextDouble()*RDConstants.hsize));
			            //todo: I should check where I put them...
			    	  }
			    	  system.beadsOnSpot.get(x, y).removeAll(system.beadsOnSpot.get(x, y));
			    	}
		  }
		}
	}
	
	protected static void removeChunckRight(RDSystem system){
		 int minPos = (int) Math.round(system.conc[0].length*(1.0-RDPatternFitnessResultIbuki.width)/2.0);
		  int maxPos = (int) Math.round(system.conc[0].length*(1.0+RDPatternFitnessResultIbuki.width)/2.0);
		  for (int y = minPos; y <=maxPos ; y++){
			    for (int x = (int) Math.round(system.conc[0].length*(1.0-1.1*RDPatternFitnessResultIbuki.width))
			    		; x < system.conc[0][y].length; x++){
			    	for(int i = RDConstants.speciesOffset; i<system.conc.length; i++) system.conc[i][x][y] = 0;
			    	if(system.beadsOnSpot.get(x, y) != null){
			    	  for(Bead b: system.beadsOnSpot.get(x, y)){
			    		  b.setParent(null);
			    		  b.setPosition((float)(Bead.rand.nextDouble()*RDConstants.wsize),(float)(Bead.rand.nextDouble()*RDConstants.hsize));
			            //todo: I should check where I put them...
			    	  }
			    	  system.beadsOnSpot.get(x, y).removeAll(system.beadsOnSpot.get(x, y));
			    	}
		  }
		}
	}
	
	public static void setTestGraph(RDSystem system){
		OligoGraph<SequenceVertex,String> g;
		  if(reac == null){
			 reac = RDLibrary.rdstart;
		  }
		  system.setNetwork(reac);
			g = GraphMaker.fromReactionNetwork(reac);
			//For debug
			JFrame frame = new JFrame("Graph");
			frame.add((new RNVisualizationViewerFactory()).createVisualizationViewer(reac));
			frame.pack();
			frame.setVisible(true);
		  
		  g.exoConc = RDConstants.exoConc;
		  g.polConc = RDConstants.polConc;
		  g.nickConc = RDConstants.nickConc;
		  system.setOS(new OligoSystem<String>(g, new PadiracTemplateFactory(g)));
		  
		}
}
