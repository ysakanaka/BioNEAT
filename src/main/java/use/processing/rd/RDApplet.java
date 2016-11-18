package use.processing.rd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

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
import utils.GraphMaker;
import utils.PadiracTemplateFactory;

/**
 * This class is used to perform a GUI simulation of specific OligoGraph.
 * @author naubertkato
 *
 */

public class RDApplet extends PApplet{
	
	
	float time = 0.0f;
	float maxTime = 1000;
	int bigTimeStep = 10; //How many step do we do between two call of draw.
	

	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
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
		  
		  noStroke();
		}

	public void draw(){
		  //System.out.println(time);
		  //background(255);
		  int base = g.fillColor;
		  //int speciesOffset = 2;
		  int offset = RDConstants.speciesOffset;
		  for (int x = 0; x < system.conc[0].length; x++){
		    for (int y = 0; y < system.conc[0][x].length; y++){
		      float val = min(1.0f,system.conc[0+offset][x][y]/RDConstants.concScale)*255;
		      float val2 = 0.0f;
		      float val3 = 0.0f;
		      if (system.chemicalSpecies >= 2+offset) val2 = min(1.0f,system.conc[1+offset][x][y]/RDConstants.concScale)*255;
		      if (system.chemicalSpecies >= 3+offset) val3 = min(1.0f,system.conc[2+offset][x][y]/RDConstants.concScale)*255;
		     // System.out.println(val);
		      fill(color(val,val2,val3));
		      rect(x*RDConstants.spaceStep,y*RDConstants.spaceStep,RDConstants.spaceStep,RDConstants.spaceStep);
		    }
		  }
		  fill(base);
		  for(int i=0; i<bigTimeStep; i++){
		    system.update();
		  }
		  time++;
		  
		  if(time >= maxTime){
			  saveFrame();
			  time = 0.0f;
		  }
		}
	
	    

		public void setTestGraph(){
		  OligoGraph<SequenceVertex,String> g;
		  if(reac == null){
		    g = GraphMaker.line();
		  
		  } else {
			g = GraphMaker.fromReactionNetwork(reac);
		  }
		  g.exoConc = RDConstants.exoConc;
		  g.polConc = RDConstants.polConc;
		  g.nickConc = RDConstants.nickConc;
		  system.os = new OligoSystem<String>(g, new PadiracTemplateFactory(g));
		  
		}

		


		

		
	
}
