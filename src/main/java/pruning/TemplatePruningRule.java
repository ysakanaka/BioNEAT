package pruning;

import reactionnetwork.ReactionNetwork;

public class TemplatePruningRule implements PruningRule {

	@Override
	public PruningRuleIterator getIterator(ReactionNetwork rn) {
		
		return new TemplatePruningIterator(rn);
	}

}
