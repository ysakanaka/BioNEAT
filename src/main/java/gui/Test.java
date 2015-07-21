package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import reactionnetwork.visual.RNVisualizationViewerFactory;
import use.oligomodel.OligoSystemComplex;
import use.oligomodel.PlotFactory;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Test {

	public JFrame frame;
	public JPanel panelTopology;
	public JTextArea txtrTest;
	public JPanel panelBehavior;
	private JButton btnSimulate;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Test window = new Test();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Test() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 570, 576);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(5, 5));

		panelTopology = new JPanel();
		panelTopology.setPreferredSize(new Dimension(250, 250));
		panelTopology.setSize(new Dimension(250, 250));
		panel.add(panelTopology, BorderLayout.WEST);
		panelTopology.setLayout(new BorderLayout(0, 0));

		JScrollPane panelInformation = new JScrollPane();
		panelInformation.setPreferredSize(new Dimension(250, 250));
		panelInformation.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(panelInformation, BorderLayout.CENTER);

		txtrTest = new JTextArea();
		txtrTest.setText("Eclipse.org\r\nWindowBuilder Pro Eclipse Update Site for Eclipse 4.4\r\n\r\nWindowBuilder Pro Eclipse is a tool for creation of RCP, SWT, and Swing UI's in Eclipse.\r\n\r\nInstallation\r\n\r\nIf you have previously installed WindowBuilder Pro Eclipse without using the Update Manager (e.g. using the WindowBuilder Pro Eclipse installer) \r\nthen you must first uninstall WindowBuilder Pro Eclipse before installing from this update site.\r\n\r\nTo install WindowBuilder Pro Eclipse from this update site\r\n\r\nSelect \"Help > Install New Software...\" in the main menu to open the \"Install\" dialog\r\nInstall New Software Menu\r\nDrag the URL for this update site and drop it into the \"Work with\" field of the \"Install\" dialog \r\n(Click and drag the icon in front of the URL in your browser's address bar to drag the URL)\r\nDrag Url\r\nIf you cannot drag the URL from your browser, then click the \"Add...\" button in the \"Install\" dialog \r\nthen copy and paste the URL into the \"Add Site\" dialog and click \"OK\".\r\n\r\nIf you add the URL but WindowBuilder Pro Eclipse does not appear in the list of sites, \r\nthen pull down the \"Work with\" field and select \"All Available Sites\".\r\n\r\nAll Available Sites\r\nCheck all features to be installed in the \"Install\" dialog\r\nClick the \"Next\" button\r\nSite Content\r\n\r\nThis update site contains a number of features and plugins");
		panelInformation.setViewportView(txtrTest);

		panelBehavior = new JPanel();
		frame.getContentPane().add(panelBehavior, BorderLayout.CENTER);
		panelBehavior.setLayout(new BorderLayout(0, 0));

		btnSimulate = new JButton("Simulate");
		btnSimulate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panelBehavior.removeAll();
				String json = txtrTest.getText();
				Gson gson = new GsonBuilder().setPrettyPrinting()
						.registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
						.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
				ReactionNetwork network = gson.fromJson(json, ReactionNetwork.class);
				OligoSystemComplex oligoSystem = new OligoSystemComplex(network);
				Map<String, double[]> timeSeries = oligoSystem.calculateTimeSeries();
				PlotFactory plotFactory = new PlotFactory();
				JPanel timeSeriesPanel = plotFactory.createTimeSeriesPanel(timeSeries);
				panelBehavior.add(timeSeriesPanel, BorderLayout.CENTER);
				panelBehavior.revalidate();

				RNVisualizationViewerFactory factory = new RNVisualizationViewerFactory();
				VisualizationViewer<String, String> vv = factory
						.createVisualizationViewer(network);
				panelTopology.removeAll();
				panelTopology.add(vv, BorderLayout.CENTER);
				panelTopology.revalidate();
			}
		});
		frame.getContentPane().add(btnSimulate, BorderLayout.SOUTH);
	}

	ReactionNetwork initReactionNetwork() {
		ReactionNetwork network = new ReactionNetwork();
		network.nodes = new ArrayList<Node>();
		Node node1 = new Node("a");
		node1.parameter = 100;
		node1.initialConcentration = 10;
		Node node2 = new Node("b");
		node2.parameter = 1;
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
		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
				.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
		String json = gson.toJson(network);

		// Testing the Json deserializer
		ReactionNetwork newNetwork = gson.fromJson(json, ReactionNetwork.class);

		// Testing the clone
		ReactionNetwork clone = newNetwork.clone();

		return clone;
	}

}
