package erne.algorithm;

import erne.PopulationDisplayer;
import erne.PopulationFactory;
import erne.mutation.Mutator;

public interface EvolutionaryAlgorithmBuilder {

	public EvolutionaryAlgorithmBuilder name(String name);
	public EvolutionaryAlgorithmBuilder mutator(Mutator mutator);
	public EvolutionaryAlgorithmBuilder populationFactory(PopulationFactory populationFactory);
	public EvolutionaryAlgorithmBuilder populationDisplayer(PopulationDisplayer populationDisplayer);
	public EvolutionaryAlgorithm buildAlgorithm();
	
}
