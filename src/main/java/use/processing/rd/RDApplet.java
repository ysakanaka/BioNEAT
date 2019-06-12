package use.processing.rd;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import processing.core.PApplet;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import reactionnetwork.visual.RNVisualizationViewerFactory;
import use.processing.bead.Bead;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;
import utils.RDLibrary;

/**
 * This class is used to perform a GUI simulation of specific OligoGraph.
 * @author naubertkato
 *
 */

public class RDApplet extends PApplet{
	
	
	float time = 0.0f;
	static float maxTime = 4000;
	static String name;
	
	static boolean selfRepair = false; //Do we remove a bunch of beads?
	static double removePatternSize = 0.2; //how much do we remove? (Square)
	static int fullRun = 4000;
	
	static int bigTimeStep = 10; //How many step do we do between two call of draw.
	static int offset = RDConstants.speciesOffset; //for display

	//For timing
	long startTime = System.currentTimeMillis();
	long realTime = startTime;
	long totalRender = 0;

	//boolean[][] target=RDPatternFitnessResultIbuki.getCenterLine(); //TODO change fitness target
	//boolean[][] target=RDPatternFitnessResultIbuki.getBottomLine();
	static boolean[][] target=RDPatternFitnessResultIbuki.getTPattern();
	//boolean[][] target=RDPatternFitnessResultIbuki.getReversedTPattern();
	
	RDSystem system = new RDSystem();
	static ReactionNetwork reac = null; 

	public static void main(String[] args) {
		if(args.length >= 1){
		Gson gson = new GsonBuilder().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
				.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
		BufferedReader in;
		
		try {
			in = new BufferedReader(new FileReader(args[0]));
			reac = gson.fromJson(in, ReactionNetwork.class);
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		}
		
		//RDConstants.hsize = 250;
		//RDConstants.wsize = 250;
		//RDConstants.spaceStep = 2.0f;
		//RDConstants.timePerStep = 0.1f;
		//RDConstants.concScale = 10.0f;
		//RDConstants.beadExclusion = true;
		//RDConstants.bounceRatePerBead = 1.4;
		//RDConstants.concChemostat = 100.0f;
		//RDConstants.gradientScale /= 1.6;

        // XXX
        //RDConstants.spaceStep = 8;
        //RDConstants.useApprox = true;
        target = RDPatternFitnessResultIbuki.getTPattern();
	
		
		RDConstants.cutOff = 5.0f;
		RDConstants.maxBeads = 500;
		//maxTime = 300000000/bigTimeStep;
		maxTime = 4000/bigTimeStep;
		RDConstants.timing = false;
		RDConstants.useMatchFitness = false;
		RDConstants.useHellingerDistance = true;
		RDConstants.horizontalBins = 1;
		RDConstants.verticalBins = 3;
		 RDPatternFitnessResultIbuki.width = 0.20;
		  RDPatternFitnessResultIbuki.weightExponential = 0.1;
		  RDConstants.matchPenalty=-0.1;
		RDConstants.showBeads = false;

		offset = 2;
		name = (selfRepair?"self-######.png":"screen-######.png");
        PApplet.main("use.processing.rd.RDApplet");

    }
	
	 public void settings(){
		 size(RDConstants.wsize,RDConstants.hsize);
	 }
	 
	 public void setup(){
		  frameRate(10000);
		  setTestGraph(); //We are not being called from somewhere else
		  system.init(true);
		  System.out.println("Params: "+system.conc.length+" "+system.conc[0].length+" "+system.conc[0][0].length);
		  if(RDConstants.timing){
			  System.out.println("Start time: "+startTime);
			  System.out.println("totalSetup: "+(System.currentTimeMillis()-realTime));
		  }
		  realTime = System.currentTimeMillis();
		  noStroke();
		}

	public void draw(){
		  //System.out.println(time);
		  //background(255);
		  realTime = System.currentTimeMillis();
		  int base = g.fillColor;
		  //int speciesOffset = 2;
		  
		  for (int x = 0; x < system.conc[0].length; x++){
		    for (int y = 0; y < system.conc[0][x].length; y++){
		    	float val, val2, val3;
		    	val2 = 0.0f;
		    	  val3 = 0.0f;
		    	if(RDConstants.showBeads) {
		    		val = 0.0f;
		    		if(system.beadsOnSpot.get(x, y) != null && !system.beadsOnSpot.get(x, y).isEmpty()){
		    			for(Bead b: system.beadsOnSpot.get(x, y)){
		    				if(b.getParent()!=null){
		    					val = 255.0f;
		    					break;
		    				}
		    			}
		    		}
		    	  
		    	  
		    	} else {
		    		val = min(1.0f,system.conc[0+offset][x][y]/RDConstants.concScale)*255;
                    // XXX
		    		//if (system.chemicalSpecies >= 2+offset) val2 = min(1.0f,system.conc[1+offset][x][y]/RDConstants.concScale)*255;
		    		//if (system.chemicalSpecies >= 3+offset) val3 = min(1.0f,system.conc[2+offset][x][y]/RDConstants.concScale)*255;
		    	}
		      //System.out.println(val);
		      fill(color(val,val2,val3));
		      rect(x*RDConstants.spaceStep,y*RDConstants.spaceStep,RDConstants.spaceStep,RDConstants.spaceStep);
		    }
		  }
		  fill(base);
		  totalRender += System.currentTimeMillis() - realTime;
		  for(int i=0; i<bigTimeStep; i++){
		    system.update();
		  }
		  time++;
		  
		  if(time >= maxTime){
			  if(RDConstants.timing){
				  System.out.println("total time: "+(System.currentTimeMillis()-startTime));
				  System.out.println("total rendering:"+totalRender);
				  System.out.println("total bead update:"+system.totalBeads);
				  System.out.println("total conc update:"+system.totalConc);
				  RDPatternFitnessResultIbuki.width = 0.4;
				  RDLexicographicFitnessResult fitness = new RDLexicographicFitnessResult(system.conc,target,system.beadsOnSpot,0.0);
				  System.out.println(fitness);
				  exit();
			  }
			 
			  saveFrame(name);
			  time = 0.0f;
			  if(selfRepair){
				  //hard coded at the bottom
				 				  
				  RDFitnessResult fitness = new RDPatternFitnessResultIbuki(system.conc,target,system.beadsOnSpot,0.0);
				  RDPatternFitnessResultIbuki.isValid(target, PatternEvaluator.detectGlue(system.conc[RDConstants.glueIndex]));
				  System.out.println(fitness);
				  System.out.println("Removed a chunck");
				  saveFrame("done"+name);
				  removeChunckCenter(); //TODO change chunk position
				  
				  RDImagePrinter ip = new RDImagePrinter(system.conc);
					BufferedImage bi = new BufferedImage((int) (system.conc[0].length* RDConstants.spaceStep),(int) (system.conc[0][0].length* RDConstants.spaceStep), BufferedImage.TYPE_INT_RGB); 
					Graphics g = bi.createGraphics();
					ip.paintComponent(g);
					
					try{ImageIO.write(bi,"png",new File("image-"+name+".png"));}catch (Exception e) {}
					g.dispose();
				selfRepair = false;
				 fitness = new RDPatternFitnessResultIbuki(system.conc,target,system.beadsOnSpot,0.0);
				 System.out.println(fitness);
				 time -= fullRun/bigTimeStep;
			  } else {
				  RDFitnessResult fitness = new RDPatternFitnessResultIbuki(system.conc,target,system.beadsOnSpot,0.0);
				  RDPatternFitnessResultIbuki.isValid(target, PatternEvaluator.detectGlue(system.conc[RDConstants.glueIndex]));
				  System.out.println(fitness);
				  exit();
			  }
			  
			 
		  }
		}
	
	    

		public void setTestGraph(){
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

		protected void removeChunckBottom(){
			 int minPos = (int) Math.round(system.conc[0].length*(1.0-removePatternSize)/2.0);
			  int maxPos = (int) Math.round(system.conc[0].length*(1.0+removePatternSize)/2.0);
			  for (int x = minPos; x <=maxPos ; x++){
				    for (int y = (int) Math.round(system.conc[0].length*(1.0-removePatternSize))
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
		
		protected void removeChunckTop(){
			 int minPos = (int) Math.round(system.conc[0].length*(1.0-removePatternSize)/2.0);
			  int maxPos = (int) Math.round(system.conc[0].length*(1.0+removePatternSize)/2.0);
			  for (int x = minPos; x <=maxPos ; x++){
				    for (int y = 0; y< (int) Math.round(system.conc[0].length*(removePatternSize)); y++){
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
		
		protected void removeChunckCenter(){
			 int minPos = (int) Math.round(system.conc[0].length*(1.0-removePatternSize)/2.0);
			  int maxPos = (int) Math.round(system.conc[0].length*(1.0+removePatternSize)/2.0);
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
		
		protected void removeChunckRight(){
			 int minPos = (int) Math.round(system.conc[0].length*(1.0-removePatternSize)/2.0);
			  int maxPos = (int) Math.round(system.conc[0].length*(1.0+removePatternSize)/2.0);
			  for (int y = minPos; y <=maxPos ; y++){
				    for (int x = (int) Math.round(system.conc[0].length*(1.0-1.1*removePatternSize))
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
		
		


		

		
	
}
