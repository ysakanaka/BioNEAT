package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

public class Main {

	private JFrame frmEvolutionaryConstructionFramework;
	private JTabbedPane tabHistory;
	private JProgressBar progressBar;
	private JPanel panelSpecies;
	private JPanel panelFitness;

	public JPanel getPanelFitness() {
		return panelFitness;
	}

	public JPanel getPanelSpecies() {
		return panelSpecies;
	}

	public JTabbedPane getTabHistory() {
		return tabHistory;
	}

	public JFrame getMainForm() {
		return frmEvolutionaryConstructionFramework;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmEvolutionaryConstructionFramework.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmEvolutionaryConstructionFramework = new JFrame();
		frmEvolutionaryConstructionFramework.setTitle("Evolutionary Construction Framework");
		frmEvolutionaryConstructionFramework.setBounds(0, 0, 1600, 860);
		frmEvolutionaryConstructionFramework.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		frmEvolutionaryConstructionFramework.getContentPane().add(progressBar, BorderLayout.SOUTH);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmEvolutionaryConstructionFramework.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		JPanel panelGeneral = new JPanel();
		tabbedPane.addTab("General", null, panelGeneral, null);
		panelGeneral.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerSize(3);
		panelGeneral.add(splitPane);

		panelFitness = new JPanel();
		splitPane.setLeftComponent(panelFitness);
		panelFitness.setLayout(new BorderLayout(0, 0));

		panelSpecies = new JPanel();
		splitPane.setRightComponent(panelSpecies);
		panelSpecies.setLayout(new BorderLayout(0, 0));

		JPanel tabEvolution = new JPanel();
		tabbedPane.addTab("Generations", null, tabEvolution, null);
		tabEvolution.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabEvolution.add(scrollPane_1, BorderLayout.CENTER);

		tabHistory = new JTabbedPane(JTabbedPane.TOP);
		scrollPane_1.setViewportView(tabHistory);
	}

}
