package reactionnetwork;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Test {
	public static void main(String[] args) {

		ReactionNetwork network = new ReactionNetwork();
		network.nodes = new ArrayList<Node>();
		Node node1 = new Node("a");
		Node node2 = new Node("b");
		network.nodes.add(node1);
		network.nodes.add(node2);
		Connection conn = new Connection(node1, node2);
		network.connections = new ArrayList<Connection>();
		network.connections.add(conn);
		network.parameters.put("exo", 10.0);
		network.parameters.put("pol", 10.0);
		network.parameters.put("nick", 10.0);
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(ReactionNetwork.class,
						new ReactionNetworkDeserializer())
				.registerTypeAdapter(Connection.class,
						new ConnectionSerializer()).create();
		@SuppressWarnings("unused")
		String json = gson.toJson(network);

		@SuppressWarnings("unused")
		ReactionNetwork newNetwork = gson
				.fromJson(
						"{\"nodes\":[{\"name\":\"a\",\"parameter\":0.0,\"initialConcentration\":0.0,\"type\":0},{\"name\":\"b\",\"parameter\":0.0,\"initialConcentration\":0.0,\"type\":0}],\"connections\":[{\"innovation\":0,\"enabled\":true,\"parameter\":0.0,\"from\":\"a\",\"to\":\"b\"}],\"parameters\":{\"nick\":10.0,\"pol\":10.0,\"exo\":10.0}}\n",
						ReactionNetwork.class);
	}
}
