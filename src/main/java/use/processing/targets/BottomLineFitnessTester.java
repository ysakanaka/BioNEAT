package use.processing.targets;

import erne.AbstractFitnessResult;
import use.processing.rd.RDConstants;
import use.processing.rd.RDFitnessFunction;
import utils.GraphMaker;

public class BottomLineFitnessTester {

	public static float width = 0.3f;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
boolean[][] target = new boolean[(int)(RDConstants.wsize/RDConstants.spaceStep)][(int)(RDConstants.hsize/RDConstants.spaceStep)];
		
		for(int i = 0; i<target.length; i++){
			for(int j = 0; j<target[i].length;j++){
				target[i][j] = (j>=target.length*(1.0f-width));
			}
		}
		
		RDConstants.evalRandomDistance = false;
		RDConstants.defaultRandomFitness = 0.24;
		
		RDFitnessFunction fitnessFunction = new RDFitnessFunction(target);
		
		AbstractFitnessResult res = fitnessFunction.evaluate(GraphMaker.bottomLine());
		System.out.println(res.getFitness());
	}

}
