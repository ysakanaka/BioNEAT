package erne;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import cluster.Cluster;
import erne.mutation.Mutator;
import erne.speciation.Species;
import reactionnetwork.Connection;
import reactionnetwork.ReactionNetwork;

public abstract class Population implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static AtomicInteger nextIndivId = new AtomicInteger();
	public static AtomicInteger nextNodeName = new AtomicInteger((int) 'b'); //TODO: refactor that approach
	//public static Map<String, Integer> innovationNumbers = new HashMap<String, Integer>(); //TODO bioneat
	public static Map<String, String> nodeNameOrigins = new HashMap<String, String>();

	private Individual initIndividual;
	protected ArrayList<Individual[]> populations = new ArrayList<Individual[]>();
	private AbstractFitnessFunction fitnessFunction;
	

	protected Random rand = new Random();

	protected Mutator mutator;

	public Population(int size, ReactionNetwork initNetwork) {
		this.populations.add(new Individual[size]);
		this.initIndividual = new Individual(initNetwork);
	}

	public int getTotalGeneration() {
		return populations.size();
	}

	/*public ArrayList<Species[]> getSpeciesByGenerations() {
		return speciationSolver.speciesByGeneration;
	}*/

	public PopulationInfo getPopulationInfo(int i) {
		Species[] species = new Species[1];
		species[0] = new Species(populations.get(i)[0]);
		species[0].individuals = new ArrayList<Individual>(Arrays.asList(populations.get(i)));
		return new PopulationInfo(populations.get(i), species);
	}

	public void setFitnessFunction(AbstractFitnessFunction fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}

	public void setMutator(Mutator mutator) {
		this.mutator = mutator;
	}

	public Individual[] resetPopulation() throws InterruptedException, ExecutionException {
		Individual[] individuals = populations.get(populations.size() - 1);
		for (int i = 0; i < individuals.length; i++) {
			individuals[i] = initIndividual.clone();
			mutator.mutate(individuals[i]);
			individuals[i].parentIds.add(initIndividual.getId());
		}
		evaluateFitness();
		
		return individuals;
	}

	public Individual[] evolve() throws InterruptedException, ExecutionException {
		reproduction();
		evaluateFitness();
		Individual[] individuals = populations.get(populations.size() - 1);
		//speciationSolver.speciate(individuals);
		return individuals;
	}

	
	public abstract Individual[] reproduction();

	protected Individual[] selectCrossoverParents(ArrayList<Individual> curGen) {
		double maxFitness = -Double.MAX_VALUE;
		Individual parent1 = null;
		Individual parent2 = null;
		if (curGen.size() == 1) {
			return new Individual[] { curGen.get(0), curGen.get(0) };
		}

		for (int k = 0; k < Constants.tournamentSize; k++) {
			int winnerIndiv = this.rand.nextInt(curGen.size());
			Individual i = curGen.get(winnerIndiv);
			if (i.getFitnessResult().getFitness() > maxFitness) {
				parent1 = i;
				maxFitness = i.getFitnessResult().getFitness();
			}
		}
		maxFitness = -Double.MAX_VALUE;

		for (int k = 0; k < Constants.tournamentSize; k++) {
			Individual i = null;
			do {
				int winnerIndiv = this.rand.nextInt(curGen.size());
				i = curGen.get(winnerIndiv);
			} while (i == parent1);

			if (i.getFitnessResult().getFitness() > maxFitness) {
				parent2 = i;
				maxFitness = i.getFitnessResult().getFitness();
			}
		}

		return new Individual[] { parent1, parent2 };
	}

	private void evaluateFitness() throws InterruptedException, ExecutionException {
		Individual[] individuals = populations.get(populations.size() - 1);
		System.out.println("Evaluating fitness");
		List<ReactionNetwork> networks = new LinkedList<ReactionNetwork>();
		for (int i = 0; i < individuals.length; i++) {
			networks.add(individuals[i].getNetwork());
		}
		Map<ReactionNetwork, AbstractFitnessResult> fitnesses = Cluster.evaluateFitness(fitnessFunction, networks);
		for (int i = 0; i < individuals.length; i++) {
			individuals[i].setFitnessResult(fitnesses.get(individuals[i].getNetwork()));
			// System.out.println("Indiv " + i + " Fitness: " +
			// individuals[i].getFitnessResult());
		}
		//Now, check if we are using a lexicographic fitness function
		if(AbstractLexicographicFitnessResult.class.isAssignableFrom(individuals[0].getFitnessResult().getClass())){
			//we have to sort them
			Arrays.sort(individuals,individualComparator);
			for(int i=0;i<individuals.length;i++){
				((AbstractLexicographicFitnessResult) individuals[i].getFitnessResult()).setRank(individuals.length-i);
			}
		}
	}
	
	private static Comparator<Individual> individualComparator = new Comparator<Individual>(){
		@Override
		public int compare(Individual o1, Individual o2) {
			if(AbstractLexicographicFitnessResult.class.isAssignableFrom(o1.getFitnessResult().getClass())
					&& AbstractLexicographicFitnessResult.class.isAssignableFrom(o2.getFitnessResult().getClass())){
			return AbstractLexicographicFitnessResult.defaultComparator.compare((AbstractLexicographicFitnessResult)o1.getFitnessResult(),
					(AbstractLexicographicFitnessResult) o2.getFitnessResult());
			}
			return 0; // not comparable
		}
	};
	
	
	public abstract void checkRestart();
	
	public String toString(){
		return populations.size()+" gens, last gen: "+Arrays.toString(populations.get(populations.size()-1));
	}
}
