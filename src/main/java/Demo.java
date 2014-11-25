
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import reactionnetwork.visual.RNGraph;
import reactionnetwork.visual.RNRenderer;
import reactionnetwork.visual.RNVisualizationViewer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.UserDatumNumberEdgeValue;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.utils.TestGraphs;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;

public class Demo extends JApplet {
	private static JPanel getGraphPanel() {

		ReactionNetwork network = new ReactionNetwork();
		network.nodes = new ArrayList<Node>();
		Node node1 = new Node("a");
		node1.parameter = 100;
		Node node2 = new Node("b");
		node2.parameter = 50;
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
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(ReactionNetwork.class,
						new ReactionNetworkDeserializer())
				.registerTypeAdapter(Connection.class,
						new ConnectionSerializer()).create();
		String json = gson.toJson(network);
		System.out.println(json);

		ReactionNetwork newNetwork = gson.fromJson(json, ReactionNetwork.class);
		System.out.println(newNetwork);
		
		ReactionNetwork clone = newNetwork.clone();

		RNGraph g = new RNGraph(newNetwork); // initial graph

		final RNVisualizationViewer vv = new RNVisualizationViewer(
				new ISOMLayout(g), new RNRenderer());

		final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		graphMouse.setMode(Mode.PICKING);
		vv.setGraphMouse(graphMouse);
		vv.setPickSupport(new ShapePickSupport());
		JPanel jp = new JPanel();
		jp.setBackground(Color.WHITE);
		jp.setLayout(new BorderLayout());
		jp.add(vv, BorderLayout.CENTER);

		JPanel control_panel = new JPanel(new GridLayout(2, 1));
		JPanel topControls = new JPanel();
		JPanel bottomControls = new JPanel();
		control_panel.add(topControls);
		control_panel.add(bottomControls);
		jp.add(control_panel, BorderLayout.NORTH);
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
