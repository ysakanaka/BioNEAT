package erne.algorithm.nsgaII;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import erne.PopulationDisplayer;
import erne.PopulationFactory;
import erne.algorithm.EvolutionaryAlgorithm;
import erne.algorithm.EvolutionaryAlgorithmBuilder;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.rules.AddActivation;
import erne.mutation.rules.AddInhibition;
import erne.mutation.rules.AddNode;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import erne.mutation.rules.TogglePseudoTemplate;
import erne.visual.NSGAIIPopulationDisplayer;

public class NSGAIIBuilder implements EvolutionaryAlgorithmBuilder, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6806163834950847762L;
	
	public static final String DEFAULT_NAME = "nsgaII";
	
	public static final Mutator DEFAULT_MUTATOR = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {
			new DisableTemplate(1), new MutateParameter(90), new AddNode(2), new AddActivation(2), new AddInhibition(5) })));

	public static final PopulationFactory DEFAULT_POPULATION_FACTORY = new NSGAIIPopulationFactory();
	
	public static final PopulationDisplayer DEFAULT_POPULATION_DISPLAYER = new NSGAIIPopulationDisplayer();
	
	protected String name = DEFAULT_NAME;
	protected Mutator mutator = DEFAULT_MUTATOR;
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
