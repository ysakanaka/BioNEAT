package model;

public class Reporter extends Template {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	/**
	 * The reporter class is a specific kind of sequence protected against
	 * the action of exonuclease. It can attach reversibly to its target,
	 * emitting fluorescence based on the amount of double-stranded reporter.
	 * Since regular sequences are not supposed to attach to each other (that
	 * would create many new possible reactions among their templates), we
	 * use a trick: we say that the reporter is a special template that does not
	 * create any new sequence. We only use the internal tempin variable to represent
	 * the state of this "template". concentrationAlone is set to 
	 * (totalConcentration-tempin)* Constants.exoKmTemplate / Constants.exoKmSimple
	 * so that exonuclease saturation is correct. Note that it will still be incorrect
	 * if saturation by templates is not activated.
	 * @param totalConcentration
	 * @param from
	 */
	public Reporter(double totalConcentration, SimpleSequence from) {
		super(totalConcentration, from, null, null);
		//this.concentrationAlone = totalConcentration* Constants.exoKmTemplate / Constants.exoKmSimple;
		this.exoKmFree = Constants.exoKmTemplate;
		this.exoKmInput = -1;
	}

	@Override
	public void reset(double totalConcentration, SimpleSequence from,
			Sequence to, InhibitingSequence inhib) {
		this.totalConcentration = totalConcentration;
		this.concentrationAlone = totalConcentration;//* Constants.exoKmTemplate / Constants.exoKmSimple;
		this.concentrationExtended = 0;
		this.concentrationExtendedProtectedInput = 0;
		this.concentrationInhibited = 0;
		this.concentrationWithBoth = 0;
		this.concentrationWithBothProtectedInput = 0;
		this.concentrationWithBothProtectedOutput = 0;
		this.concentrationWithBothProtectedBoth = 0;
		this.concentrationWithOutput = 0;
		this.concentrationWithProtectedOutput = 0;
		this.concentrationWithInput = 0;
		this.concentrationWithProtecteInput = 0;
		
	}
	
	@Override
	public double[] flux() {
		double[] ans = { 0, inputFlux(), 0, 0,
				0, 0, 0,
				0, 0,
				0, 0,
				0 };
		return ans;
	}
	
	@Override
	public double inputSequenceFlux() {
		double conc = from.getConcentration();
		
		double debug = from.getK()*Constants.Kduplex
				* this.concentrationWithInput 
				- Constants.Kduplex
				* conc
				* (this.totalConcentration-this.concentrationWithInput);
		// System.out.println("Conc: "+conc+" inhib: "+inhib+" kinhib: "+kinhib+" kdup: "+Constants.Kduplex+" Template alone: "+this.concentrationAlone+" w/input: "+this.concentrationWithInput+" w/output: "+this.concentrationWithOutput+" w/both: "+this.concentrationWithBoth);
		return debug;
	}

	@Override
	protected double inputFlux() {
		double concIn = from.getConcentration();
		
		
		double debug = Constants.Kduplex
				* concIn
				* (this.totalConcentration-this.concentrationWithInput)
				- this.concentrationWithInput
				* from.getK()*Constants.Kduplex;
		return debug;
	}
	
	@Override
	public double getPolyUsage(){
		return 0;
	}

	@Override
	public double getNickUsage(){
		return 0;
	}
	
	@Override
	public void setStates(double[] values) throws InvalidConcentrationException {
		if (values.length != 12) {
			System.err
					.println("Error: wrong internal values setting for template "
							+ this);
			return;
		}
		for (int i = 0; i < 12; i++) {
			if (values[i] < 0 || values[i] > this.totalConcentration) {
				// System.err.println("Another one bites the dust. Template "+from+":"+to+". Value "+i);
				// throw new InvalidConcentrationException();
				values[i] = 0;
			}
		}
		// System.out.println("Template from "+from.ID+" (Simple) to "+to.getID()+(to.getClass()==SimpleSequence.class?"(Simple)":"(Inhibiting)")+" is being updated with values "+values[0]+" "+values[1]+" "+values[2]+" "+values[3]+" "+values[4]+" "+values[5]);
		this.concentrationAlone = (this.totalConcentration-values[1])* Constants.exoKmTemplate / Constants.exoKmSimple;
		this.concentrationWithInput = values[1];
	}

	@Override
	public double totalConcentration() {
		return this.totalConcentration;
	}
	
	@Override
	public double[] getStates() {
		double ans[] = { this.totalConcentration, this.concentrationWithInput,
				this.concentrationWithOutput, this.concentrationWithBoth,
				this.concentrationExtended, this.concentrationInhibited,
				this.concentrationWithProtecteInput,
				this.concentrationWithProtectedOutput,
				this.concentrationWithBothProtectedInput,
				this.concentrationWithBothProtectedOutput,
				this.concentrationWithBothProtectedBoth,
				this.concentrationExtendedProtectedInput };
		return ans;
	}
}
