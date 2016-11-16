package pruning;

import reactionnetwork.ReactionNetwork;

public interface PruningRule {

	public PruningRuleIterator getIterator(ReactionNetwork rn);
	
}
