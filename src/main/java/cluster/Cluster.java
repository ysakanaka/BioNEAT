package cluster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JProgressBar;

import reactionnetwork.ReactionNetwork;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import erne.AbstractFitnessFunction;
import erne.AbstractFitnessResult;

public class Cluster {

	static {
		// jdk: JDK logging (default)
		// log4j: Log4j
		// slf4j: Slf4j
		// none: disable logging
		System.setProperty("hazelcast.logging.type", "none");
	}

	private static final String nProcessorsAttribute = "nProcessor";

	private static HazelcastInstance hz = Hazelcast.newHazelcastInstance();

	private static final com.hazelcast.core.Cluster cluster = hz.getCluster();

	private static final Map<Member, Integer> activeTaskCounts = new HashMap<Member, Integer>();

	private static JProgressBar progressBar;

	public static void start() {
	}

	public static void bindProgressBar(JProgressBar progressBar) {
		Cluster.progressBar = progressBar;
	}

	public static String echoOnTheMember(String input, Member member) throws Exception {
		Callable<String> task = new ClusterSampleTask(input);
		IExecutorService executorService = hz.getExecutorService("default");
		Future<String> future = executorService.submitToMember(task, member);
		return future.get();
	}

	public static Future<AbstractFitnessResult> evaluateOnTheMember(FitnessEvaluationData data, Member member) throws InterruptedException,
			ExecutionException {
		Callable<AbstractFitnessResult> task = new FitnessEvaluationTask(data);
		IExecutorService executorService = hz.getExecutorService("default");

		Future<AbstractFitnessResult> future = executorService.submitToMember(task, member);
		return future;
	}

	public static void main(String[] args) throws Exception {
		Cluster.start();
	}

	public static Set<Member> getMembers() {
		return cluster.getMembers();
	}

	public static Map<ReactionNetwork, AbstractFitnessResult> evaluateFitness(AbstractFitnessFunction fitnessFunction,
			List<ReactionNetwork> networks) throws InterruptedException, ExecutionException {
		int totalJobCount = networks.size();
		int completedJobCount = 0;
		if (progressBar != null)
			progressBar.setValue(0);
		Map<ReactionNetwork, AbstractFitnessResult> results = new HashMap<ReactionNetwork, AbstractFitnessResult>();
		Map<Future<AbstractFitnessResult>, Member> futureToMember = new HashMap<Future<AbstractFitnessResult>, Member>();
		Map<Future<AbstractFitnessResult>, ReactionNetwork> futureToNetwork = new HashMap<Future<AbstractFitnessResult>, ReactionNetwork>();
		while (!networks.isEmpty()) {
			ReactionNetwork network = networks.get(0);
			boolean submitted = false;
			Set<Member> members = cluster.getMembers();
			Iterator<Member> it = members.iterator();
			while (it.hasNext()) {
				Member m = it.next();
				Integer taskCountObject = activeTaskCounts.get(m);
				int taskCount = 0;
				if (taskCountObject != null) {
					taskCount = taskCountObject.intValue();
				}
				if (taskCount < m.getIntAttribute(nProcessorsAttribute)) {
					Future<AbstractFitnessResult> future = evaluateOnTheMember(new FitnessEvaluationData(fitnessFunction, network), m);
					taskCount++;
					// System.out.println("Submitted to <" + m +
					// ">. Task count: " + taskCount);
					futureToMember.put(future, m);
					futureToNetwork.put(future, network);
					activeTaskCounts.put(m, taskCount);
					submitted = true;
					break;
				}
			}
			if (!submitted) {
				loop1: while (true) {
					for (Future<AbstractFitnessResult> future : futureToMember.keySet()) {
						if (future.isDone()) {
							AbstractFitnessResult fitnessResult = future.get();
							Member m = futureToMember.get(future);
							int taskCount = activeTaskCounts.get(m) - 1;
							completedJobCount++;
							if (progressBar != null)
								progressBar.setValue(completedJobCount * 100 / totalJobCount);
							System.out.println("<" + m + ">. DONE. Task count: " + taskCount + ". Fitness:" + fitnessResult);
							activeTaskCounts.put(m, taskCount);
							ReactionNetwork n = futureToNetwork.get(future);
							results.put(n, fitnessResult);
							futureToMember.remove(future);
							break loop1;
						}
					}
				}
			} else {
				networks.remove(0);
			}
		}
		while (futureToMember.size() > 0) {
			Iterator<Entry<Future<AbstractFitnessResult>, Member>> it = futureToMember.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Future<AbstractFitnessResult>, Member> entry = it.next();
				Future<AbstractFitnessResult> future = entry.getKey();
				if (future.isDone()) {
					AbstractFitnessResult fitnessResult = future.get();
					Member m = entry.getValue();
					int taskCount = activeTaskCounts.get(m) - 1;
					completedJobCount++;
					if (progressBar != null)
						progressBar.setValue(completedJobCount * 100 / totalJobCount);
					System.out.println("<" + m + ">. DONE. Task count: " + taskCount + ". Fitness:" + fitnessResult);
					activeTaskCounts.put(m, taskCount);
					ReactionNetwork n = futureToNetwork.get(future);
					results.put(n, fitnessResult);
					it.remove();
				}
			}
		}
		return results;

	}
}
