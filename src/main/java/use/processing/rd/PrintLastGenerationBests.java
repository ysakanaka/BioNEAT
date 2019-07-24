package use.processing.rd;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.imageio.ImageIO;

import erne.AbstractFitnessResult;
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

	public static int reevaluationLenght = 4000;//1000; //TODO: center-line is 4000
	public static boolean[][] target; //TODO: change based on target 
	public static boolean debug = false;
	public static int generation = 59;
	
	public static void main (String[] args){
		RDPatternFitnessResultIbuki.width = 0.2;
		RDPatternFitnessResultIbuki.weightExponential = 0.1; //good candidate so far: 0.1 0.1
		  RDConstants.matchPenalty=-0.2;
		target = RDPatternFitnessResultIbuki.getTPattern();
		
		RDConstants.useMedian = true;
		RDConstants.reEvaluation = 5;
		StringBuilder sb = new StringBuilder();
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
					Individual[] indivs = Evolver.getBestOverTime(files[i].getAbsolutePath());
					Individual indiv = indivs[generation];
					bestLastGen[i] = indiv.getNetwork();
					System.out.println(files[i].getName()+": "+indiv.getFitnessResult().getFitness());
				} catch(Exception e){
					e.printStackTrace();
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
			RDSystem syst = null;
			AbstractFitnessResult[] results = new AbstractFitnessResult[RDConstants.reEvaluation];
		for(int attempt = 0; attempt<RDConstants.reEvaluation;attempt++){
			
		    
			
			syst = new RDSystem();
			OligoGraph<SequenceVertex,String> gr;
			gr = GraphMaker.fromReactionNetwork(bestLastGen[i]);
			gr.exoConc = RDConstants.exoConc;
			gr.polConc = RDConstants.polConc;
			gr.nickConc = RDConstants.nickConc;
			syst.setNetwork(bestLastGen[i]);
			syst.setOS(new OligoSystem<String>(gr, new PadiracTemplateFactory(gr)));
			syst.init(false);
			for(int step = 0; step<reevaluationLenght; step++)syst.update();
			
			RDPatternFitnessResultIbuki temp = new RDPatternFitnessResultIbuki(syst.conc,target,syst.beadsOnSpot,0.0);
			results[attempt] = temp;
			System.out.println(temp.fitness);
			
		}
		Arrays.sort(results, new AbstractFitnessResult.AbstractFitnessResultComparator());
		float[][][] conc;
		  if (RDConstants.useMedian){
			  conc = ((RDPatternFitnessResultIbuki)results[results.length-1]).conc;
			  System.out.println("Fitness: "+results[results.length-1]);
			  System.out.println("Other fitness: "+results[0]);
			  sb.append(results[results.length/2]+"\n");
		  } else {
			  conc = ((RDPatternFitnessResultIbuki)results[0]).conc;
		  }
			boolean isValid = RDPatternFitnessResultIbuki.isValid(target, PatternEvaluator.detectGlue(conc[RDConstants.glueIndex]));
		
			
			RDImagePrinter ip = new RDImagePrinter(conc);
			BufferedImage bi = new BufferedImage((int) (syst.conc[0].length* RDConstants.spaceStep),(int) (syst.conc[0][0].length* RDConstants.spaceStep), BufferedImage.TYPE_INT_RGB); 
			Graphics g = bi.createGraphics();
			ip.paintComponent(g);
			
			try{ImageIO.write(bi,"png",new File("image-"+folder.getName()+"-"+files[i].getName()+(isValid?"_VALID_":"")+".png"));}catch (Exception e) {}
			g.dispose();
			
			//Now print red only
			float[][][] redComp = new float[1+RDConstants.speciesOffset][][];
			for (int index = 0; index<RDConstants.speciesOffset+1; index++) redComp[index] = conc[index];
			 
			ip = new RDImagePrinter(redComp);
		    bi = new BufferedImage((int) (syst.conc[0].length* RDConstants.spaceStep),(int) (syst.conc[0][0].length* RDConstants.spaceStep), BufferedImage.TYPE_INT_RGB); 
			g = bi.createGraphics();
		    ip.paintComponent(g);
				
			try{ImageIO.write(bi,"png",new File("redComp-"+folder.getName()+"-"+files[i].getName()+(isValid?"_VALID_":"")+".png"));}catch (Exception e) {}
			g.dispose();
			
			}
		File toFile = new File(folder.getAbsolutePath()+"/allLastFitness"+".dat");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(toFile));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
