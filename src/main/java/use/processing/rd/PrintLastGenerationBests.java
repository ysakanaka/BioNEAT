package use.processing.rd;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import erne.Evolver;
import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import reactionnetwork.ReactionNetwork;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;
import utils.RDLibrary;

public class PrintLastGenerationBests {

	public static int reevaluationLenght = 1000;
	public static boolean debug = false;
	
	public static void main (String[] args){
		File folder = new File(args[0]);
		ReactionNetwork[] bestLastGen;
		if(folder.isDirectory()){
		File[] files = folder.listFiles();
		bestLastGen = new ReactionNetwork[files.length];
		System.out.println("Found "+files.length+" potential folders");
		for(int i=0; i<files.length; i++){
			if(files[i].isDirectory()){
				if (debug) {
					bestLastGen[i] = RDLibrary.rdstart;
					continue;
				}
				System.out.println("File "+files[i]);
				try{
					bestLastGen[i] = Evolver.getBestLastGen(files[i].getAbsolutePath()).getNetwork();
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
		
		System.out.println("Done with loading");
		RDConstants.showBeads = true;
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
			
			try{ImageIO.write(bi,"png",new File("image-"+folder.getName()+"-"+i+".png"));}catch (Exception e) {}
			g.dispose();
			}
		System.exit(0);
	}
}