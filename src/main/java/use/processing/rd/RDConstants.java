package use.processing.rd;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

import com.google.common.base.Strings;

public class RDConstants {
	
    public static boolean debug = false;
    public static boolean timing = true;

	public static int maxTimeEval = 3000; //in time steps
	public static int bigTimeStep = 100; //Save concentrations every big time step for temporal fitness
	public static double timePerStep = 0.1;
	public static float spaceStep = 2.0f;
	public static int hsize = 160;
	public static int wsize = 160;
	
	public static int maxBeads = 500;
	public static double beadScale = 1e0;
    public static float beadRadius = 10.0f;
    public static boolean beadExclusion = true;
    public static double bounceRatePerBead = beadScale*1.4; //value is pretty random, means 30% increase per bead
	

	public static boolean wrap = false; //whether we wrap around the diffusion arena
	public static float fastDiff = 0.1f;
	public static float slowDiff = 0.01f;
	public static float initConc = 1.0f;
	public static float ratio = 1; //what fraction of the area should be spiked (i.e. activation on the side versus full act.
	
	public static float concScale = 10.0f; //for display, concentration scaling. [c] > scale saturates the color
	public static float greyTargetScale = 0.3f; //To display the target area in grey
	public static boolean showBeads = false; //Display beads instead of chemicals
	
	public static boolean gradients = true;
	public static float concChemostat = 100.0f;//for gradients
	public static double gradientScale = 4e-2; //to stabilize the gradient concentrations, equal to sqrt(exo/diff)
	public static int glueIndex = 2; //which species is the glue?
	public static int speciesOffset = 2; //to prevent seeing gradients or some basic species
	
	public static boolean useGlueAsTarget = true; //If true, then use concentration of glue (as defined by the glue index) instead of bead position
	public static float cutOff = 5e0f; //Below this concentration, beads are unlikely to aggregate (although, K dependent...) so not taken into account
	
	public static double polConc = 1.0;
	public static double nickConc = 1.0;
	public static double exoConc = 1.0;
	
	public static int nicenessGUIEval = 0; //MAX POWER
	public static int nicenessEvoEval = 20; //Multiple evals in parallel, so we should leave space for the others
	
	public static String[] gradientsName = {"a", "b"};
	
	public static boolean useMatchFitness = true; // Check how good we cover the pattern, rather than using a distance based approach. Useful if we can barely cover the pattern.
	public static double matchBonus = 1.0;
	public static double matchPenalty = -0.1; // we expect a lot more penalties
	public static boolean useHellingerDistance = true; // false means hamming. Only used if not match fitness
	public static boolean patternHellinger = true; // true means we care about the actual values rather than coverage
	public static int horizontalBins = 80; //used to bin the Hellinger distance. Too little bins mean no pattern coverage. Too much means too much penalty for holes
	public static int verticalBins = 80;
	
	public static int coverageHBins = 7; //used to bin the lexicographic fitness. Makes no sense to be the same as Hellinger
	public static int coverageVBins = 7;
	public static boolean evalRandomDistance = true; //before starting, check the distance between a random dist and the pattern.
	public static double defaultRandomFitness = 2.0; //if not, use this one
	//public static int trials = 10; //Average over how many attempts?
	
	public static boolean ceilingNodes = true; // Do we enforce a maximum graph size?
	public static boolean ceilingTemplates = false; // Do we enforce a maximum number of templates?
	public static boolean useMaxTotalNodes = true; // Do we then check the total number of nodes or just activator nodes
	public static int maxNodes = 6;
	public static int maxTemplates = 12;
	public static boolean hardTrim = true; //If true, any species that is not produced by the system is removed from the
	//individual. This is may prevent some specific patterns that affect a system's "initialization" phase, but will also
	//greatly prevent bloating
	
	public static double ratioValidity = 0.5; //Coverage ratio of target to consider a pattern "valid"
	public static double factorInOut = 1.0; //To be valid, we need in > factor * out
	
	//For evolution
	public static int weightDisableTemplate = 3;
	public static int weightMutateParameter = 20;
	public static int weightAddNodeWithGradients = 1;
	public static int weightAddActivationWithGradients = 3;
	public static int weightAddInhibitionWithGradients = 1;
	
	
	public static int populationSize = 50;
	public static int maxGeneration = 200;
	public static int reEvaluation = 1; // number of time an individual is reevaluated (multiplies the number of evaluations, obviously)
	public static boolean useMedian= false; //if not, use the worst individual
	public static boolean sampleUntilMeanConvergence = false; // if not, simply do reEvalution. If true, keep reevaluating until standard error is below a threshold
	public static double standardErrorThreshold = 0.005;
	public static int maxReEvaluation = 15;
	
	//public static double comparisonThreshold = 0.0; //Since we are manipulating doubles, we want to make sure fitnesses are different enough to sort them
	public static boolean useNatBlurFitness = false;
	public static boolean useEuclDistance = true; // In case of Nat's version of blur fitness, we base the dist matrix on the euclidian distance (if not, infinite distance = max of two differences)
	public static String targetName = "undefined";
	
	//gui for outside interface
	public static boolean displayGraph = true;
	
	
	
	//TODO add a function to read parameters from outside
	
	public static String configsToString(){
		return configsToString(RDConstants.class);
	}
	
	public static String configsToString(Class<?> subConfigClass){
		StringBuilder sb = new StringBuilder();
		sb.append(new Date(System.currentTimeMillis()).toString()+"\n");
		
		Field[] f = subConfigClass.getFields();
		
		
		for(int i=0; i<f.length; i++){
			try {
				if(f[i].getType().isArray()){
					sb.append(f[i].getName()+"="+Arrays.deepToString((Object[]) f[i].get(null))+"\n");
				} else {
					sb.append(f[i].getName()+"="+f[i].get(null)+"\n"); //Only valid for static methods
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * A parameter setting is formated as parameterName = parameterValue
	 * @param subConfigClass the class to update
	 * @param parameterName
	 * @param parameterValue
	 */
	public static void readConfigFromString(Class<?> subConfigClass, String parameterName, String parameterValue){
		Field[] fields = subConfigClass.getFields();
			
			
			for(int j = 0; j<fields.length; j++){
				if(fields[j].getName().trim().equals(parameterName.trim())){
					//We found the field, update the parameter
					//Now we need to parse correctly the parameter's value
					try {
						switch(fields[j].getType().toString()){
							case "Integer":
							case "int":
						
								fields[j].setInt(null, Integer.parseInt(parameterValue.trim()));
								break;
							case "Float":
							case "float":
						
								fields[j].setFloat(null, Float.parseFloat(parameterValue.trim()));
								break;
							case "Double":
							case "double":
						
								fields[j].setDouble(null, Double.parseDouble(parameterValue.trim()));
								break;
							case "Boolean":
							case "boolean":
						
								fields[j].setBoolean(null, Boolean.parseBoolean(parameterValue.trim()));
								break;
							case "String":
							case "class java.lang.String":
								fields[j].set(null, parameterValue.trim());
								break;
							case "String[]":
							case "class [Ljava.lang.String;":
								String base = parameterValue.trim(); // format: [ , ... , ]
								base = base.substring(1, base.length()-1); //removed surrounding brackets
								String[] vals = base.split("\\s*,\\s*");
								fields[j].set(null, vals);
								break;
							default:
								System.out.println(fields[j].getType().toString());
						
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//fields[j].set(null, );
				}
			}
		
	}
	
	public static void main(String[] args){
		System.out.println(configsToString());
	}
	
}
