package erne.algorithm.bioNEAT;

import erne.Population;
import erne.PopulationFactory;
import reactionnetwork.ReactionNetwork;

public class BioNEATCMAESPopulationFactory extends PopulationFactory {

	@Override
	public Population createPopulation(int popSize, ReactionNetwork startingNetwork) {
		return new BioNEATCMAESPopulation(popSize, startingNetwork);
	}

}
