package use.ready.mutation.rules;

import java.util.HashSet;

import erne.Individual;
import erne.mutation.MutationRule;
import reactionnetwork.Connection;
import use.ready.ReadyReactionNetwork;

public class BeadMerge extends MutationRule {

	public BeadMerge(int weight) {
		super(weight);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Individual mutate(Individual indiv) {
		if(ReadyReactionNetwork.class.isAssignableFrom(indiv.getNetwork().getClass())){
			ReadyReactionNetwork rrn = (ReadyReactionNetwork) indiv.getNetwork();
			if (rrn.templateOnBeads.size() > 1){
				int index1 = rand.nextInt(rrn.templateOnBeads.size());
				int index2 = rand.nextInt(rrn.templateOnBeads.size()-1);
				if (index2 >= index1) index2++;
				rrn.templateOnBeads.get(index1).addAll(rrn.templateOnBeads.get(index2));
				rrn.templateOnBeads.remove(index2);
			}
		}
		return indiv;
	}

}
