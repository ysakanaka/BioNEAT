package erne;

public class Constants {

	public static boolean autoSpeciationThreshold = true;
	public static int targetNSpecies = 20;
	public static double speciationThresholdMod = 0.1;
	public static double speciationThresholdMin = 0.03;
	public static double probMutationOnly = 0.5;
	public static int tournamentSize = 5;
	public static double defaultSpeciationThreshold = 0.4;
    public static int maxEvalTime = 4000; //in steps
	public static int maxEvalClockTime = 30; //in seconds; -1 means no time out
	public static double comparisonThreshold = 0.0; //for multiobjective and lexicographic fitnesses.
	public static boolean debug = false;
	public static boolean stabilityCheck = false;
}
