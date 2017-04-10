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

	}

}
