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
		
		JPanel tabEvolution = new JPanel();
		tabbedPane.addTab("Evolution", null, tabEvolution, null);
				tabEvolution.setLayout(new BorderLayout(0, 0));
																
																tabHistory = new JTabbedPane(JTabbedPane.TOP);
																tabEvolution.add(tabHistory, BorderLayout.CENTER);
																
																		JSplitPane splitPane = new JSplitPane();
																		tabHistory.addTab("General", null, splitPane, null);
																		splitPane.setDividerSize(3);
																		
																				JPanel panel = new JPanel();
																				splitPane.setLeftComponent(panel);
																				panel.setLayout(new BorderLayout(0, 0));
																				
																						JSplitPane splitPane_1 = new JSplitPane();
																						splitPane_1.setDividerSize(3);
																						splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
																						panel.add(splitPane_1);
																						
																								JPanel panelFitness = new JPanel();
																								splitPane_1.setLeftComponent(panelFitness);
																								
																										JPanel panelSpecies = new JPanel();
																										splitPane_1.setRightComponent(panelSpecies);
																										splitPane_1.setDividerLocation(400);
																										
																												JScrollPane scrollPane = new JScrollPane();
																												scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
																												scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
																												splitPane.setRightComponent(scrollPane);
																												
																														JPanel panelIndividual = new JPanel();
																														scrollPane.setViewportView(panelIndividual);
																														splitPane.setDividerLocation(400);
	}

}
