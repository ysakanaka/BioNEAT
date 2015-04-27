package demo;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JApplet;
import javax.swing.JPanel;

import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import reactionnetwork.visual.RNVisualizationViewerFactory;
import use.oligomodel.OligoSystemComplex;
import use.oligomodel.PlotFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import gui.Test;

public class Oligator extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static ReactionNetwork getInitNetwork() {
		ReactionNetwork network = new ReactionNetwork();
		network.nodes = new ArrayList<Node>();
		Node node1 = new Node("a");
		node1.parameter = 40;
		node1.initialConcentration = 10;
		Node node2 = new Node("b");
		node2.parameter = 50;
		node2.initialConcentration = 5;
		Node node3 = new Node("Iaa");
		node3.parameter = 0.1;
		node3.type = Node.INHIBITING_SEQUENCE;
		network.nodes.add(node1);
		network.nodes.add(node2);
		network.nodes.add(node3);
		Connection conn1 = new Connection(node1, node2);
		conn1.parameter =1;
		Connection conn2 = new Connection(node1, node1);
		conn2.parameter = 5;
		Connection conn3 = new Connection(node2, node3);
		conn3.parameter = 15;
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

		// Testing the Json deserializer
		ReactionNetwork newNetwork = gson.fromJson(json, ReactionNetwork.class);

		// Testing the clone
		ReactionNetwork clone = newNetwork.clone();
		return clone;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Test window = new Test();
					window.frame.setVisible(true);

					ReactionNetwork network = getInitNetwork();

					RNVisualizationViewerFactory factory = new RNVisualizationViewerFactory();
					VisualizationViewer<String, String> vv = factory
							.createVisualizationViewer(network);
					window.panelTopology.add(vv, BorderLayout.CENTER);

					window.txtrTest.setText(network.toString());

					OligoSystemComplex oligoSystem = new OligoSystemComplex(
							network);
					Map<String, double[]> timeSeries = oligoSystem
							.calculateTimeSeries();
					PlotFactory plotFactory = new PlotFactory();
					JPanel timeSeriesPanel = plotFactory
							.createTimeSeriesPanel(timeSeries);
					window.panelBehavior.add(timeSeriesPanel,
							BorderLayout.CENTER);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
