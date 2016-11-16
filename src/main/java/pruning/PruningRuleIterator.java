package pruning;

import java.util.Iterator;

import reactionnetwork.ReactionNetwork;

public class PruningRuleIterator implements Iterator<ReactionNetwork>{
	
	
	
	protected ReactionNetwork base;
	
	public PruningRuleIterator(ReactionNetwork toIterate){
		base = toIterate.clone();
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ReactionNetwork next() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
