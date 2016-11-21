package use.processing.rd;

public class RDConstants {
	
    public static boolean debug = false;

	public static int maxTimeEval = 15000; //in time steps
	public static double timePerStep = 1.0;
	public static float spaceStep = 2.0f;
	public static int hsize = 160;
	public static int wsize = 160;
	
	public static int maxBeads = 100;
	public static double beadScale = 1e0;
    public static float beadRadius = 10.0f;
	

	public static boolean wrap = false; //whether we wrap around the diffusion arena
	public static float fastDiff = 0.1f;
	public static float slowDiff = 0.01f;
	public static float initConc = 1.0f;
	public static float ratio = 1; //what fraction of the area should be spiked (i.e. activation on the side versus full act.
	
	public static float concScale = 1.0f; //for display, concentration scaling. [c] > scale saturates the color
	public static float greyTargetScale = 0.3f; //To display the target area in grey
	public static boolean showBeads = true; //Display beads instead of chemicals
	
	public static boolean gradients = true;
	public static float concChemostat = 100.0f;//for gradients
	public static double gradientScale = 4e-2; //to stabilize the gradient concentrations, equal to sqrt(exo/diff)
	public static int glueIndex = 2; //which species is the glue?
	public static int speciesOffset = 2; //to prevent seeing gradients or some basic species
	
	public static double polConc = 1.0;
	public static double nickConc = 1.0;
	public static double exoConc = 1.0;
	
	public static int nicenessGUIEval = 0; //MAX POWER
	public static int nicenessEvoEval = 20; //Multiple evals in parallel, so we should leave space for the others
	
	public static String[] gradientsName = {"a", "b"};
	
	public static boolean useMatchFitness = true; // Check how good we cover the pattern, rather than using a distance based approach. Useful if we can barely cover the pattern.
	public static boolean evalRandomDistance = true; //before starting, check the distance between a random dist and the pattern.
	public static double defaultRandomFitness = 2.0; //if not, use this one
	public static int trials = 10; //Average over how many attempts?
	//TODO add a function to read parameters from outside
	
}
