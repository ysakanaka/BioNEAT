package use.processing.rd.tasks;

import java.util.ArrayList;

import use.processing.rd.PatternEvaluator;
import use.processing.rd.RDConstants;
import use.processing.rd.RDPatternFitnessResultIbuki;

public class PositionBasedTaskEvaluation extends OngoingTaskEvaluation {
	
	protected ArrayList<boolean[][]> targets;

	public PositionBasedTaskEvaluation(String name, ArrayList<boolean[][]> targets) {
		super(name);
		this.targets = targets;
	}

	@Override
	public double evaluateNow(float[][][] species) {
		boolean[][] target = getCurrentTarget(); 
		boolean[][] positions = PatternEvaluator.detectGlue(species[RDConstants.glueIndex]);
		evaluation ++;
		return RDPatternFitnessResultIbuki.distanceNicolasExponential(target, positions);
	}
	
	public ArrayList<boolean[][]> getTargets(){
		return targets;
	}
	
	public boolean[][] getTarget(int i){
		if (targets == null || targets.size() == 0) return null;
		return targets.get(i);
	}
	
	public boolean[][] getCurrentTarget(){
		if (targets == null || targets.size() == 0) return null;
		return targets.get(evaluation % targets.size());
	}

	public static void main(String[] args) {
		int repeat = 3; //how many times do we cycle
		float factor = 25; //to make it more interesting, only the lower bottom part is up
		// def test default size concs
		int size = (int)(RDConstants.wsize/RDConstants.spaceStep);
		float[][][] concs = new float[RDConstants.glueIndex+1][size][size];
		for(int i = 0; i< concs.length; i++){
			for(int j = 0; j<concs[i].length; j++){
				for(int k = 0; k<concs[i][j].length; k++){
					concs[i][j][k] = (i+j+k)/factor;
				}
			}
		}
		
		// trivial target + bottom line
		ArrayList<boolean[][]> targets = new ArrayList<boolean[][]>();
		targets.add(PatternEvaluator.detectGlue(concs[RDConstants.glueIndex]));
		targets.add(RDPatternFitnessResultIbuki.getBottomLine());
		
		PositionBasedTaskEvaluation pbte = new PositionBasedTaskEvaluation("test",targets);
		
		for(int i=0; i<repeat*targets.size(); i++)
			System.out.println(pbte.getName()+": "+pbte.evaluateNow(concs));
		
		//Example number 2, with concrete targets
		System.out.println();
		System.out.println("====================");
		System.out.println();
		
		float motifSize = 0.3f;
		ArrayList<boolean[][]> targets2 = new ArrayList<boolean[][]>();
		boolean[][] topLeftCorner = new boolean[size][size];
		boolean[][] bottomRightCorner = new boolean[size][size];
		
		for(int i = 0; i< size; i++){
			for(int j = 0; j < size; j++){
				topLeftCorner[i][j] = (i+j) < (motifSize * size);
				bottomRightCorner[i][j] = (i+j) > ((1.0 - motifSize)*size);
			}
		}
		targets2.add(topLeftCorner);
		targets2.add(bottomRightCorner);
		
		PositionBasedTaskEvaluation pbte2 = new PositionBasedTaskEvaluation("Olaf",targets2);
		
		for(int i=0; i<repeat*targets2.size(); i++)
			System.out.println(pbte2.getName()+": "+pbte2.evaluateNow(concs));
		

	}

}
