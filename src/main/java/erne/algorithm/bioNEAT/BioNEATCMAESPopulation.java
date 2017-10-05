package erne.algorithm.bioNEAT;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import erne.Individual;
import erne.algorithm.EvolutionaryAlgorithm;
import erne.algorithm.cmaes.CMAESBuilder;
import erne.algorithm.cmaes.CMAESPopulation;
import reactionnetwork.ReactionNetwork;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessDisplayer;

public class BioNEATCMAESPopulation extends BioNEATPopulation {
	
	public static int cmaesPopulation = 20;
	public static int cmaesRounds = 10;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5224550594871604647L;

	public BioNEATCMAESPopulation(int size, ReactionNetwork initNetwork) {
		super(size, initNetwork);
		
	}

	@Override
	protected void evaluateFitness() throws InterruptedException, ExecutionException {
		//Same as the original function
		Individual[] individuals = populations.get(populations.size() - 1);
		System.out.println("Evaluating fitness");
		//EvolutionaryAlgorithm algorithm = new CMAESBuilder().buildAlgorithm();
		
		//Now, for each individual, we use CMAES for optimization
		for (int i = 0; i < individuals.length; i++) {
			
				
				
				/* We don't need a full evolver, just a population will do
				Evolver evolver = new Evolver(cmaesPopulation, cmaesRounds, individuals[i].getNetwork(),
						getFitnessFunction(), null, algorithm);
				evolver.setGUI(false);
				evolver.setSaveEvo(false); //we are only using it inline
				evolver.evolve();
				*/
				CMAESPopulation pop = new CMAESPopulation(cmaesPopulation,individuals[i].getNetwork());
				pop.setFitnessFunction(getFitnessFunction());
				pop.resetPopulation();
				boolean allZero = testAllZero(pop.getPopulationInfo(0).getIndividuals());
				for(int j =1; j<cmaesRounds; j++){
					pop.evolve();
					allZero = allZero && testAllZero(pop.getPopulationInfo(j).getIndividuals());
					if(allZero){
						System.out.println("WARNING: BioNEATCMAESPopulation: CMAES optimization cancelled due to flat landscape");
						break;
					}
				}
				
				//Now, we get the best performing result, and give it to the individual.
				Individual best = Evolver.getBestEver(pop);
				individuals[i].setFitnessResult(best.getFitnessResult());
				individuals[i].setReactionNetwork(best.getNetwork());
				
		}
		
		
		
	}
	
	private boolean testAllZero(Individual[] indivs){
		boolean res = true;
		for(int i=0; i< indivs.length; i++){
			if(indivs[i].getFitnessResult().getFitness() >0.0){
				res = false;
				break;
			}
		}
		return res;
	}

}
