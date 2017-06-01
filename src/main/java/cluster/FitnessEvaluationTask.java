package cluster;

import erne.AbstractFitnessResult;
import reactionnetwork.ReactionNetwork;

public class FitnessEvaluationTask extends AbstractTask<ReactionNetwork,AbstractFitnessResult> {
	

	public FitnessEvaluationTask(FitnessEvaluationData data) {
		//super(data.network);
		this.origin = data.network;
		this.data = data;
	}
	
	public FitnessEvaluationTask() {
		super();
	}
	

	@Override
	public AbstractFitnessResult call() throws Exception {
		FitnessEvaluationData fdata = (FitnessEvaluationData) data;
		return fdata.fitnessFunction.evaluate(fdata.network);
	}

}
