package erne.algorithm.bioNEAT;

import java.util.concurrent.ExecutionException;

import erne.Individual;
import erne.Population;
import erne.PopulationFactory;
import reactionnetwork.ReactionNetwork;

public class BioNEATPopulationFactory extends PopulationFactory {
	
	@Override
	public Population createPopulation(int popSize, ReactionNetwork startingNetwork){
		return new BioNEATPopulation(popSize, startingNetwork);
	}

	
	
}
