package use.processing.cmaes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;

public class OptimizedNetworkWriter {
	
	protected ReactionNetwork reac;
	protected ArrayList<Node> orderedNodes; //so that we keep the same order when trying to interpret the genome
	protected ArrayList<Connection> orderedConnections ;
	
	public OptimizedNetworkWriter(ReactionNetwork reac){
		init(reac, null, null);
	}
	
	public OptimizedNetworkWriter(ReactionNetwork reac,  ArrayList<Node> nodes, ArrayList<Connection> connections){
		init(reac, nodes, connections);
	}
	
	protected void init(ReactionNetwork reac, ArrayList<Node> nodes, ArrayList<Connection> connections){
		this.reac = reac;
		
		if(nodes == null){
			orderedNodes = new ArrayList<Node>();
			for(int i= 0; i< reac.nodes.size(); i++){//maybe more stable than the enumeration in RDCMAES...
				Node n = reac.nodes.get(i);
				if(n.type == Node.SIMPLE_SEQUENCE){ 
					orderedNodes.add(n);
				}
			}
		}
		
		if(connections == null){
			orderedConnections = new ArrayList<Connection>();
			for(int i = 0; i<reac.connections.size();i++){
				Connection c = reac.connections.get(i);
				if(c.enabled){
					
					orderedConnections.add(c);
				}
			}
		}
	}
	
	public ReactionNetwork getReactionNetwork(){
		return reac;
	}
	
	public ReactionNetwork getReactionNetworksWithParams(double[] genome){
		ReactionNetwork r = reac.clone();
		
		if(!(genome.length== orderedNodes.size()+orderedConnections.size())) 
			System.err.println("Warning, inconsistent sizes. Trying best effort match");
		
		for(int i = 0; i<Math.min(genome.length, orderedNodes.size()); i++){
			Node n = r.getNodeByName(orderedNodes.get(i).name);
			n.parameter = genome[i];
		}
		
		for(int i = 0; i<Math.min(genome.length-orderedNodes.size(), orderedConnections.size()); i++){
			
			Connection c = r.getConnectionByEnds(orderedConnections.get(i).from, orderedConnections.get(i).to);
			c.parameter = genome[i+orderedNodes.size()];
		}
		
		return r;
	}
	
	public static void writeToFile(ReactionNetwork r, String file){
		PrintWriter fileOut;
		try {
			fileOut = new PrintWriter(file);
			fileOut.write(r.toString());
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if(args.length >= 2){
			ReactionNetwork reac = null;
			Gson gson = new GsonBuilder().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
					.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
			BufferedReader in;
			
			try {
				in = new BufferedReader(new FileReader(args[0]));
				reac = gson.fromJson(in, ReactionNetwork.class);
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}
			if(reac == null){
				System.out.println("ERROR: unreadable network");
			}
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(args[1]));
				try {
					String params = br.readLine();
					String[] svals = params.split("\\s+");
					double[] vals = new double[svals.length];
					for(int i = 0; i<vals.length; i++){
						vals[i] = Double.parseDouble(svals[i]);
					}
					OptimizedNetworkWriter onw = new OptimizedNetworkWriter(reac);
					ReactionNetwork mod = onw.getReactionNetworksWithParams(vals);
					String fileName = "./optimized.graph";
					if(args.length >= 3){
						fileName = args[3];
					}
					writeToFile(mod,fileName);
				} catch (IOException e) {
					System.out.println("ERROR: Could not read parameter file");
				}
				br.close();
			} catch (IOException e) {
				System.out.println("ERROR: Could not find parameter file");
			}
		} else {
			System.out.println("ERROR: Not enough arguments");
			System.out.println("usage: (function name) reactionNetworkFile parameterFile [outputName]");
		}

	}

}
