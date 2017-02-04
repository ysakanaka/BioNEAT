package use.processing.rd;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import erne.Evolver;
import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;
import utils.RDLibrary;

public class PrintBatchNetworks {
	
	static boolean debug = false;
	static int reevaluationLenght = 1000;
	
	public static void main(String[] args){
		File folder = new File(args[0]);
		ReactionNetwork[] bestLastGen;
		File[] files;
		if(folder.isDirectory()){
		 files= folder.listFiles();
		bestLastGen = new ReactionNetwork[files.length];
		System.out.println("Found "+files.length+" potential folders");
		for(int i=0; i<files.length; i++){
			if(!files[i].isDirectory()){
				if (debug) {
					bestLastGen[i] = RDLibrary.rdstart;
					continue;
				}
				System.out.println("File "+files[i]);
				try{
					Gson gson = new GsonBuilder().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
							.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
					BufferedReader in;
					
					try {
						in = new BufferedReader(new FileReader(files[i]));
						bestLastGen[i] = gson.fromJson(in, ReactionNetwork.class);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					 
				} catch(Exception e){
					System.err.println("Warning: could not read");
					bestLastGen[i] = null;
				}
			} else {
				System.err.println("Regular file");
				bestLastGen[i] = null;
			}
		}
		} else {
			System.err.println("Not a folder");
			return;
		}
		RDConstants.showBeads = false;
		for(int i= 0; i<bestLastGen.length; i++){
			if(bestLastGen[i]==null) continue;
			RDSystem syst = new RDSystem();
			OligoGraph<SequenceVertex,String> gr;
			gr = GraphMaker.fromReactionNetwork(bestLastGen[i]);
			gr.exoConc = RDConstants.exoConc;
			gr.polConc = RDConstants.polConc;
			gr.nickConc = RDConstants.nickConc;
			syst.os = new OligoSystem<String>(gr, new PadiracTemplateFactory(gr));
			syst.init(false);
			for(int step = 0; step<reevaluationLenght; step++)syst.update();
			
			RDImagePrinter ip = new RDImagePrinter(syst.conc);
			BufferedImage bi = new BufferedImage((int) (syst.conc[0].length* RDConstants.spaceStep),(int) (syst.conc[0][0].length* RDConstants.spaceStep), BufferedImage.TYPE_INT_RGB); 
			Graphics g = bi.createGraphics();
			ip.paintComponent(g);
			
			try{ImageIO.write(bi,"png",new File("image-"+files[i].getName()+".png"));}catch (Exception e) {}
			g.dispose();
			
			
			}
		System.exit(0);
	}

}
