package use.processing.rd;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import erne.Evolver;
import erne.Individual;
import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import reactionnetwork.ReactionNetwork;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;
import utils.RDLibrary;

public class PrintLastGenerationBests {

	public static int reevaluationLenght = 1000; //TODO: center-line is 4000
	public static boolean[][] target = RDPatternFitnessResultIbuki.getBottomLine(); //TODO: change based on target 
	public static boolean debug = false;
	
	public static void main (String[] args){
		File folder = new File(args[0]);
		ReactionNetwork[] bestLastGen;
		File[] files;
		if(folder.isDirectory()){
		 files = folder.listFiles();
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
					Individual indiv = Evolver.getBestLastGen(files[i].getAbsolutePath());
					bestLastGen[i] = indiv.getNetwork();
					System.out.println(files[i].getName()+": "+indiv.getFitnessResult().getFitness());
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
		
		for(int i= 0; i<bestLastGen.length; i++){
			PrintWriter fileOut;
			if(bestLastGen[i]==null)continue;
			try {
				fileOut = new PrintWriter("./network-"+folder.getName()+"-"+files[i].getName()+".txt");
				fileOut.write(bestLastGen[i].toString());
				fileOut.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Done with writing");
		
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
			
			boolean isValid = RDPatternFitnessResultIbuki.isValid(target, PatternEvaluator.detectGlue(syst.conc[RDConstants.glueIndex]));
			
			
			RDImagePrinter ip = new RDImagePrinter(syst.conc);
			BufferedImage bi = new BufferedImage((int) (syst.conc[0].length* RDConstants.spaceStep),(int) (syst.conc[0][0].length* RDConstants.spaceStep), BufferedImage.TYPE_INT_RGB); 
			Graphics g = bi.createGraphics();
			ip.paintComponent(g);
			
			try{ImageIO.write(bi,"png",new File("image-"+folder.getName()+"-"+files[i].getName()+(isValid?"_VALID_":"")+".png"));}catch (Exception e) {}
			g.dispose();
			
			
			}
		System.exit(0);
	}
}
