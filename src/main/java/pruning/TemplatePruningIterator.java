package pruning;

import reactionnetwork.ReactionNetwork;

import java.util.ArrayList;

import reactionnetwork.Connection;
import reactionnetwork.Library;

public class TemplatePruningIterator extends PruningRuleIterator {
	
	public ArrayList<Connection> potentialTarget;
	
	public TemplatePruningIterator(ReactionNetwork toIterate) {
		super(toIterate);
		potentialTarget = new ArrayList<Connection>();
		for(Connection c : base.connections){
			if(c.enabled){
				potentialTarget.add(c);
			}
		}
	}

	@Override
	public boolean hasNext() {
		return !potentialTarget.isEmpty();
	}

	@Override
	public ReactionNetwork next() {
		Connection c = potentialTarget.get(0);
		potentialTarget.remove(0);
		ReactionNetwork rn = base.clone();
		rn.getConnectionByIN(c.innovation).enabled = false;
		return rn;
	}
	
	public static void main(String[] args){
		ReactionNetwork test = Library.oldGaussian;
		TemplatePruningIterator it = new TemplatePruningIterator(test);
		while(it.hasNext()){
			System.out.println("======================\n\n");
			System.out.println(it.next());
			System.out.println("\n\n");
		}
	}

}
