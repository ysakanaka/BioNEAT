package cluster;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class ClusterSampleTask implements Callable<String>, DataSerializable, HazelcastInstanceAware {

	private String input;
	private transient HazelcastInstance hz;

	public ClusterSampleTask(String input) {
		this.input = input;
	}

	public ClusterSampleTask() {
		this.input = "";
	}

	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hz = hazelcastInstance;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(input);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		input = in.readUTF();
	}

	@Override
	public String call() throws Exception {
		System.out.println(input);
		Thread.sleep(5000);
		return hz.toString();
	}

}
