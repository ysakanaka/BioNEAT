package model;

public class SequenceDependent {

	//values at 42 degrees, under the current salt conditions
	
	//slowdown, given in the 5' to 3' direction FOR THE TEMPLATE
	//all values in kcal per mol
	
	public static double temperature = 315.15; //in Kelvin
	
	public static double r = 1.98720e-3; //kcal.mol-1.K-1 
	
	public static double dangleAAleft = - 0.52; // Means that the sequence on the template at the nick is AA, 
								   // and the left (input) signal is on it.
	public static double dangleAAright = -0.11; // Same, for the output
	
	public static double stackAA = -1.41;       // Both are on template
	
	public static double dangleACleft = - 0.91; 
	
	public static double dangleACright = 0.21; 

	public static double stackAC = - 2.11; 
	
	public static double dangleAGleft = - 0.55; 
	
	public static double dangleAGright = 0.06; 

	public static double stackAG = - 1.36; 
	
	public static double dangleATleft = - 0.50; 
	
	public static double dangleATright = - 0.07; 

	public static double stackAT = - 1.64; 
	
	public static double dangleCAleft = - 0.44; 
	
	public static double dangleCAright = - 0.74; 

	public static double stackCA = - 0.85; 
	
	public static double dangleCCleft = - 0.43; 
	
	public static double dangleCCright = - 0.27; 

	public static double stackCC = - 1.74; 
	
	public static double dangleCGleft = - 0.25; 
	
	public static double dangleCGright = 0.04; 

	public static double stackCG = - 1.21; 
	
	public static double dangleCTleft =  0.05; 
	
	public static double dangleCTright = - 0.45; 

	public static double stackCT = - 1.36; 
	
	public static double dangleGAleft = - 0.61; 
	
	public static double dangleGAright = - 0.90; 

	public static double stackGA = - 1.73; 
	
	public static double dangleGCleft = - 0.65; 
	
	public static double dangleGCright = - 0.23; 

	public static double stackGC = - 2.47; 
	
	public static double dangleGGleft = - 0.51; 
	
	public static double dangleGGright =- 0.38; 

	public static double stackGG = - 1.74; 
	
	public static double dangleGTleft =  0.55; 
	
	public static double dangleGTright = - 0.28; 

	public static double stackGT = - 2.11; 
	
	public static double dangleTAleft = - 0.61; 
	
	public static double dangleTAright = - 0.07; 

	public static double stackTA = - 0.46; 
	
	public static double dangleTCleft = - 0.53; 
	
	public static double dangleTCright = - 0.26; 

	public static double stackTC = - 1.73; 
	
	public static double dangleTGleft = - 0.54; 
	
	public static double dangleTGright =- 0.48; 

	public static double stackTG = - 0.85; 
	
	public static double dangleTTleft = - 0.10; 
	
	public static double dangleTTright = - 0.34; 

	public static double stackTT = - 1.41;
	
	static double getDangleLeft(char first, char second){
		double value = 0.0;
		switch(first){
		case 'A':
			switch(second){
			case 'A':
				value = dangleAAleft;
				break;
			case 'T':
				value = dangleATleft;
				break;
			case 'G':
				value = dangleAGleft;
				break;
			case 'C':
				value = dangleACleft;
				break;
			}
			break;
		case 'T':
			switch(second){
			case 'A':
				value = dangleTAleft;
				break;
			case 'T':
				value = dangleTTleft;
				break;
			case 'G':
				value = dangleTGleft;
				break;
			case 'C':
				value = dangleTCleft;
				break;
			}
			break;
		case 'G':
			switch(second){
			case 'A':
				value = dangleGAleft;
				break;
			case 'T':
				value = dangleGTleft;
				break;
			case 'G':
				value = dangleGGleft;
				break;
			case 'C':
				value = dangleGCleft;
				break;
			}
			break;
		case 'C':
			switch(second){
			case 'A':
				value = dangleCAleft;
				break;
			case 'T':
				value = dangleCTleft;
				break;
			case 'G':
				value = dangleCGleft;
				break;
			case 'C':
				value = dangleCCleft;
				break;
			}
			break;
		}
		
		return value;
	}
	
	static double getDangleRight(char first, char second){
		double value = 0.0;
		switch(first){
		case 'A':
			switch(second){
			case 'A':
				value = dangleAAright;
				break;
			case 'T':
				value = dangleATright;
				break;
			case 'G':
				value = dangleAGright;
				break;
			case 'C':
				value = dangleACright;
				break;
			}
			break;
		case 'T':
			switch(second){
			case 'A':
				value = dangleTAright;
				break;
			case 'T':
				value = dangleTTright;
				break;
			case 'G':
				value = dangleTGright;
				break;
			case 'C':
				value = dangleTCright;
				break;
			}
			break;
		case 'G':
			switch(second){
			case 'A':
				value = dangleGAright;
				break;
			case 'T':
				value = dangleGTright;
				break;
			case 'G':
				value = dangleGGright;
				break;
			case 'C':
				value = dangleGCright;
				break;
			}
			break;
		case 'C':
			switch(second){
			case 'A':
				value = dangleCAright;
				break;
			case 'T':
				value = dangleCTright;
				break;
			case 'G':
				value = dangleCGright;
				break;
			case 'C':
				value = dangleCCright;
				break;
			}
			break;
		}
		
		return value;
	}
	
	static double getStack(char first, char second){
		double value = 0.0;
		switch(first){
		case 'A':
			switch(second){
			case 'A':
				value = stackAA;
				break;
			case 'T':
				value = stackAT;
				break;
			case 'G':
				value = stackAG;
				break;
			case 'C':
				value = stackAC;
				break;
			}
			break;
		case 'T':
			switch(second){
			case 'A':
				value = stackTA;
				break;
			case 'T':
				value = stackTT;
				break;
			case 'G':
				value = stackTG;
				break;
			case 'C':
				value = stackTC;
				break;
			}
			break;
		case 'G':
			switch(second){
			case 'A':
				value = stackGA;
				break;
			case 'T':
				value = stackGT;
				break;
			case 'G':
				value = stackGG;
				break;
			case 'C':
				value = stackGC;
				break;
			}
			break;
		case 'C':
			switch(second){
			case 'A':
				value = stackCA;
				break;
			case 'T':
				value = stackCT;
				break;
			case 'G':
				value = stackCG;
				break;
			case 'C':
				value = stackCC;
				break;
			}
			break;
		}
		
		return value;
	}
	
	private static char compl(char mychar){
		char comp = ' ';
		switch(mychar){
		case 'A':
			comp = 'T';
			break;
		case 'T':
			comp = 'A';
			break;
		case 'G':
			comp = 'C';
			break;
		case 'C':
			comp = 'G';
			break;
		}
		return comp;
	}
	
	/**
	 * 
	 * @param inputSeq the ATGC sequence of the input
	 * @param outputSeq the ATGC sequence of the output
	 * @return
	 */
	public static double getInputSlowdown(String inputSeq, String outputSeq){
		char first, second;
		second = SequenceDependent.compl(inputSeq.charAt(inputSeq.length()-1));
		first = SequenceDependent.compl(outputSeq.charAt(0));
		double value = SequenceDependent.getDangleLeft(first, second);
		return Math.exp(value/(r*temperature));
	}
	
	/**
	 * 
	 * @param inputSeq the ATGC sequence of the input
	 * @param outputSeq the ATGC sequence of the output
	 * @return
	 */
	public static double getOutputSlowdown(String inputSeq, String outputSeq){
		char first, second;
		second = SequenceDependent.compl(inputSeq.charAt(inputSeq.length()-1));
		first = SequenceDependent.compl(outputSeq.charAt(0));
		double value = SequenceDependent.getDangleRight(first, second);
		return Math.exp(value/(r*temperature));
	}
	
	/**
	 * 
	 * @param inputSeq the ATGC sequence of the input
	 * @param outputSeq the ATGC sequence of the output
	 * @return
	 */
	public static double getStackSlowdown(String inputSeq, String outputSeq){
		char first, second;
		second = SequenceDependent.compl(inputSeq.charAt(inputSeq.length()-1));
		first = SequenceDependent.compl(outputSeq.charAt(0));
		double value = SequenceDependent.getStack(first, second);
		return Math.exp(value/(r*temperature));
	}
}
