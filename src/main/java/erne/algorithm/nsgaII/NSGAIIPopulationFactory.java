package erne.algorithm.nsgaII;

import erne.Population;
import erne.PopulationFactory;
import reactionnetwork.ReactionNetwork;

public class NSGAIIPopulationFactory  extends PopulationFactory{
	
	public boolean superElitist = false;
	
	public NSGAIIPopulationFactory(){}
	
	public NSGAIIPopulationFactory(boolean b) {
		superElitist = b;
	}

	@Override
	public Population createPopulation(int popSize, ReactionNetwork startingNetwork){
		if(superElitist) System.out.println("INFO: super elitist mode");
		return new NSGAIIPopulation(popSize, startingNetwork, superElitist);
	}

}
