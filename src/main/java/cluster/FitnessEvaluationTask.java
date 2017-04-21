package cluster;

import erne.AbstractFitnessResult;
import reactionnetwork.ReactionNetwork;

public class FitnessEvaluationTask extends AbstractTask<ReactionNetwork,AbstractFitnessResult> {

	//private FitnessEvaluationData data;
	

	FitnessEvaluationTask(FitnessEvaluationData data) {
		super(data.network);
		this.data = data;
	}

	@Override
	public AbstractFitnessResult call() throws Exception {
		FitnessEvaluationData fdata = (FitnessEvaluationData) data;
		return fdata.fitnessFunction.evaluate(fdata.network);
	}

}
