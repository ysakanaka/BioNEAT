package cluster;

import java.io.IOException;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class ClusterSampleTask extends AbstractTask<String,String> {

	
	private transient HazelcastInstance hz;

	public ClusterSampleTask(String input) {
		super(input);
		this.data = input;
	}

	public ClusterSampleTask() {
		super("");
		this.data = "";
	}

	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hz = hazelcastInstance;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(origin);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		origin = in.readUTF();
		data = origin;
	}

	@Override
	public String call() throws Exception {
		System.out.println(origin);
		Thread.sleep(5000);
		return hz.toString();
	}

}
