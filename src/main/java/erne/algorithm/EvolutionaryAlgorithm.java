package erne.algorithm;

import erne.PopulationDisplayer;
import erne.PopulationFactory;
import erne.mutation.Mutator;

public class EvolutionaryAlgorithm {
	
	protected String name;
	protected Mutator mutator;
	protected PopulationFactory populationFactory;
	protected PopulationDisplayer populationDisplayer;
	
	public EvolutionaryAlgorithm(String name, Mutator mutator, PopulationFactory populationFactory, PopulationDisplayer populationDisplayer){
		this.name = name;
		this.mutator = mutator;
		this.populationFactory = populationFactory;
		this.populationDisplayer = populationDisplayer;
	}

	public String getName(){
		return name;
	}
	public Mutator getMutator(){
		return mutator;
	}
	public PopulationFactory getPopulationFactory(){
		return populationFactory;
	}
	public PopulationDisplayer getPopulationDisplayer(){
		return populationDisplayer;
	}
	
	
}
