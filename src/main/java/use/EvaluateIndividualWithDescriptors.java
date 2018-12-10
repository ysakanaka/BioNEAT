package use;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFrame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import erne.Constants;
import erne.Individual;
import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import reactionnetwork.visual.RNVisualizationViewerFactory;
import use.processing.multiobjective.RDInObjective;
import use.processing.multiobjective.RDObjective;
import use.processing.multiobjective.RDOutObjective;
import use.processing.rd.PatternEvaluator;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessResult;
import use.processing.rd.RDPatternFitnessResult;
import use.processing.rd.RDPatternFitnessResultIbuki;
import use.processing.rd.RDSystem;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;
import utils.RDLibrary;
import utils.RunningStatsAnalysis;

public class EvaluateIndividualWithDescriptors {

	public static String outputSuffix = "test.txt";
	public static ReactionNetwork reac = null; 
	public static boolean[][] target = null;
	//public static ArrayList<RDObjective> features = new ArrayList<RDObjective>(); TODO

	public static void main(String[] args) {

		
		
		if(args.length >= 2){
			String outputfilename = args[1].split("\\.")[0]+outputSuffix;//we don't want the graph or txt extension
			Gson gson = new GsonBuilder().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
					.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();

			try {
				
				
				BufferedReader br = new BufferedReader(new FileReader(args[0]));
				String line;
				
				while((line = br.readLine()) != null){
					String[] paramPair = line.split("\\s*=\\s*");
					String trimmedParamName = paramPair[0].trim();
					if(trimmedParamName.startsWith("#") || paramPair.length != 2) continue; //Incorrect format or comment
					
					if(trimmedParamName.equals("target")) {
						target = setTarget(paramPair[1].trim().toLowerCase());
					} else {
						RDConstants.readConfigFromString(Constants.class,trimmedParamName, paramPair[1]);
						RDConstants.readConfigFromString(RDConstants.class,trimmedParamName, paramPair[1]);
					}
					
				}
				br.close();
				
				

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			 
			try {
				BufferedReader br = new BufferedReader(new FileReader(args[1]));
				reac = gson.fromJson(br, ReactionNetwork.class);
				br.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			evaluateIndividual(reac,outputfilename);
			
		} else {
			System.out.println("USAGE: java [options] "+EvaluateIndividualWithDescriptors.class.getName()+" parameters");
		}
		
		System.exit(0);
	}

	public static void evaluateIndividual(ReactionNetwork r, String outputfilename) {
		//RDConstants.maxBeads = 500;
		RDPatternFitnessResultIbuki.width = 0.3;
		RDPatternFitnessResultIbuki.weightExponential = 0.1;
		//RDConstants.matchPenalty=-0.1;
		  StringBuilder sb = new StringBuilder("");
		  RDPatternFitnessResult fitness;
		  RDInObjective inObjective = new RDInObjective();
		  RDOutObjective outObjective = new RDOutObjective();
		  if (RDConstants.debug) System.out.println("GradientNames: ['"+RDConstants.gradientsName[0]+"', '"+RDConstants.gradientsName[1]+"']");
		  if (target == null) {
			  System.out.println("warning: TargetUndefined");
			  target = RDPatternFitnessResultIbuki.getCenterLine();
		  }
		  int realEvaluations = RDConstants.reEvaluation;
		  RunningStatsAnalysis rsa = new RunningStatsAnalysis();
		  for(int i = 0; i<realEvaluations;i++){
			  RDSystem system = new RDSystem();
			  setTestGraph(system);
			  system.init(false); //with full power, because we are doing parallel eval (maybe)
			  for(int j = 0; j<RDConstants.maxTimeEval; j++) system.update();
			  fitness = new RDPatternFitnessResultIbuki(system.conc,target,system.beadsOnSpot,0.0);
			  rsa.addData(fitness.getFitness());
			  
			  sb.append("fitness"+i+": "+fitness+"\n");
			  sb.append("meansofar"+i+": "+rsa.getMean()+"\n");
			  sb.append("sdsofar"+i+": "+rsa.getStandardDeviation()+"\n");
			  sb.append("sesofar"+i+": "+rsa.getStandardError()+"\n");
			  sb.append("in"+i+": "+inObjective.evaluateScore(r, target, PatternEvaluator.detectGlue(system.conc[RDConstants.glueIndex]))+"\n");
			  sb.append("out"+i+": "+(1.0-outObjective.evaluateScore(r, target, PatternEvaluator.detectGlue(system.conc[RDConstants.glueIndex])))+"\n");
		      //moving goal post
			  if(i == realEvaluations -1 && RDConstants.sampleUntilMeanConvergence 
					  && realEvaluations < RDConstants.maxReEvaluation && rsa.getStandardError() > RDConstants.standardErrorThreshold) {
				  realEvaluations++;
			  }
		  }
		  sb.append("nEvaluations: "+realEvaluations+"\n");
		  sb.append("standardDeviation: "+rsa.getStandardDeviation()+"\n");
		  sb.append("standardError: "+rsa.getStandardError()+"\n");
		  sb.append("nTemplate: "+reac.getNEnabledConnections()+"\n");
		  
		  System.out.println(sb.toString());
			
	}

public static void setTestGraph(RDSystem system){
	OligoGraph<SequenceVertex,String> g;
	  if(reac == null){
		 reac = RDLibrary.rdstart;
	  }
	  system.setNetwork(reac);
		g = GraphMaker.fromReactionNetwork(reac);
		//For debug
		if(RDConstants.displayGraph) {
		  JFrame frame = new JFrame("Graph");
		  frame.add((new RNVisualizationViewerFactory()).createVisualizationViewer(reac));
		  frame.pack();
		  frame.setVisible(true);
		}
	  
	  g.exoConc = RDConstants.exoConc;
	  g.polConc = RDConstants.polConc;
	  g.nickConc = RDConstants.nickConc;
	  system.setOS(new OligoSystem<String>(g, new PadiracTemplateFactory(g)));
	  
	}


	
	protected static <T> T loadNewInstance(String className){
		T object = null;
		try {
			Class<?> cls = Class.forName(className.trim());

			object = (T) cls.newInstance();

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			System.err.println("Class not found: "+className+" . Please check spelling");
		}

		return object;
	}
	
	public static boolean[][] setTarget(String name){
		switch(name) {
		case "top":
			return RDPatternFitnessResultIbuki.getTopLine();
		case "bottom":
			return RDPatternFitnessResultIbuki.getBottomLine();
		case "left":
			return RDPatternFitnessResultIbuki.getLeftLine();
		case "right":
			return RDPatternFitnessResultIbuki.getRightLine();
		case "center":
			return RDPatternFitnessResultIbuki.getCenterLine();
		case "center-top":
			return RDPatternFitnessResultIbuki.getTopCenterLine();
		case "center-bottom":
			return RDPatternFitnessResultIbuki.getBottomCenterLine();
		case "smiley":
			return RDPatternFitnessResultIbuki.getSmileyFace();
		case "disk":
			return RDPatternFitnessResultIbuki.getDisk();
		case "circle":
			return RDPatternFitnessResultIbuki.getCircle();
			
		}
		
		return null;
	}

}
