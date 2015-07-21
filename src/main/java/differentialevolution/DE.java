package differentialevolution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import cluster.Cluster;
import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;
import erne.Individual;
import reactionnetwork.Connection;
import reactionnetwork.Library;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import use.math.gaussian.GaussianFitnessFunction;

public class DE {

	ArrayList<Double> minRange;
	ArrayList<Double> maxRange;

	protected Random rnd;
	protected int PopSize = 50;
	protected int maxGeneration = 1000;
	protected int GenCount;
	ReactionNetwork templateNetwork;
	AbstractFitnessFunction fitnessFunction;

	DEIndiv CurGen[];
	DEIndiv NextGen[];

	public DE(ReactionNetwork templateSystem, AbstractFitnessFunction fitnessFunction) {
		this.templateNetwork = templateSystem;
		this.fitnessFunction = fitnessFunction;
		this.rnd = new Random(System.currentTimeMillis());
		this.GenCount = 0;
	}

	/*******
	 * Method to convert the flattened parameter array (double[] x) into
	 * OligoSystemGeneral class
	 * 
	 * @param x
	 * @return
	 */

	private ReactionNetwork getReactionNetwork(double[] x) {
		ReactionNetwork network = this.templateNetwork.clone();
		int count = 0;
		for (int i = 0; i < network.nodes.size(); i++) {
			Node node = network.nodes.get(i);
			if (node.type == Node.SIMPLE_SEQUENCE) {
				node.parameter = x[count];
				count++;
			}
		}
		for (int i = 0; i < network.connections.size(); i++) {
			Connection conn = network.connections.get(i);
			conn.parameter = x[count];
			count++;
		}
		return network;
	}

	/*******
	 * Method to convert the OligoSystemGeneral into flattened double vector of
	 * parameters to be optimized by DE
	 * 
	 * @param x
	 * @return
	 */

	private double[] getParameterArray(ReactionNetwork network) {

		double params[] = new double[network.getNSimpleSequences() + network.connections.size()];

		int count = 0;
		for (int i = 0; i < network.nodes.size(); i++) {
			Node node = network.nodes.get(i);
			if (node.type == Node.SIMPLE_SEQUENCE) {
				params[count] = node.parameter;
				count++;
			}
		}
		for (int i = 0; i < network.connections.size(); i++) {
			Connection conn = network.connections.get(i);
			params[count] = conn.parameter;
			count++;
		}
		return (params);
	}

	/**********************
	 * Method to generate the initial DE population and convert them into an
	 * array of OligSystemGeneral models and store the DE inidiviuals parameters
	 * e.g. crossRate / ampFactors
	 */

	public ReactionNetwork[] createInitGeneration(ReactionNetwork indiv) {

		ReactionNetwork[] initGen;

		this.minRange = new ArrayList<Double>();
		this.maxRange = new ArrayList<Double>();

		for (int i = 0; i < indiv.getNSimpleSequences(); i++) {
			this.minRange.add(new Double(Individual.minNodeValue));
			this.maxRange.add(new Double(Individual.maxNodeValue));
		}

		for (int i = 0; i < indiv.connections.size(); i++) {
			this.minRange.add(new Double(Individual.minTemplateValue));
			this.maxRange.add(new Double(Individual.maxTemplateValue));
		}

		this.CurGen = new DEIndiv[this.PopSize];
		this.NextGen = new DEIndiv[this.PopSize];

		initGen = new ReactionNetwork[this.PopSize];

		// first randomly place the template individual in the population
		int index = (int) (this.rnd.nextDouble() * this.PopSize);

		double[] param = getParameterArray(indiv);
		this.CurGen[index] = new DEIndiv(param, this.rnd);
		initGen[index] = getReactionNetwork(this.CurGen[index].getParameters());
		this.CurGen[index].setReactionNetwork(initGen[index]);
		index = (index + 1) % this.PopSize;

		// now create other initial individuals randomly
		for (int i = 0; i < this.PopSize; ++i) {
			this.CurGen[index] = new DEIndiv(param.length, this.minRange, this.maxRange, this.rnd);
			initGen[index] = getReactionNetwork(this.CurGen[index].getParameters());
			this.CurGen[index].setReactionNetwork(initGen[index]);
			index = (index + 1) % this.PopSize;
		}

		return initGen;
	}

	// @OVERRIDE the parent method because of DE's one to one generation
	// alternation mechanism
	public ReactionNetwork optimize() throws InterruptedException, ExecutionException {
		ReactionNetwork[] curGen = createInitGeneration(templateNetwork);
		ReactionNetwork[] nextGen;
		double[] curFitness = new double[curGen.length];
		double[] nextFitness;
		System.out.println("Evaluating fitness");
		List<ReactionNetwork> networks = new LinkedList<ReactionNetwork>();
		for (int i = 0; i < curGen.length; i++) {
			networks.add(curGen[i]);
		}
		Map<ReactionNetwork, AbstractFitnessResult> fitnesses = Cluster.evaluateFitness(fitnessFunction, networks);
		for (int i = 0; i < curGen.length; i++) {
			curFitness[i] = fitnesses.get(curGen[i]).getFitness();
			this.CurGen[i].setFitnessScore(curFitness[i]);
		}

		int bestIndivIndex = 0;
		while (this.GenCount < maxGeneration) {
			nextGen = getNextGeneration(curGen, curFitness);
			nextFitness = new double[nextGen.length];
			System.out.println("Evaluating fitness");
			networks = new LinkedList<ReactionNetwork>();
			for (int i = 0; i < nextGen.length; i++) {
				networks.add(nextGen[i]);
			}
			fitnesses = Cluster.evaluateFitness(fitnessFunction, networks);
			for (int i = 0; i < nextGen.length; i++) {
				nextFitness[i] = fitnesses.get(nextGen[i]).getFitness();
				this.NextGen[i].setFitnessScore(curFitness[i]);
			}

			for (int i = 0; i < curGen.length; i++) {
				if (nextFitness[i] >= curFitness[i]) {
					curGen[i] = nextGen[i];
					this.CurGen[i] = this.NextGen[i];
					curFitness[i] = nextFitness[i];
				}
			}

			bestIndivIndex = 0;
			double avgFitness = 0;
			for (int i = 0; i < curGen.length; i++) {
				if (curFitness[i] > curFitness[bestIndivIndex]) {
					bestIndivIndex = i;
				}
				avgFitness += curFitness[i];
			}
			avgFitness /= curGen.length;

			// debug
			// printCurGen();

			System.out.println("Gen " + this.GenCount + " Best fitness index: " + bestIndivIndex + " Best fitness score: "
					+ curFitness[bestIndivIndex] + " Avg fitness score: " + avgFitness);
			System.out.println(this.CurGen[bestIndivIndex].getReactionNetwork());
			if (stopEvolution()) {
				break;
			}
			this.GenCount++;
		}
		return this.CurGen[bestIndivIndex].getReactionNetwork();
	}

	public ReactionNetwork[] getNextGeneration(ReactionNetwork[] curGen, double[] results) {
		// TODO Auto-generated method stub
		ReactionNetwork[] oligoSystemGen = new ReactionNetwork[this.PopSize];

		int a, b, c;

		for (int i = 0; i < this.PopSize; ++i) {
			do {
				a = (int) (this.rnd.nextDouble() * this.PopSize);
			} while (a == i);
			do {
				b = (int) (this.rnd.nextDouble() * this.PopSize);
			} while (b == i || b == a);
			do {
				c = (int) (this.rnd.nextDouble() * this.PopSize);
			} while (c == i || c == a || c == b);

			this.NextGen[i] = this.CurGen[i].breed(this.CurGen[a], this.CurGen[b], this.CurGen[c], this.rnd, this.minRange, this.maxRange);
		}

		for (int i = 0; i < this.PopSize; ++i) {
			oligoSystemGen[i] = getReactionNetwork(this.NextGen[i].getParameters());
			this.NextGen[i].setReactionNetwork(oligoSystemGen[i]);
		}

		return oligoSystemGen;
	}

	protected boolean stopEvolution() {
		return false;
	}

}
