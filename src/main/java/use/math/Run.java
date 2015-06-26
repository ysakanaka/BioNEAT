package use.math;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

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
				new MutateParameter(1), new AddNode(1), new AddActivation(1), new AddInhibition(1) }))));
		population.resetPopulation();
		window.getTabHistory().addTab("Gen " + 0, null, reflectionUI.createObjectForm(population.getPopulationInfo(0)), null);
		for (int i = 1; i < 100; i++) {
			population.evolve();
			window.getTabHistory().addTab("Gen " + i, null, reflectionUI.createObjectForm(population.getPopulationInfo(i)), null);
		}

	}

}
