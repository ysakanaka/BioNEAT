package use.ready.fakefitness;

import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;
import erne.SimpleFitnessResult;
import reactionnetwork.ReactionNetwork;
import use.ready.AbstractReadyFitnessFunction;
import use.ready.ReadyReactionNetwork;

public class FakeReadyFitnessBeads extends AbstractFitnessFunction {

	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public AbstractFitnessResult evaluate(ReactionNetwork network) {
		if (ReadyReactionNetwork.class.isAssignableFrom(network.getClass())){
			return new SimpleFitnessResult(((ReadyReactionNetwork) network).templateOnBeads.size());
		}
		System.err.println("ERROR: incompatible reaction network class");
		return minFitness();
	}

	@Override
	public AbstractFitnessResult minFitness() {
		
		return new SimpleFitnessResult(0.0);
	}

}
