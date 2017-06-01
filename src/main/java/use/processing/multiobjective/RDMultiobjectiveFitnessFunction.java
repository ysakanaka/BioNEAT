package use.processing.multiobjective;

import java.util.ArrayList;

import erne.AbstractFitnessResult;
import reactionnetwork.ReactionNetwork;
import use.processing.rd.RDFitnessFunction;
import use.processing.rd.RDSystem;

public class RDMultiobjectiveFitnessFunction extends RDFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7817656697732864457L;
	
	/**
	 * Objective computed on a single run of a system
	 */
	protected ArrayList<RDObjective> directObjs;
	
	/**
	 * Objective computed on multiple runs of a system (e.g. stability, average fitness, etc...)
	 */
	protected ArrayList<RDMultievaluationObjective> multievaluationObjs;
	
	public RDMultiobjectiveFitnessFunction(boolean[][] pattern){
		super(pattern);
		directObjs = new ArrayList<RDObjective>();
		directObjs.add(new RDInObjective());
		directObjs.add(new RDOutObjective());
		multievaluationObjs = new ArrayList<RDMultievaluationObjective>();
	}
	
	public RDMultiobjectiveFitnessFunction(boolean[][] pattern, ArrayList<RDObjective> directObjs){
		super(pattern);
		this.directObjs = directObjs;
		multievaluationObjs = new ArrayList<RDMultievaluationObjective>();
	}
	
	public RDMultiobjectiveFitnessFunction(boolean[][] pattern, ArrayList<RDObjective> directObjs, ArrayList<RDMultievaluationObjective> multievaluationObjs){
		super(pattern);
		this.directObjs = directObjs;
		this.multievaluationObjs = multievaluationObjs;
	}

	@Override
	protected AbstractFitnessResult computeResult(RDSystem syst){
		
		return new RDMultiobjectivePatternFitnessResult(syst,pattern,directObjs);
	}
	
	@Override
	protected AbstractFitnessResult[] multipleEvaluation(ReactionNetwork network){
		//TODO: horrible cast, may blow up any minute (based on extending classes)
		AbstractFitnessResult[] results = super.multipleEvaluation(network);
		RDMultiobjectivePatternFitnessResult[] results2 = new RDMultiobjectivePatternFitnessResult[results.length];
		
		for(int i= 0; i< results.length; i++){
			results2[i] = (RDMultiobjectivePatternFitnessResult) results[i];
		}
		
		for(int i = 0; i<multievaluationObjs.size(); i++){
			double fit = multievaluationObjs.get(i).evaluateScore(results2);
			for(int j = 0; j<results2.length; j++){
				results2[j].addFit(fit);
			}
		}
		
		
		return results2;
	}
	
}
