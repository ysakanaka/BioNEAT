package erne;

import reactionnetwork.ReactionNetwork;

public class DummyFitnessFunction extends AbstractFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public AbstractFitnessResult evaluate(ReactionNetwork network) {
		return new SimpleFitnessResult(1.0);
	}

	@Override
	public AbstractFitnessResult minFitness() {
		return new SimpleFitnessResult(0.0);
	}

}
