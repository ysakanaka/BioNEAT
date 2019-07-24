package use.processing.rd;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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

public class RunMultiEvals {
	
	public static int maxEvals = 4000;
	
	public static int totalRetry = 100;
	public static boolean printReevals = true;
	
	public static String outputSuffix = "_reeval.dat";
	
	public static ReactionNetwork reac = null; 
	
	public static void main(String[] args) throws FileNotFoundException {
		String outputfilename;
		String[] filenames;
		if(args.length >= 1){
			File f = new File(args[0]);
			if(f.isDirectory()) {
				outputfilename = args[0]+"/reevals.dat";
				File[] files = f.listFiles();
				filenames = new String[files.length];
				
				for( int i = 0; i<files.length; i++) {
					filenames[i] = files[i].getAbsolutePath();
					System.out.println(filenames[i]);
				}
			} else {
			outputfilename = args[0].split("\\.")[0]+outputSuffix;//we don't want the graph or txt extension
			filenames= new String[1];
			filenames[0] = args[0];
			}
		Gson gson = new GsonBuilder().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
				.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
		BufferedReader in;
		boolean[][] target = RDPatternFitnessResultIbuki.getTPattern(); //TODO: fitness type
		for (int which = 0; which < filenames.length; which++) {
			try {
				
				in = new BufferedReader(new FileReader(filenames[which]));
				
				reac = gson.fromJson(in, ReactionNetwork.class);
				 
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			} catch (JsonSyntaxException e) {
				System.err.println("Not a valid graph, skipping");
				continue;
			}
			RDConstants.maxBeads = 500;
			RDPatternFitnessResultIbuki.width = 0.2;
			  RDPatternFitnessResultIbuki.weightExponential = 0.1;
			  RDConstants.matchPenalty=-0.2;
			  StringBuilder sb = new StringBuilder("");
			  RDFitnessResult fitness;
			  
			 
			  for(int i = 0; i<totalRetry;i++){
			  RDSystem system = new RDSystem();
			  setTestGraph(system);
			  system.init(true); //with full power, because we are sequential
			  for(int j = 0; j<maxEvals; j++) system.update();
			  fitness = new RDPatternFitnessResultIbuki(system.conc,target,system.beadsOnSpot,0.0);
			  if(printReevals) printRed(filenames[which].substring(0, filenames[which].lastIndexOf("."))+i,target,system.conc);
			  sb.append(fitness+"\n");
			  System.out.println(i+" "+fitness);
			  }
			  
			  PrintWriter fileOut = new PrintWriter(outputfilename);
				fileOut.write(sb.toString());
				fileOut.close();
			}
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
	
	public static void printRed(String basefilename, boolean[][] target, float[][][] conc) {
		boolean isValid = RDPatternFitnessResultIbuki.isValid(target, PatternEvaluator.detectGlue(conc[RDConstants.glueIndex]));
		
		
		RDImagePrinter ip = new RDImagePrinter(conc);
		BufferedImage bi = new BufferedImage((int) (conc[0].length* RDConstants.spaceStep),(int) (conc[0][0].length* RDConstants.spaceStep), BufferedImage.TYPE_INT_RGB); 
		Graphics g = bi.createGraphics();
		ip.paintComponent(g);
		try{ImageIO.write(bi,"png",new File(basefilename+"-fullColor-"+"-"+(isValid?"_VALID":"")+".png"));}catch (Exception e) {}
		g.dispose();
		
		//Now print red only
		float[][][] redComp = new float[1+RDConstants.speciesOffset][][];
		for (int index = 0; index<RDConstants.speciesOffset+1; index++) redComp[index] = conc[index];
		
		ip = new RDImagePrinter(redComp);
	    bi = new BufferedImage((int) (conc[0].length* RDConstants.spaceStep),(int) (conc[0][0].length* RDConstants.spaceStep), BufferedImage.TYPE_INT_RGB); 
		g = bi.createGraphics();
	    ip.paintComponent(g);
			
		try{ImageIO.write(bi,"png",new File(basefilename+"-redComp-"+"-"+(isValid?"_VALID_":"")+".png"));}catch (Exception e) {}
		g.dispose();
		
	}
}
