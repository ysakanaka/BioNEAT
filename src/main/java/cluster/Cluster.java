package cluster;

import java.util.ArrayList;
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
	
	private static boolean running = false;

	public static void start() {
		if(running){
			System.err.println("WARNING: cluster is already running");
			return;
		}
		initMembers();
		running = true;
	}
	
	public static void stop() {
		if(!running){
			System.err.println("WARNING: cluster is not running");
			return;
		}
		hz.getExecutorService("default").shutdown();
		hz.getExecutorService("default").destroy();
		running = false;
	}

	public static void bindProgressBar(JProgressBar progressBar) {
		Cluster.progressBar = progressBar;
	}

	public static String echoOnTheMember(String input, Member member) throws Exception {
		if(!running){
			System.err.println("Cluster not running. Start cluster first.");
			return null;
		}
		Callable<String> task = new ClusterSampleTask(input);
		IExecutorService executorService = hz.getExecutorService("default");
		Future<String> future = executorService.submitToMember(task, member);
		return future.get();
	}
	
	public static <T1,T2> Future<T2> evaluateOnTheMember(AbstractTask<T1,T2> task, Member member) throws InterruptedException, ExecutionException {
		if(!running){
			System.err.println("Cluster not running. Start cluster first.");
			return null;
		}
		
		IExecutorService executorService = hz.getExecutorService("default");

		Future<T2> future = executorService.submitToMember(task, member);
		return future;
		
	}

	public static Future<AbstractFitnessResult> evaluateOnTheMember(FitnessEvaluationData data, Member member) throws InterruptedException,
			ExecutionException {
		if(!running){
			System.err.println("Cluster not running. Start cluster first.");
			return null;
		}
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

	protected static Set<Member> initMembers(){
		Set<Member> members = cluster.getMembers();
		Iterator<Member> it = members.iterator();
		while (it.hasNext()) {
			Member m = it.next();
			m.setIntAttribute(nProcessorsAttribute, 2*Runtime.getRuntime().availableProcessors());
		}
		
		return members;
	}
	
	public static <T1,T2> Map<T1,T2> submitToCluster(List<AbstractTask<T1,T2>> tasks) throws InterruptedException, ExecutionException {
		int completedJobCount = 0;
		int totalJobCount = tasks.size();
		Map<T1, T2> results = new HashMap<T1, T2>();
		Map<Future<T2>, Member> futureToMember = new HashMap<Future<T2>, Member>();
		Map<Future<T2>, T1> futureToNetwork = new HashMap<Future<T2>, T1>();
		
		while (!tasks.isEmpty()) {
			//ReactionNetwork network = tempNetworks.get(0);
			AbstractTask<T1,T2> task = tasks.get(0);
			boolean submitted = false;
			Set<Member> members = getMembers();
			
			Iterator<Member> it = members.iterator();
			while (it.hasNext()) {
				Member m = it.next();
				Integer taskCountObject = activeTaskCounts.get(m);
				int taskCount = 0;
				if (taskCountObject != null) {
					taskCount = taskCountObject.intValue();
				}
				
				if (taskCount < m.getIntAttribute(nProcessorsAttribute)) {
					Future<T2> future = evaluateOnTheMember(task, m);
					taskCount++;
					// System.out.println("Submitted to <" + m +
					// ">. Task count: " + taskCount);
					futureToMember.put(future, m);
					futureToNetwork.put(future, task.getOrigin());
					activeTaskCounts.put(m, taskCount);
					submitted = true;
					break;
				}
			}
			if (!submitted) {
				loop1: while (true) {
					for (Future<T2> future : futureToMember.keySet()) {
						if (future.isDone()) {
							T2 fitnessResult = future.get();
							Member m = futureToMember.get(future);
							int taskCount = activeTaskCounts.get(m) - 1;
							completedJobCount++;
							if (progressBar != null)
								progressBar.setValue(completedJobCount * 100 / totalJobCount);
							System.out.println("<" + m + ">. DONE. Task count: " + taskCount + ". Fitness:" + fitnessResult);
							activeTaskCounts.put(m, taskCount);
							T1 n = futureToNetwork.get(future);
							results.put(n, fitnessResult);
							futureToMember.remove(future);
							break loop1;
						}
					}
				}
			} else {
				tasks.remove(0);
			}
		}
		while (futureToMember.size() > 0) {
			Iterator<Entry<Future<T2>, Member>> it = futureToMember.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Future<T2>, Member> entry = it.next();
				Future<T2> future = entry.getKey();
				if (future.isDone()) {
					T2 fitnessResult = future.get();
					Member m = entry.getValue();
					int taskCount = activeTaskCounts.get(m) - 1;
					completedJobCount++;
					if (progressBar != null)
						progressBar.setValue(completedJobCount * 100 / totalJobCount);
					System.out.println("<" + m + ">. DONE. Task count: " + taskCount + ". Fitness:" + fitnessResult);
					activeTaskCounts.put(m, taskCount);
					T1 n = futureToNetwork.get(future);
					results.put(n, fitnessResult);
					it.remove();
				}
			}
		}
		
		return results;
	}
	
	public static Map<ReactionNetwork, AbstractFitnessResult> evaluateFitness(AbstractFitnessFunction fitnessFunction,
			List<ReactionNetwork> networks) throws InterruptedException, ExecutionException {
		if(!running){
			System.err.println("Cluster not running. Start cluster first.");
			return null;
		}
		int totalJobCount = networks.size();
		ArrayList<ReactionNetwork> tempNetworks = new ArrayList<ReactionNetwork>(networks); // To keep track
		int completedJobCount = 0;
		if (progressBar != null)
			progressBar.setValue(0);
		Map<ReactionNetwork, AbstractFitnessResult> results = new HashMap<ReactionNetwork, AbstractFitnessResult>();
		Map<Future<AbstractFitnessResult>, Member> futureToMember = new HashMap<Future<AbstractFitnessResult>, Member>();
		Map<Future<AbstractFitnessResult>, ReactionNetwork> futureToNetwork = new HashMap<Future<AbstractFitnessResult>, ReactionNetwork>();
		while (!tempNetworks.isEmpty()) {
			ReactionNetwork network = tempNetworks.get(0);
			boolean submitted = false;
			Set<Member> members = getMembers();
			
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
				tempNetworks.remove(0);
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
