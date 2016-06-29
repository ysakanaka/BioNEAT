package use.ready.mutation.rules;

import java.util.HashSet;

import erne.Individual;
import erne.mutation.MutationRule;
import reactionnetwork.Connection;
import use.ready.ReadyReactionNetwork;

public class BeadSplit extends MutationRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2982497658574155715L;
	public double probTemplateDuplication = 0.1;
	public double probTemplateMove = 0.5;
	

	public BeadSplit(int weight) {
		super(weight);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Individual mutate(Individual indiv) {
		if(ReadyReactionNetwork.class.isAssignableFrom(indiv.getNetwork().getClass())){
			ReadyReactionNetwork rrn = (ReadyReactionNetwork) indiv.getNetwork();
			
			int index = rand.nextInt(rrn.templateOnBeads.size());
			HashSet<Connection> bead = new HashSet<Connection>();
			HashSet<Connection> toRemove = new HashSet<Connection>();
			for (Connection conn : rrn.templateOnBeads.get(index)){
				if (rand.nextDouble() < probTemplateMove){
					bead.add(conn);
					if(rand.nextDouble() >= probTemplateDuplication) toRemove.add(conn);
				}
			}
			rrn.templateOnBeads.get(index).removeAll(toRemove);
			if (bead.size() > 0) rrn.templateOnBeads.add(bead);
			if (rrn.templateOnBeads.get(index).size() == 0) rrn.templateOnBeads.remove(index);
			
		}
		return indiv;
	}

	public static void main(String[] args){
		
	}
	
}
