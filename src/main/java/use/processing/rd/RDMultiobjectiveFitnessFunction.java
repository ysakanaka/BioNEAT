package use.processing.rd;

import erne.AbstractFitnessResult;

public class RDMultiobjectiveFitnessFunction extends RDFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7817656697732864457L;
	
	public RDMultiobjectiveFitnessFunction(boolean[][] pattern){
		super(pattern);
	}

	@Override
	protected AbstractFitnessResult computeResult(RDSystem syst){
		return new RDMultiobjectivePatternFitnessResult(syst.conc,pattern);
	}
	
}
