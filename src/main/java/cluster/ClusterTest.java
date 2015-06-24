package cluster;

public class ClusterTest {

	public static void main(String[] args) throws Exception {
		Cluster.start();
		while (Cluster.getMembers().size() < 1) {

		}
		Cluster.doSomething();
	}

}
