package use.processing.targets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import use.processing.mutation.rules.AddActivationWithGradients;
import use.processing.mutation.rules.AddInhibitionWithGradients;
import use.processing.mutation.rules.AddNodeWithGradients;
import use.processing.rd.RDBeadPositionFitnessFunction;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessDisplayer;
import utils.RDLibrary;

/**
 * Defines a target with a centered line of ratio width compared to the whole area
 * @author naubertkato
 *
 */
public class RDLine {
	
	public static float width = 0.15f; //not used right now
	public static float offset = 0.5f*RDConstants.hsize;
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		boolean[][] target = new boolean[(int)(RDConstants.wsize/RDConstants.spaceStep)][(int)(RDConstants.hsize/RDConstants.spaceStep)];
		
		for(int i = 0; i<target.length; i++){
			for(int j = 0; j<target[i].length;j++){
				target[i][j] = (i>=target.length*(0.5f-width/2.0f)&&i<=target.length*(0.5f+width/2.0f));
			}
		}
		
		
		
		
		RDBeadPositionFitnessFunction fitnessFunction = new RDBeadPositionFitnessFunction(new BeadLineTarget(offset), target);
		//RDFitnessFunction fitnessFunction = new RDFitnessFunction(target);
		
        Mutator mutator = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {
    			new DisableTemplate(2), new MutateParameter(10), new AddNodeWithGradients(2), new AddActivationWithGradients(2), new AddInhibitionWithGradients(5)})));
		Evolver evolver = new Evolver(Evolver.DEFAULT_POP_SIZE, Evolver.MAX_GENERATIONS, RDLibrary.rdstart, fitnessFunction, mutator, new RDFitnessDisplayer());
		//evolver.setGUI(false);
		evolver.setExtraConfig(RDConstants.configsToString());
		evolver.evolve();
        System.out.println("Evolution completed.");
        //System.exit(0);
	}
}
