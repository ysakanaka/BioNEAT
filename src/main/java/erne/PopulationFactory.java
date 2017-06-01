package erne;

import reactionnetwork.ReactionNetwork;

public abstract class PopulationFactory {

	
	public abstract Population createPopulation(int popSize, ReactionNetwork startingNetwork);
}
