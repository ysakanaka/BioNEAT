package use;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import erne.AbstractFitnessFunction;
import erne.Constants;
import erne.Evolver;
import erne.FitnessDisplayer;
import reactionnetwork.Library;
import reactionnetwork.ReactionNetwork;
import use.math.gaussian.GaussianFitnessDisplayer;
import use.processing.rd.RDConstants;

public class Run {
	
	public final static String fitnessFunctionParameter = "fitnessFunction";
	public final static String fitnessDisplayerParameter = "fitnessDisplayer";
	public final static String reactionNetworkParameter = "initialNetwork";
	
	public String fitnessFunctionName = "use.math.gaussian.GaussianFitnessFunction";
	public String fitnessDisplayerName = "use.math.gaussian.GaussianFitnessDisplayer";
	public String initReactionNetworkName = "reactionnetwork.Library.startingMath";
	
	protected String configFile;
	
	protected AbstractFitnessFunction function= null;
	protected ReactionNetwork init = null;
	protected FitnessDisplayer displayer = null;

	/**
	 * Has one arg: run parameters
	 * Mandatory parameters: fitness function name
	 * @param args
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args)  {
		if(args.length < 1){
			System.out.println("Usage: java [options] "+Run.class.getName()+" parameters");
			System.exit(0);
		}
		Run run = null;
		try {
			run = initRun(args[0]);
			System.out.println("STUB: initialization done");
		} catch (ClassNotFoundException | InterruptedException | ExecutionException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//testParameterSetsInConfigClasses()
		testFitnessClassesCorrectlyLoaded(run);
		try {
			run.doRun();
		} catch (ClassNotFoundException | IOException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected static Run initRun(String configFile) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException{
		Run run = new Run();
		run.configFile = configFile;
		BufferedReader br = new BufferedReader(new FileReader(configFile));
		String line;
		
		while((line = br.readLine()) != null){
			String[] paramPair = line.split("\\s*=\\s*");
			String trimmedParamName = paramPair[0].trim();
			if(trimmedParamName.startsWith("#") || paramPair.length != 2) continue; //Incorrect format or comment
			
			if(trimmedParamName.equals(fitnessFunctionParameter)){ // special parameter defining the fitness function
				//run.function = loadNewInstance(paramPair[1].trim());
			} else if(trimmedParamName.equals(fitnessDisplayerParameter)) {
				//run.displayer = loadNewInstance(paramPair[1].trim());
			} else if(trimmedParamName.equals(reactionNetworkParameter)) {
				//run.init = loadStaticFieldValue(paramPair[1].trim());
			}else { // Standard parameters
				RDConstants.readConfigFromString(Constants.class,trimmedParamName, paramPair[1]);
				RDConstants.readConfigFromString(RDConstants.class,trimmedParamName, paramPair[1]);
			}
		}
		run.function = loadNewInstance(run.fitnessFunctionName);
		run.displayer = loadNewInstance(run.fitnessDisplayerName);
		run.init = loadStaticFieldValue(run.initReactionNetworkName);
		br.close();
		return run;
	}
	
	protected void doRun() throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
		Evolver evolver = new Evolver(init, function, displayer);
		evolver.setGUI(Evolver.hasGUI(configFile));
		evolver.setExtraConfig(RDConstants.configsToString());
		evolver.evolve();
        System.out.println("Evolution completed.");
        if(!Evolver.hasGUI(configFile)) System.exit(0);
	}
	
	@SuppressWarnings("unchecked")
	protected static <T> T loadNewInstance(String className, Object... initargs){
		T object = null;
		try {
		    Class<?> cls = Class.forName(className.trim());
		    	
			object = (T) cls.getDeclaredConstructor().newInstance(initargs);
		   
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
		    System.err.println("Class not found: "+className+" . Please check spelling");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return object;
	}
	
	@SuppressWarnings("unchecked")
	protected static <T> T loadStaticFieldValue(String fieldName){
		T object = null;
		int index =  fieldName.lastIndexOf(".", fieldName.length()-1);
		if(index < 0){
			System.err.println("Invalid field name: "+fieldName+" . Please check spelling");
		}
		String className = fieldName.substring(0,index);
		try {
			//Find the last dot, to get the class name
			
			String actualName = fieldName.substring(index+1);
		    Class<?> cls = Class.forName(className.trim());
		    	
		    	object = (T) cls.getField(actualName).get(null);
		   
		} catch (ClassNotFoundException  | IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
			System.err.println("Class not found: "+className+" . Please check spelling");
			e.printStackTrace();
		}
		
		return object;
	}
	
	/** Checks that parameters read are correctly set into the RDConstants config class and Constants config class.
	 * 
	 */
	private static void testParameterSetsInConfigClasses(){
		System.out.println(RDConstants.configsToString());
		System.out.println(RDConstants.configsToString(Constants.class));
	}
	
	private static void testFitnessClassesCorrectlyLoaded(Run run){
		if(run == null){
			System.err.println("testFitnessClassesCorrectlyLoaded failed due to null Run");
			return;
		}
		if(run.function!=null) System.out.println(run.function.getClass().getName());
		if(run.displayer!=null) System.out.println(run.displayer.getClass().getName());
		if(run.init!=null) System.out.println(run.init.toString());
	}
}
