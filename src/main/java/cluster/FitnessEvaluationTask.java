package cluster;

import java.io.IOException;
import java.util.concurrent.Callable;

import use.math.FitnessResult;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import erne.AbstractFitnessResult;

public class FitnessEvaluationTask implements Callable<AbstractFitnessResult>, DataSerializable, HazelcastInstanceAware {

	private FitnessEvaluationData data;
	private transient HazelcastInstance hz;

	FitnessEvaluationTask(FitnessEvaluationData data) {
		this.data = data;
	}

	public FitnessEvaluationTask() {

	}

	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hz = hazelcastInstance;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(this.data);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		this.data = in.readObject();
	}

	@Override
	public AbstractFitnessResult call() throws Exception {
		return data.fitnessFunction.evaluate(data.network);
	}

}
