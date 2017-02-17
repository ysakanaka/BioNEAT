package use.processing.cmaes;

import java.util.ArrayList;

import erne.AbstractFitnessFunction;
import optimizers.cmaes.fitness.IObjectiveFunction;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;

public class RDCMAES  implements IObjectiveFunction, Runnable{
	
	protected double[] baseGenome; // values of the parameters in structure: stability + concentration, i.e. the same as in BioNEAT
	protected ReactionNetwork structure;
	protected AbstractFitnessFunction fitnessFunction;
	protected ArrayList<Node> orderedNodes = new ArrayList<Node>(); //so that we keep the same order when trying to interpret the genome
	protected ArrayList<Connection> orderedConnections = new ArrayList<Connection>();
	
	public RDCMAES(ReactionNetwork structure, AbstractFitnessFunction fitnessFunction){
		this.structure = structure.clone();
		this.fitnessFunction = fitnessFunction;
		initGenome();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public double[] getBaseGenome(){
		return baseGenome;
	}
	
	public ReactionNetwork getStructure(){
		return structure; // may not be in sync with genome
	}
	
	protected void initGenome(){
		//first, find the parameter space: 1 for each signaling strand + 1 for each template
		int size = 0;
		for(Node n : structure.nodes){
			if(n.type == Node.SIMPLE_SEQUENCE){ 
				size ++;
				orderedNodes.add(n);
			}
		}
		for(Connection c : structure.connections){
			if(c.enabled){
				size++;
				orderedConnections.add(c);
			}
		}
		baseGenome = new double[size];
		//then, we set the values in the genome
		for(int i = 0; i<orderedNodes.size(); i++) baseGenome[i] = orderedNodes.get(i).parameter;
		for(int i = 0; i<orderedConnections.size(); i++) baseGenome[orderedNodes.size()+i] = orderedConnections.get(i).parameter;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFeasible(double[] arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double valueOf(double[] arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
