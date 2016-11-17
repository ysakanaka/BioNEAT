package reactionnetwork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.Static;

public class ReactionNetwork implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<Node> nodes;
	public ArrayList<Connection> connections;
	public Map<String, Double> parameters;

	public ReactionNetwork() {
		nodes = new ArrayList<Node>();
		connections = new ArrayList<Connection>();
		parameters = new HashMap<String, Double>();
	}

	public Node getNodeByName(String name) {
		for (Node node : nodes) {
			if (node.name.equals(name)) {
				return node;
			}
		}
		return null;
	}

	public Node addNode(Node node) {
		nodes.add(node);
		return node;
	}

	public Connection addConnection(int innovation, Node from, Node to) {
		if (nodes.contains(from) && nodes.contains(to)) {
			Connection connection = new Connection(from, to);
			connection.innovation = innovation;
			connections.add(connection);
			return connection;
		} else {
			return null;
		}
	}

	public Connection addConnection(int innovation, String fromName, String toName) {
		Node from = null;
		Node to = null;
		for (Node node : nodes) {
			if (node.name.equals(fromName)) {
				from = node;
			}
			if (node.name.equals(toName)) {
				to = node;
			}
		}
		if (from != null && to != null) {
			Connection connection = new Connection(from, to);
			connection.innovation = innovation;
			connections.add(connection);
			return connection;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return Static.gson.toJson(this);
	}

	public ReactionNetwork clone() {
		Gson gson = new GsonBuilder().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
				.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
		String json = gson.toJson(this);
		ReactionNetwork clone = gson.fromJson(json, ReactionNetwork.class);
		return clone;
	}

	public int getNSimpleSequences() {
		int ret = 0;
		for (Node node : nodes) {
			if (node.type == Node.SIMPLE_SEQUENCE)
				ret++;
		}
		return ret;
	}

	public int getNEnabledConnections() {
		int ret = 0;
		for (Connection connection : connections) {
			if (connection.enabled)
				ret++;
		}
		return ret;
	}

	public Connection getConnectionByIN(int innovationNumber) {
		for (Connection connection : connections) {
			if (connection.innovation == innovationNumber)
				return connection;
		}
		return null;
	}

	public Connection getConnectionByEnds(Node from, Node to) {
		if(from == null || to == null) return null;
		for (Connection connection : connections) {
			if (connection.from.name.equals(from.name) && connection.to.name.equals(to.name)) {
				return connection;
			}
		}
		return null;
	}
	
	@Override
	public boolean equals(Object o){
		if(o.getClass()!= this.getClass()){
			return false;
		}
		ReactionNetwork rn = (ReactionNetwork) o;
		
		ArrayList<Connection> fake = new ArrayList<Connection>(connections);
		
		for(Connection c : rn.connections){
			if(!fake.contains(c)){
				return false;
			}
			fake.remove(c);
		}
		
		ArrayList<Node> fake2 = new ArrayList<Node>(nodes);
		
		for(Node n : rn.nodes){
			if(!fake2.contains(n)){
				return false;
			}
			fake2.remove(n);
		}
		
		return fake.isEmpty()&&fake2.isEmpty();
	}
	
	@Override
	public int hashCode(){
		int result = 349;
		int prime = 37;
		for(Connection c : connections){
			result = prime*result + c.hashCode();
		}
		for(Node n : nodes){
			result = prime*result + n.hashCode();
		}
		return result;
	}
	
	public static void main(String[] args){
		ReactionNetwork rn = Library.oldGaussian.clone();
		System.out.println(rn.hashCode()+" "+Library.oldGaussian.hashCode()+" "+rn.equals(Library.oldGaussian));
		System.out.println(rn);
	}
}
