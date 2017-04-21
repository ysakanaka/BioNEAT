package cluster;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class AbstractTask<T1,T2> implements Callable<T2>, DataSerializable, HazelcastInstanceAware {
	
	protected transient HazelcastInstance hz;
	protected Object data;
	protected T1 origin;

	public AbstractTask(T1 origin){
		this.origin = origin;
	}
	
	public AbstractTask(){
		this.origin = null;
		this.data = null;
	}
	
	public T1 getOrigin(){
		return origin;
	}
	
	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hz = hazelcastInstance;
	}
	
	public HazelcastInstance getHazelCastInstance(){
		return hz;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(this.data);
		out.writeObject(this.origin);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		this.data = in.readObject();
		this.origin = in.readObject();
	}

	@Override
	public T2 call() throws Exception {
		System.err.println("Abstract task, does nothing");
		return null;
	}

}
