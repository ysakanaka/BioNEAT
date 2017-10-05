package erne.algorithm.cmaes;

import java.io.Serializable;

import erne.Population;
import erne.PopulationDisplayer;
import erne.PopulationFactory;
import erne.algorithm.EvolutionaryAlgorithm;
import erne.algorithm.EvolutionaryAlgorithmBuilder;
import erne.mutation.Mutator;
import erne.visual.CMAESPopulationDisplayer;
import reactionnetwork.ReactionNetwork;

public class CMAESBuilder implements EvolutionaryAlgorithmBuilder, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7038238173007300673L;
	
	public static final String DEFAULT_NAME = "CMAES";
	public static final PopulationDisplayer DEFAULT_POPULATION_DISPLAYER = new CMAESPopulationDisplayer();
	public static final PopulationFactory DEFAULT_POPULATION_FACTORY = new PopulationFactory(){

		@Override
		public Population createPopulation(int popSize, ReactionNetwork startingNetwork) {
			
			return new CMAESPopulation(popSize, startingNetwork);
		}
		
	};
	
	protected String name = DEFAULT_NAME;
	protected Mutator mutator = null;
	protected PopulationDisplayer populationDisplayer = DEFAULT_POPULATION_DISPLAYER;
	protected PopulationFactory populationFactory = DEFAULT_POPULATION_FACTORY;

	@Override
	public EvolutionaryAlgorithmBuilder name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public EvolutionaryAlgorithmBuilder mutator(Mutator mutator) {
		this.mutator = mutator;
		System.out.println("WARNING: "+name+" relies on a builtin mutator. Provided mutator will be ignored.");
		return this;
	}

	@Override
	public EvolutionaryAlgorithmBuilder populationFactory(PopulationFactory populationFactory) {
		this.populationFactory = populationFactory;
		return this;
	}

	@Override
	public EvolutionaryAlgorithmBuilder populationDisplayer(PopulationDisplayer populationDisplayer) {
		this.populationDisplayer = populationDisplayer;
		return this;
	}

	@Override
	public EvolutionaryAlgorithm buildAlgorithm() {
		return new EvolutionaryAlgorithm(name,mutator,populationFactory,populationDisplayer);
	}

}
