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
import common.Static;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import gui.Test;

public class ProtectedSeq extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static ReactionNetwork getInitNetwork() {
		model.Constants.numberOfPoints = 1000;
		ReactionNetwork network = new ReactionNetwork();
		network.nodes = new ArrayList<Node>();
		Node node1 = new Node("a");
		node1.parameter = 40;
		node1.initialConcentration = 10;
		node1.protectedSequence = true;
		network.nodes.add(node1);
		Node node2 = new Node("b");
		node2.parameter = 50;
		node2.initialConcentration = 5;
		node2.reporter = true;
		network.nodes.add(node2);
		Connection conn1 = new Connection(node1, node2);
		conn1.parameter =1;
		network.connections = new ArrayList<Connection>();
		network.connections.add(conn1);
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

					window.txtrTest.setText(Static.gson.toJson(network));

					OligoSystemComplex oligoSystem = new OligoSystemComplex(
							network);
					Map<String, double[]> timeSeries = oligoSystem.calculateTimeSeries(30);
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