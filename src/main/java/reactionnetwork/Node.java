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
	
	@Override
	public boolean equals(Object o){
		if(o.getClass()!=this.getClass()){
			return false;
		}
		Node n = (Node) o;
		return n.name.equals(name) && n.parameter == parameter && n.type == type && n.protectedSequence == protectedSequence &&
				n.hasPseudoTemplate == hasPseudoTemplate && n.pseudoTemplateConcentration == pseudoTemplateConcentration;
		
	}
	
	@Override
	public int hashCode(){
		int result = 347; //"large" prime number
		int prime = 37;
		result = prime * result + name.hashCode();
		long f = Double.doubleToLongBits(parameter);
		result = prime * result + (int) (f ^ (f>>>32));
		result = prime * result + (type-1);
		result = prime * result + (protectedSequence?1:0);
		result = prime * result + (hasPseudoTemplate?1:0);
		f = Double.doubleToLongBits(pseudoTemplateConcentration);
		result = prime * result + (int) (f ^ (f>>>32));
		return result;
	}
	
	public static void main(String[] args){
		Node a = new Node("a", 1);
		Node aprime = new Node("");
		aprime.name = a.name;
		aprime.type = a.type;
		aprime.pseudoTemplateConcentration = a.pseudoTemplateConcentration;
		
		Node b = new Node("a",1);
		
		Node c = new Node("b",1);
		
		Node d = new Node("a",2);
		
		System.out.println("Test 1: a versus aprime");
		System.out.println(a+" hash: "+a.hashCode()+"\n"+aprime+" hash: "+aprime.hashCode()+"\n"+"Equals: "+a.equals(aprime));
		
		System.out.println("Test 2: a versus b");
		System.out.println(a+" hash: "+a.hashCode()+"\n"+b+" hash: "+b.hashCode()+"\n"+"Equals: "+a.equals(b));
		
		System.out.println("Test 1: a versus c");
		System.out.println(a+" hash: "+a.hashCode()+"\n"+c+" hash: "+c.hashCode()+"\n"+"Equals: "+a.equals(c));
		
		System.out.println("Test 1: a versus d");
		System.out.println(a+" hash: "+a.hashCode()+"\n"+d+" hash: "+d.hashCode()+"\n"+"Equals: "+a.equals(d));
		
	}
	
}
