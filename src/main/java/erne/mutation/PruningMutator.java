package erne.mutation;

import java.util.ArrayList;

import erne.Constants;
import erne.Individual;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import utils.RDLibrary;

/**
 * Mutator class that automatically removes useless nodes
 */
public class PruningMutator extends Mutator {

	public PruningMutator(ArrayList<MutationRule> mutationRules) {
		super(mutationRules);
	}

	public Individual mutate(Individual indiv) {
		Individual mut = super.mutate(indiv);
		
		prune(mut);
		return mut;
	}
	
	/**
	 * Prunes in place. Returned as well for checking purposes
	 * @param mut
	 * @return
	 */
	public static Individual prune(Individual mut){
		//Algorithm: breath first. 
				// 1. Check that every node is protected OR has an incoming connection.
				// 2. Remove unused nodes (and connections relating to them)
				// 3. Repeat until no node has been removed
				
				ArrayList<Node> toRemove;
				ArrayList<Connection> toRemoveConnection;
				ReactionNetwork rn = mut.getNetwork();
				boolean doingIt = false;
				
				do{
					toRemove = new ArrayList<Node>();
					toRemoveConnection = new ArrayList<Connection>();
					for (Node n : rn.nodes){
						if(n.protectedSequence || n.reporter) continue;
						
						if(n.type == Node.INHIBITING_SEQUENCE){
							Node from;
							Node to;
							if(n.name.contains("T")){
								String[] names = n.name.substring(1).split("T");
								from = rn.getNodeByName(names[0]); // TODO: warning, very implementation dependent
								to = rn.getNodeByName(names[1]);
							} else {
							    from = rn.getNodeByName(""+n.name.charAt(1)); // TODO: warning, very implementation dependent
							    to = rn.getNodeByName(""+n.name.charAt(2));
							}
							if (from == null || to == null){
								toRemove.add(n);
								continue;
							}
							Connection inhibited = rn.getConnectionByEnds(from, to);
							if (inhibited == null || !inhibited.enabled){
								toRemove.add(n);
								continue;
							}
						}
						
						boolean toBeRemoved = true;
						for(Connection c : rn.connections){
							if(c.to.name.equals(n.name) && c.enabled){
								toBeRemoved = false;
								break;
							}
						}
						if(!toBeRemoved) continue;
						//could not find anything
						toRemove.add(n);
					}
					
					if(!toRemove.isEmpty()){
						if(!doingIt){
							doingIt = true;
							if(Constants.debug) System.out.println("before prunning: "+mut);
						}
		              for (Connection c : rn.connections){
						  if (toRemove.contains(c.from) || toRemove.contains(c.to)) toRemoveConnection.add(c);
					  }
					
					  rn.nodes.removeAll(toRemove);
					  rn.connections.removeAll(toRemoveConnection);
					}
				}while(!toRemove.isEmpty()); //we removed some stuff
				if(doingIt && Constants.debug) System.out.println("after prunning: "+mut);
				return mut;
	}
	
	public static void main(String[] args){
		PruningMutator.prune(new Individual(RDLibrary.rdstart));
	}
	
	private static final long serialVersionUID = 1L;

}
