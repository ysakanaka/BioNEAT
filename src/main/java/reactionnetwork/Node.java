package reactionnetwork;

import java.io.Serializable;

import erne.Individual;
import erne.util.Randomizer;

public class Node implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int SIMPLE_SEQUENCE = 1;
	public static int INHIBITING_SEQUENCE = 2;

	public String name;
	public double parameter;
	public double initialConcentration = 0;
	public int type = SIMPLE_SEQUENCE;
	public boolean protectedSequence = false;
	public String DNAString = "";
	public boolean reporter = false;
	public boolean hasPseudoTemplate = false;
	public double pseudoTemplateConcentration = Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue);

	public Node(String name) {
		this.name = name;
	}
	
	public Node(String name, int type) {
		this.name = name;
		this.type = type;
	}
	
}
