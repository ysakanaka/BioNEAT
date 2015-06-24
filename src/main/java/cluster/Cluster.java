package cluster;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import use.math.SquareFitnessFunction;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import demo.ProtectedSeq;
import erne.AbstractFitnessResult;

public class Cluster {
	static {
		// jdk: JDK logging (default)
		// log4j: Log4j
		// slf4j: Slf4j
		// none: disable logging
		System.setProperty("hazelcast.logging.type", "none");
	}

	private static final HazelcastInstance hz = Hazelcast.newHazelcastInstance();
	private static final com.hazelcast.core.Cluster cluster = hz.getCluster();

	public static void start() {

	}

	public static String echoOnTheMember(String input, Member member) throws Exception {
		Callable<String> task = new ClusterSampleTask(input);
		IExecutorService executorService = hz.getExecutorService("default");

		Future<String> future = executorService.submitToMember(task, member);
		return future.get();
	}

	public static AbstractFitnessResult evaluateOnTheMember(FitnessEvaluationData data, Member member) throws InterruptedException,
			ExecutionException {
		Callable<AbstractFitnessResult> task = new FitnessEvaluationTask(data);
		IExecutorService executorService = hz.getExecutorService("default");

		Future<AbstractFitnessResult> future = executorService.submitToMember(task, member);
		return future.get();
	}

	public static void main(String[] args) throws Exception {
		Cluster.start();
	}

	public static Set<Member> getMembers() {
		return cluster.getMembers();
	}

	public static void doSomething() throws Exception {
		Set<Member> members = cluster.getMembers();
		Iterator<Member> it = members.iterator();
		while (it.hasNext()) {
			Member m = it.next();
			System.out.println(evaluateOnTheMember(new FitnessEvaluationData(new SquareFitnessFunction(), ProtectedSeq.getInitNetwork()), m));
		}
	}
}
