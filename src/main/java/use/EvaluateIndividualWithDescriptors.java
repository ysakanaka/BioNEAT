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
import use.processing.rd.RDPatternFitnessResultIbuki;
import use.processing.rd.RDSystem;
import utils.GraphMaker;
import utils.PadiracTemplateFactory;
import utils.RDLibrary;

public class EvaluateIndividualWithDescriptors {

	public static String outputSuffix = "test.txt";
	public static ReactionNetwork reac = null; 
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
						
					RDConstants.readConfigFromString(Constants.class,trimmedParamName, paramPair[1]);
					RDConstants.readConfigFromString(RDConstants.class,trimmedParamName, paramPair[1]);
					
				}
				br.close();
				
				br = new BufferedReader(new FileReader(args[1]));
				reac = gson.fromJson(br, ReactionNetwork.class);

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (IOException e) {
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
		  RDFitnessResult fitness;
		  RDInObjective inObjective = new RDInObjective();
		  RDOutObjective outObjective = new RDOutObjective();
		  if (RDConstants.debug) System.out.println("GradientNames: ['"+RDConstants.gradientsName[0]+"', '"+RDConstants.gradientsName[1]+"']");
		  boolean[][] target = RDPatternFitnessResultIbuki.getCenterLine();
		  for(int i = 0; i<RDConstants.reEvaluation;i++){
			  RDSystem system = new RDSystem();
			  setTestGraph(system);
			  system.init(false); //with full power, because we are doing parallel eval (maybe)
		  for(int j = 0; j<RDConstants.maxTimeEval; j++) system.update();
		  fitness = new RDPatternFitnessResultIbuki(system.conc,target,system.beadsOnSpot,0.0);
		  sb.append("fitness"+i+": "+fitness+"\n");
		  sb.append("in"+i+": "+inObjective.evaluateScore(r, target, PatternEvaluator.detectGlue(system.conc[RDConstants.glueIndex]))+"\n");
		  sb.append("out"+i+": "+(1.0-outObjective.evaluateScore(r, target, PatternEvaluator.detectGlue(system.conc[RDConstants.glueIndex])))+"\n");
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

}
