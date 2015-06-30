package use.math;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.lang3.SerializationUtils;

import cluster.Cluster;
import reactionnetwork.Library;
import xy.reflect.ui.ReflectionUI;
import erne.Population;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.rules.AddActivation;
import erne.mutation.rules.AddInhibition;
import erne.mutation.rules.AddNode;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import gui.Main;

public class Run {

	public static Main window;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new Main();
					window.getMainForm().setVisible(true);
					Cluster.bindProgressBar(window.getProgressBar());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		ReflectionUI reflectionUI = new ReflectionUI();

		Population population = new Population(200, Library.startingMath);
		population.setFitnessFunction(new SquareFitnessFunction());
		population.setMutator(new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] { new DisableTemplate(1),
				new MutateParameter(90), new AddNode(2), new AddActivation(2), new AddInhibition(5) }))));
		for (int i = 0; i < 100; i++) {
			if (i == 0) {
				population.resetPopulation();
			} else {
				population.evolve();
			}
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			window.getTabHistory().addTab("Gen " + i, null, scrollPane, null);
			scrollPane.setViewportView(reflectionUI.createObjectForm(population.getPopulationInfo(i)));
		}

	}

}
