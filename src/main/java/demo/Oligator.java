package demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;

import oligomodel.OligoSystemComplex;
import oligomodel.PlotFactory;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import reactionnetwork.visual.RNVisualizationViewer;
import reactionnetwork.visual.RNVisualizationViewerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Oligator extends JApplet {
	private static JPanel getGraphPanel() {

		// Manually creaet a ReactionNetwork object
		ReactionNetwork network = new ReactionNetwork();
		network.nodes = new ArrayList<Node>();
		Node node1 = new Node("a");
		node1.parameter = 100;
		node1.initialConcentration = 10;
		Node node2 = new Node("b");
		node2.parameter = 50;
		node2.initialConcentration = 5;
		Node node3 = new Node("Iaa");
		node3.type = Node.INHIBITING_SEQUENCE;
		network.nodes.add(node1);
		network.nodes.add(node2);
		network.nodes.add(node3);
		Connection conn1 = new Connection(node1, node2);
		conn1.parameter = 60;
		Connection conn2 = new Connection(node1, node1);
		conn2.parameter = 50;
		Connection conn3 = new Connection(node2, node3);
		conn3.parameter = 40;
		network.connections = new ArrayList<Connection>();
		network.connections.add(conn1);
		network.connections.add(conn2);
		network.connections.add(conn3);
		network.parameters.put("exo", 10.0);
		network.parameters.put("pol", 10.0);
		network.parameters.put("nick", 10.0);

		// Testing the Json serializer
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(ReactionNetwork.class,
						new ReactionNetworkDeserializer())
				.registerTypeAdapter(Connection.class,
						new ConnectionSerializer()).create();
		String json = gson.toJson(network);
		System.out.println(json);

		// Testing the Json deserializer
		ReactionNetwork newNetwork = gson.fromJson(json, ReactionNetwork.class);
		System.out.println(newNetwork);

		// Testing the clone
		ReactionNetwork clone = newNetwork.clone();
		System.out.println(newNetwork);

		// create visualization from a reaction network
		RNVisualizationViewerFactory factory = new RNVisualizationViewerFactory();
		RNVisualizationViewer vv = factory.createVisualizationViewer(clone);

		// display reaction network
		JPanel jp = new JPanel();
		jp.setBackground(Color.WHITE);
		jp.setLayout(new BorderLayout());
		jp.add(vv, BorderLayout.CENTER);

		// generate time series chart from a reaction network using
		// OligoSystemComplex
		OligoSystemComplex oligoSystem = new OligoSystemComplex(clone);
		Map<String, double[]> timeSeries = oligoSystem.calculateTimeSeries();
		PlotFactory plotFactory = new PlotFactory();
		JPanel timeSeriesPanel = plotFactory.createTimeSeriesPanel(timeSeries);
		jp.add(timeSeriesPanel, BorderLayout.SOUTH);

		return jp;
	}

	public void start() {
		this.getContentPane().add(getGraphPanel());
	}

	public static void main(String[] args) {
		JPanel jp = getGraphPanel();

		JFrame jf = new JFrame();
		jf.getContentPane().add(jp);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
	}
}
