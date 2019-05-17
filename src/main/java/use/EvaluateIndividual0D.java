package use;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JFrame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import erne.Constants;
import erne.Individual;
import model.OligoGraph;
import model.OligoSystem;
import model.OligoSystemComplex;
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
import utils.GraphMaker;
import utils.PadiracTemplateFactory;
import utils.RDLibrary;
import utils.RunningStatsAnalysis;

public class EvaluateIndividual0D {

	public static String outputSuffix = "test.txt";
	public static ReactionNetwork reac = null; 

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
			System.out.println("USAGE: java [options] "+EvaluateIndividual0D.class.getName()+" parameters");
		}
		
		System.exit(0);
	}

	public static void evaluateIndividual(ReactionNetwork r, String outputfilename) {
		
		  StringBuilder sb = new StringBuilder("");
		  model.Constants.numberOfPoints = RDConstants.maxTimeEval;
		  OligoSystemComplex oc = new OligoSystemComplex(reac);
		  Map<String, double[]> results = oc.calculateTimeSeries();
		  String key = results.keySet().iterator().next(); //Take the first individual
		  double[] vals = results.get(key);
		  for (int i = 0; i< vals.length; i++) {
			  sb.append(vals[i]+"\n");
		  }
		  System.out.println(sb.toString());
			
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
		default:
			System.err.println("WARNING: incorrect pattern name");
			return RDPatternFitnessResultIbuki.getCenterLine();
			
		}
		
	}

}
