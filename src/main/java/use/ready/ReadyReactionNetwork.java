package use.ready;

import reactionnetwork.ReactionNetwork;
import use.ready.fakefitness.RunReady;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.Node;

public class ReadyReactionNetwork extends ReactionNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3523985909301277110L;
	
	public static Gson gson = new GsonBuilder().setPrettyPrinting()
			.registerTypeAdapter(ReadyReactionNetwork.class, new ReadyReactionNetworkDeserializer())
			.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
	
	public ArrayList<Set<Connection>> templateOnBeads = new ArrayList<Set<Connection>>();
	protected static Random rand = new Random();

	
	public ReadyReactionNetwork(){
		super();
		templateOnBeads.add(new HashSet<Connection>());
	}
	
	public void addTemplateOnBead(Connection temp, int index){
		if (index >= templateOnBeads.size() || !connections.contains(temp) || !temp.enabled) return;
		templateOnBeads.get(index).add(temp);
	}
	
	@Override
	public Connection addConnection(int innovation, Node from, Node to) {
		Connection conn = super.addConnection(innovation, from, to);
		addTemplateOnBead(conn,rand.nextInt(templateOnBeads.size()));
		return conn;
	}
	
	@Override
	public Connection addConnection(int innovation, String from, String to) {
		Connection conn = super.addConnection(innovation, from, to);
		addTemplateOnBead(conn,rand.nextInt(templateOnBeads.size()));
		return conn;
	}
	
	@Override
	public ReactionNetwork clone() {
		String json = gson.toJson(this);
		ReactionNetwork clone = gson.fromJson(json, ReadyReactionNetwork.class);
		return clone;
	}
	
	public static void main(String[] args){
		
		//Making some tests.
		ReadyReactionNetwork rrn = new ReadyReactionNetwork();
		Node a = new Node("a");
		rrn.addNode(a);
		rrn.addConnection(0, a, a);
		System.out.println(rrn);
		
		System.out.println(rrn.clone());
		
		//Some other basic test
		System.out.println(RunReady.startingReady);
		System.out.println(RunReady.startingReady.templateOnBeads);
	}
}
