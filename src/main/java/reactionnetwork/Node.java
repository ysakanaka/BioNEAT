package reactionnetwork;

public class Node {

	public static int SIMPLE_SEQUENCE = 1;
	public static int INHIBITING_SEQUENCE = 2;

	public String name;
	public double parameter;
	public double initialConcentration;
	public int type;
	public boolean protectedSequence = false;
	public String DNAString="";
	public boolean reporter = false;

	public Node(String name) {
		this.name = name;
	}
}
