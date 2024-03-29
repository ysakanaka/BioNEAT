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
import use.processing.features.RDFeature;
import use.processing.multiobjective.RDInObjective;
import use.processing.multiobjective.RDObjective;
import use.processing.multiobjective.RDOutObjective;
import use.processing.rd.PatternEvaluator;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessResult;
import use.processing.rd.RDPatternFitnessResult;
import use.processing.rd.RDPatternFitnessResultIbuki;
import use.processing.rd.RDSystem;
import use.processing.rd.RDSystemApprox;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;
import utils.RDLibrary;
import utils.RunningStatsAnalysis;

public class EvaluateIndividualWithDescriptors {

	public static String outputSuffix = "test.txt";
	public static ReactionNetwork reac = null; 
	public static String[] targetNames = {"center"};
	public static boolean[][][] targets = null; //TODO should be moved into a general fitness class
	public static double width = 0.3; //TODO should be moved into a general fitness class
	public static ArrayList<RDFeature> features = new ArrayList<RDFeature>();
	public static boolean useApprox = false;

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
						//target = setTarget(paramPair[1].trim().toLowerCase());
						targetNames = paramPair[1].trim().toLowerCase().split(",");
					} else if(trimmedParamName.equals("width")) {
					    width = Double.parseDouble(paramPair[1].trim());
					}else if(trimmedParamName.equals("useApprox")) {
						useApprox = Boolean.parseBoolean(paramPair[1].trim());
					}
					else {
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
		RDConstants.maxBeads = 500;
		RDPatternFitnessResultIbuki.width = width;
		targets = new boolean[targetNames.length][][];
		RunningStatsAnalysis[] rsa = new RunningStatsAnalysis[targets.length];
		for(int i = 0; i< targets.length; i++ ) {
			targets[i] = setTarget(targetNames[i].trim());
			rsa[i] = new RunningStatsAnalysis();
		}
		RDPatternFitnessResultIbuki.weightExponential = 0.1;
		//RDConstants.matchPenalty=-0.1;
		  StringBuilder sb = new StringBuilder("");
		  RDPatternFitnessResult[] fitness = new RDPatternFitnessResult[targets.length];
		  RDInObjective inObjective = new RDInObjective();
		  RDOutObjective outObjective = new RDOutObjective();
		  if (RDConstants.debug) System.out.println("GradientNames: ['"+RDConstants.gradientsName[0]+"', '"+RDConstants.gradientsName[1]+"']");
		  int realEvaluations = useApprox?1:RDConstants.reEvaluation;
		  for(int i = 0; i<realEvaluations;i++){
			  RDSystem system = useApprox? new RDSystemApprox(): new RDSystem();
			  setTestGraph(system);
			  system.init(false); //with full power, because we are doing parallel eval (maybe)
			  for(int j = 0; j<RDConstants.maxTimeEval; j++) system.update();
			  
			  boolean[][] glue = PatternEvaluator.detectGlue(system.conc[RDConstants.glueIndex]);
			  
			  for(int k = 0; k< targets.length; k++ ) {
				  fitness[k] = new RDPatternFitnessResultIbuki(system.conc,targets[k],system.beadsOnSpot,0.0);
				  rsa[k].addData(fitness[k].getFitness());
			  
				  sb.append("fitness"+i+"_"+k+": "+fitness[k]+"\n");
				  sb.append("meansofar"+i+"_"+k+": "+rsa[k].getMean()+"\n");
				  sb.append("sdsofar"+i+"_"+k+": "+rsa[k].getStandardDeviation()+"\n");
				  sb.append("sesofar"+i+"_"+k+": "+rsa[k].getStandardError()+"\n");
				  sb.append("in"+i+"_"+k+": "+inObjective.evaluateScore(r, targets[k], glue)+"\n");
				  sb.append("out"+i+"_"+k+": "+(1.0-outObjective.evaluateScore(r, targets[k], glue))+"\n");
				  sb.append("hellinger"+i+"_"+k+": "+PatternEvaluator.hellingerDistance(system.conc[RDConstants.glueIndex], targets[k])+"\n");
			  }
			  //moving goal post
			  if(!useApprox && i == realEvaluations -1 && RDConstants.sampleUntilMeanConvergence 
					  && realEvaluations < RDConstants.maxReEvaluation && rsa[0].getStandardError() > RDConstants.standardErrorThreshold) {
				  realEvaluations++;
			  }
		  }
		  sb.append("nEvaluations: "+realEvaluations+"\n");
		  for(int k = 0; k< targets.length; k++ ) {
		    sb.append("standardDeviation"+k+": "+rsa[k].getStandardDeviation()+"\n");
		    sb.append("standardError"+k+": "+rsa[k].getStandardError()+"\n");
		  }
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
		case "smiley-eyes":
			return RDPatternFitnessResultIbuki.getSmileyEyes();
		case "smiley-mouth":
			return RDPatternFitnessResultIbuki.getSmileyMouth();
		case "disk":
			return RDPatternFitnessResultIbuki.getDisk();
		case "circle":
			return RDPatternFitnessResultIbuki.getCircle();
		case "t":
			return RDPatternFitnessResultIbuki.getTPattern();
		case "reversed-t":
			return RDPatternFitnessResultIbuki.getReversedTPattern();
		case "top-bottom":
			return RDPatternFitnessResultIbuki.getTopBottomLines();
		case "ko":
			return RDPatternFitnessResultIbuki.getKoKanji();
		default:
			System.err.println("WARNING: incorrect pattern name: " + name);
			return RDPatternFitnessResultIbuki.getCenterLine();
			
		}
		
	}

}
