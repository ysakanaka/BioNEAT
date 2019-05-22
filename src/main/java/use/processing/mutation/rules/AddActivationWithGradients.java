package use.processing.mutation.rules;

import java.util.ArrayList;

import edu.uci.ics.jung.graph.util.Pair;
import erne.Individual;
import erne.mutation.MutationRule;
import erne.util.Randomizer;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import use.processing.rd.RDConstants;

public class AddActivationWithGradients extends MutationRule {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double probGeneMutation = 0.8;
	
	

	public AddActivationWithGradients(int weight) {
		super(weight);
	}

	@Override
	public Individual mutate(Individual indiv) {
		ArrayList<Pair<Node>> possibleActivations = getPossibleActivations(indiv);
		if (possibleActivations.size() > 0) {
			int index = rand.nextInt(possibleActivations.size());
			Pair<Node> nodes = possibleActivations.get(index);
			Connection connection = indiv.getNetwork().getConnectionByEnds(nodes.getFirst(), nodes.getSecond());
			if (connection == null) {
				indiv.addConnection(nodes.getFirst(), nodes.getSecond(),
						Randomizer.getRandomLogScale(Individual.minTemplateValue, Individual.maxTemplateValue));
			} else {
				connection.enabled = true;
			}
		}
		return indiv;
	}
	
	protected boolean isGradient(Node node){
		for(int i = 0; i<RDConstants.gradientsName.length; i++){
			if(RDConstants.gradientsName[i].equals(node.name)) return true;
		}
		return false;
	}
	
	protected ArrayList<Pair<Node>> getPossibleActivations(Individual indiv){
		ArrayList<Pair<Node>> possibleActivations = new ArrayList<Pair<Node>>();
		for (Node from : indiv.getNetwork().nodes) {
			if (from.type != Node.INHIBITING_SEQUENCE) {
				for (Node to : indiv.getNetwork().nodes) {
					if( !isGradient(to))
					{
						Connection connection = indiv.getNetwork().getConnectionByEnds(from, to);
					
					    if (connection == null || !connection.enabled) {
						    possibleActivations.add(new Pair<Node>(from, to));
					    }
					}
				}
			}
		}
		return possibleActivations;
	}

	@Override
	public boolean isApplicable(Individual indiv) {
		
		return (RDConstants.ceilingTemplates && indiv.getNetwork().getNEnabledConnections() < RDConstants.maxTemplates) 
				|| (getPossibleActivations(indiv).size()>0);
	}
}
