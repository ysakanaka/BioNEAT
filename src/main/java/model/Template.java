package model;

import java.io.Serializable;

public class Template implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4394186475468043658L;
	public double totalConcentration;
	public double concentrationAlone, concentrationWithInput,
			concentrationWithOutput;
	public double concentrationWithBoth, concentrationExtended,
			concentrationInhibited;
	protected SimpleSequence from;
	private Sequence to;
	private InhibitingSequence inhib;
	public double concentrationExtendedProtectedInput;
	public double concentrationWithBothProtectedInput;
	public double concentrationWithBothProtectedOutput;
	public double concentrationWithBothProtectedBoth;
	public double concentrationWithProtectedOutput;
	public double concentrationWithProtecteInput;
	private boolean protectedInput;
	private boolean protectedOutput;
	private double inputSlowdown = 1;
	private double outputSlowdown = 1;
	private double stackSlowdown = 1;

	public double exoKmFree = Constants.exoKmTemplate;
	public double exoKmInput = Constants.exoKmTemplate;
	public double exoKmOutput = Constants.exoKmTemplate;
	public double exoKmBoth = -1;
	public double exoKmExtended = -1;
	public double exoKmInhib = Constants.exoKmTemplate;
	public double exoKmInputProtected = Constants.exoKmTemplate;
	public double exoKmOutputProtected = Constants.exoKmTemplate;
	public double exoKmBothProtectedInput = -1;
	public double exoKmBothProtectedOutput = -1;
	public double exoKmBothProtectedBoth = -1;
	public double exoKmExtendedProtectedInput = -1;

	private Enzyme poly = new Enzyme("pol", Constants.polVm / Constants.polKm,
			Constants.polKm, Constants.polKmBoth);
	private Enzyme nick = new Enzyme("nick", Constants.nickVm
			/ Constants.nickKm, Constants.nickKm);

	public void reset(double totalConcentration, SimpleSequence from,
			Sequence to, InhibitingSequence inhib) {
		this.totalConcentration = totalConcentration;
		this.concentrationAlone = totalConcentration;
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
		this.from = from;
		this.to = to;
		this.inhib = inhib;

		this.protectedInput = from.isProtected();
		this.protectedOutput = to.isProtected();
	}

	public Template(double totalConcentration, SimpleSequence from,
			Sequence to, InhibitingSequence inhib) {
		this.totalConcentration = totalConcentration;
		this.concentrationAlone = totalConcentration;
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
		this.from = from;
		this.to = to;
		this.inhib = inhib;
		this.protectedInput = from.isProtected();
		if (to != null) {
			this.protectedOutput = to.isProtected();
		} else {
			this.protectedOutput = false;
		}

		if (!from.seq.equals("") && !to.seq.equals("")) {
			inputSlowdown = SequenceDependent
					.getInputSlowdown(from.seq, to.seq);
			outputSlowdown = SequenceDependent.getOutputSlowdown(from.seq,
					to.seq);
			stackSlowdown = SequenceDependent
					.getStackSlowdown(from.seq, to.seq);
		}
	}

	public double[] flux() {
		double[] ans = { aloneFlux(), inputFlux(), outputFlux(), bothFlux(),
				extendedFlux(), inhibitedFlux(), inputProtectedFlux(),
				outputProtectedFlux(), bothProtectedInputFlux(),
				bothProtectedOutputFlux(), bothProtectedBothFlux(),
				extendedProtectedInputFlux() };
		return ans;
	}

	public double inhibSequenceFlux() {
		double conc = inhib.getConcentration();
		return inhib.KInhib
				* Constants.Kduplex // Add dangle
				* this.concentrationInhibited
				- Constants.Kduplex
				* conc
				* (this.concentrationAlone + this.concentrationWithInput
						+ this.concentrationWithOutput
						+ this.concentrationWithProtectedOutput + this.concentrationWithProtecteInput)
				+ this.concentrationInhibited
				* Constants.Kduplex
				* (Constants.ratioToeholdLeft * from.getConcentration()
						+ Constants.ratioToeholdLeft
						* from.getProtectedConcentration()
						+ Constants.ratioToeholdRight * to.getConcentration() + Constants.ratioToeholdRight
						* to.getProtectedConcentration());
	}

	public double inputSequenceFlux() {
		double conc = from.getConcentration();
		double inhib;
		double kinhib;
		double debug;
		if (this.inhib != null) {
			inhib = this.inhib.getConcentration();
			kinhib = Constants.Kduplex; // The inhibitor forms a duplex with the
			// template
		} else {
			inhib = 0;
			kinhib = 0;
		}
		debug = from.getK()
				* Constants.Kduplex
				* (inputSlowdown * this.concentrationWithInput + stackSlowdown
						/ inputSlowdown * this.concentrationWithBoth + stackSlowdown
						/ inputSlowdown
						* this.concentrationWithBothProtectedOutput)
				+ kinhib
				* inhib
				* this.concentrationWithInput
				- Constants.Kduplex
				* conc
				* (this.concentrationAlone + this.concentrationWithOutput
						+ this.concentrationWithProtectedOutput + Constants.ratioToeholdLeft
						* this.concentrationInhibited);
		// System.out.println("Conc: "+conc+" inhib: "+inhib+" kinhib: "+kinhib+" kdup: "+Constants.Kduplex+" Template alone: "+this.concentrationAlone+" w/input: "+this.concentrationWithInput+" w/output: "+this.concentrationWithOutput+" w/both: "+this.concentrationWithBoth);
		return debug;
	}

	public double inputProtectedSequenceFlux() {
		if (this.protectedInput) {
			double conc = from.getProtectedConcentration();
			double inhib;
			double kinhib;
			double debug;
			if (this.inhib != null) {
				inhib = this.inhib.getConcentration();
				kinhib = Constants.Kduplex; // The inhibitor forms a duplex with
				// the template
			} else {
				inhib = 0;
				kinhib = 0;
			}
			debug = from.getK()
					* Constants.Kduplex
					* (inputSlowdown * this.concentrationWithProtecteInput
							+ stackSlowdown / inputSlowdown
							* this.concentrationWithBothProtectedInput + stackSlowdown
							/ inputSlowdown
							* this.concentrationWithBothProtectedBoth)
					+ kinhib
					* inhib
					* this.concentrationWithProtecteInput
					- Constants.Kduplex
					* conc
					* (this.concentrationAlone + this.concentrationWithOutput
							+ this.concentrationWithProtectedOutput + Constants.ratioToeholdLeft
							* this.concentrationInhibited);
			// System.out.println("Conc: "+conc+" inhib: "+inhib+" kinhib: "+kinhib+" kdup: "+Constants.Kduplex+" Template alone: "+this.concentrationAlone+" w/input: "+this.concentrationWithInput+" w/output: "+this.concentrationWithOutput+" w/both: "+this.concentrationWithBoth);
			return debug;
		} else {
			return 0;
		}
	}

	public double outputSequenceFlux() {
		double conc = to.getConcentration();
		double inhib;
		double kinhib;
		if (this.inhib != null) {
			inhib = this.inhib.getConcentration();
			kinhib = Constants.Kduplex; // The inhibitor forms a duplex with the
			// template
		} else {
			inhib = 0;
			kinhib = 0;
		}
		double pol; // to represent that we are in the double-stranded case.
		// with the default values, it is 1 anyway.
		if (this.to.getClass() == InhibitingSequence.class) {
			pol = this.poly.basicKm / this.poly.optionalValue
					* this.poly.activity * Constants.displ;
		} else {
			pol = this.poly.activity;
		}

		double debug = to.getK()
				* Constants.Kduplex
				* (outputSlowdown * this.concentrationWithOutput
						+ stackSlowdown / outputSlowdown
						* this.concentrationWithBoth + stackSlowdown
						/ outputSlowdown
						* this.concentrationWithBothProtectedInput)
				+ kinhib
				* inhib
				* this.concentrationWithOutput
				- Constants.Kduplex
				* conc
				* (this.concentrationAlone + this.concentrationWithInput
						+ this.concentrationWithProtecteInput + Constants.ratioToeholdRight
						* this.concentrationInhibited)
				+ pol
				* (this.concentrationWithBoth + this.concentrationWithBothProtectedInput);
		// System.out.println("OutputSequenceFlux: "+debug);
		return debug;
	}

	public double outputProtectedSequenceFlux() {
		if (this.protectedOutput) {
			double conc = to.getProtectedConcentration();
			double inhib;
			double kinhib;
			if (this.inhib != null) {
				inhib = this.inhib.getConcentration();
				kinhib = Constants.Kduplex; // The inhibitor forms a duplex with
				// the template
			} else {
				inhib = 0;
				kinhib = 0;
			}
			double pol; // to represent that we are in the double-stranded case.
			// with the default values, it is 1 anyway.
			if (this.to.getClass() == InhibitingSequence.class) {
				pol = this.poly.basicKm / this.poly.optionalValue
						* this.poly.activity * Constants.displ;
			} else {
				pol = this.poly.activity;
			}

			double debug = to.getK()
					* Constants.Kduplex
					* (outputSlowdown * this.concentrationWithProtectedOutput
							+ stackSlowdown / outputSlowdown
							* this.concentrationWithBothProtectedOutput + stackSlowdown
							/ outputSlowdown
							* this.concentrationWithBothProtectedBoth)
					+ kinhib
					* inhib
					* this.concentrationWithProtectedOutput
					- Constants.Kduplex
					* conc
					* (this.concentrationAlone + this.concentrationWithInput
							+ this.concentrationWithProtecteInput + Constants.ratioToeholdRight
							* this.concentrationInhibited)
					+ pol
					* (this.concentrationWithBothProtectedOutput + this.concentrationWithBothProtectedBoth);
			// System.out.println("OutputSequenceFlux: "+debug);
			return debug;
		} else {
			return 0;
		}
	}

	protected double aloneFlux() {
		double concIn = from.getConcentration();
		double concprotectIn = from.getProtectedConcentration();
		double concOut = to.getConcentration();
		double concprotectOut = to.getProtectedConcentration();
		double concInhib;
		double kreleaseInhib;
		if (this.inhib != null) {
			concInhib = this.inhib.getConcentration();
			kreleaseInhib = this.inhib.KInhib * Constants.Kduplex;
		} else {
			concInhib = 0;
			kreleaseInhib = 0;
		}

		double debug = from.getK()
				* Constants.Kduplex
				* inputSlowdown
				* (this.concentrationWithInput + this.concentrationWithProtecteInput)
				+ to.getK()
				* Constants.Kduplex
				* outputSlowdown
				* (this.concentrationWithOutput + this.concentrationWithProtectedOutput)
				+ kreleaseInhib
				* this.concentrationInhibited
				- this.concentrationAlone
				* Constants.Kduplex
				* (concIn + concprotectIn + concOut + concprotectOut + concInhib)
				- this.poly.activity * this.concentrationAlone
				* Constants.leakRatio;
		// System.out.println("AloneFlux = "+ debug);
		return debug;
	}

	protected double inputFlux() {
		double concIn = from.getConcentration();
		double concOut = to.getConcentration();
		double concprotectOut = to.getProtectedConcentration();
		double concInhib;
		if (this.inhib != null) {
			concInhib = this.inhib.getConcentration();
		} else {
			concInhib = 0;
		}
		return Constants.Kduplex
				* concIn
				* (this.concentrationAlone + Constants.ratioToeholdLeft
						* this.concentrationInhibited)
				+ to.getK()
				* Constants.Kduplex
				* stackSlowdown
				/ inputSlowdown
				* (this.concentrationWithBoth + this.concentrationWithBothProtectedOutput)
				- this.concentrationWithInput
				* (from.getK() * Constants.Kduplex * inputSlowdown
						+ this.poly.activity + Constants.Kduplex
						* (concOut + concprotectOut) + Constants.Kduplex
						* concInhib);
	}

	private double inputProtectedFlux() {
		if (this.protectedInput) {
			double concprotectIn = from.getProtectedConcentration();
			double concOut = to.getConcentration();
			double concprotectOut = to.getProtectedConcentration();
			double concInhib;
			if (this.inhib != null) {
				concInhib = this.inhib.getConcentration();
			} else {
				concInhib = 0;
			}
			return Constants.Kduplex
					* concprotectIn
					* (this.concentrationAlone + Constants.ratioToeholdLeft
							* this.concentrationInhibited)
					+ to.getK()
					* Constants.Kduplex
					* stackSlowdown
					/ inputSlowdown
					* (this.concentrationWithBothProtectedInput + this.concentrationWithBothProtectedBoth)
					- this.concentrationWithProtecteInput
					* (from.getK() * Constants.Kduplex * inputSlowdown
							+ this.poly.activity + Constants.Kduplex
							* (concOut + concprotectOut) + Constants.Kduplex
							* concInhib);
		} else {
			return 0;
		}

	}

	protected double outputFlux() {

		double concIn = from.getConcentration();
		double concprotectIn = from.getProtectedConcentration();
		double concOut = to.getConcentration();
		double concInhib;
		if (this.inhib != null) {
			concInhib = this.inhib.getConcentration();
		} else {
			concInhib = 0;
		}
		double debug = concOut
				* Constants.Kduplex
				* (this.concentrationAlone + Constants.ratioToeholdRight
						* this.concentrationInhibited)
				+ from.getK()
				* Constants.Kduplex
				* stackSlowdown
				/ outputSlowdown
				* (this.concentrationWithBoth + this.concentrationWithBothProtectedInput)
				- this.concentrationWithOutput
				* (to.getK() * Constants.Kduplex * outputSlowdown
						+ Constants.Kduplex * (concIn + concprotectIn) + Constants.Kduplex
						* concInhib) + this.poly.activity
				* this.concentrationAlone * Constants.leakRatio;
		// System.out.println("OutputFlux =="+debug);
		return debug;
	}

	private double outputProtectedFlux() {
		if (this.protectedOutput) {
			double concIn = from.getConcentration();
			double concprotectIn = from.getProtectedConcentration();
			double concprotectOut = to.getProtectedConcentration();
			double concInhib;
			if (this.inhib != null) {
				concInhib = this.inhib.getConcentration();
			} else {
				concInhib = 0;
			}
			double debug = concprotectOut
					* Constants.Kduplex
					* (this.concentrationAlone + Constants.ratioToeholdRight
							* this.concentrationInhibited)
					+ from.getK()
					* Constants.Kduplex
					* stackSlowdown
					/ outputSlowdown
					* (this.concentrationWithBothProtectedBoth + this.concentrationWithBothProtectedOutput)
					- this.concentrationWithProtectedOutput
					* (to.getK() * Constants.Kduplex * outputSlowdown
							+ Constants.Kduplex * (concIn + concprotectIn) + Constants.Kduplex
							* concInhib);
			// System.out.println("OutputFlux =="+debug);
			return debug;
		} else {
			return 0;
		}
	}

	protected double bothFlux() {
		double concIn = from.getConcentration();
		double concOut = to.getConcentration();
		double pol = (to.getClass() == InhibitingSequence.class ? this.poly.basicKm
				/ this.poly.optionalValue
				* this.poly.activity
				* Constants.displ
				: this.poly.activity);

		return Constants.Kduplex
				* concIn
				* this.concentrationWithOutput
				+ Constants.Kduplex
				* concOut
				* this.concentrationWithInput
				+ this.nick.activity
				* this.concentrationExtended
				- this.concentrationWithBoth
				* (from.getK() * Constants.Kduplex * stackSlowdown + to.getK()
						* Constants.Kduplex * stackSlowdown + pol);
	}

	private double bothProtectedInputFlux() {
		if (this.protectedInput) {
			double concprotectIn = from.getProtectedConcentration();
			double concOut = to.getConcentration();
			double pol = (to.getClass() == InhibitingSequence.class ? this.poly.basicKm
					/ this.poly.optionalValue
					* this.poly.activity
					* Constants.displ
					: this.poly.activity);

			return Constants.Kduplex
					* concprotectIn
					* this.concentrationWithOutput
					+ Constants.Kduplex
					* concOut
					* this.concentrationWithProtecteInput
					+ this.nick.activity
					* this.concentrationExtendedProtectedInput
					- this.concentrationWithBothProtectedInput
					* (from.getK() * Constants.Kduplex * stackSlowdown
							/ inputSlowdown + to.getK() * Constants.Kduplex
							* stackSlowdown / outputSlowdown + pol);
		} else {
			return 0;
		}
	}

	private double bothProtectedOutputFlux() {
		if (this.protectedOutput) {
			double concIn = from.getConcentration();
			double concprotectOut = to.getProtectedConcentration();
			double pol = (to.getClass() == InhibitingSequence.class ? this.poly.basicKm
					/ this.poly.optionalValue
					* this.poly.activity
					* Constants.displ
					: this.poly.activity);

			return Constants.Kduplex
					* concIn
					* this.concentrationWithProtectedOutput
					+ Constants.Kduplex
					* concprotectOut
					* this.concentrationWithInput
					- this.concentrationWithBothProtectedOutput
					* (from.getK() * Constants.Kduplex * stackSlowdown
							/ inputSlowdown + to.getK() * Constants.Kduplex
							* stackSlowdown / outputSlowdown + pol);
		} else {
			return 0;
		}
	}

	private double bothProtectedBothFlux() {
		if (this.protectedInput && this.protectedOutput) {
			double concprotectIn = from.getProtectedConcentration();
			double concprotectOut = to.getProtectedConcentration();
			double pol = (to.getClass() == InhibitingSequence.class ? this.poly.basicKm
					/ this.poly.optionalValue
					* this.poly.activity
					* Constants.displ
					: this.poly.activity);

			return Constants.Kduplex
					* concprotectIn
					* this.concentrationWithProtectedOutput
					+ Constants.Kduplex
					* concprotectOut
					* this.concentrationWithProtecteInput
					- this.concentrationWithBothProtectedBoth
					* (from.getK() * Constants.Kduplex * stackSlowdown
							/ inputSlowdown + to.getK() * Constants.Kduplex
							* stackSlowdown / outputSlowdown + pol);
		} else {
			return 0;
		}
	}

	private double extendedFlux() {
		double poldispl = (to.getClass() == InhibitingSequence.class ? this.poly.basicKm
				/ this.poly.optionalValue
				* this.poly.activity
				* Constants.displ
				: this.poly.activity);

		return this.poly.activity // because single stranded after the input
				* this.concentrationWithInput
				+ poldispl
				* (this.concentrationWithBoth + this.concentrationWithBothProtectedOutput)
				- this.nick.activity * this.concentrationExtended;
	}

	private double extendedProtectedInputFlux() {
		if (this.protectedInput) {
			double poldispl = (to.getClass() == InhibitingSequence.class ? this.poly.basicKm
					/ this.poly.optionalValue
					* this.poly.activity
					* Constants.displ
					: this.poly.activity);

			return this.poly.activity
					* this.concentrationWithProtecteInput
					+ poldispl
					* (this.concentrationWithBothProtectedInput + this.concentrationWithBothProtectedBoth)
					- this.nick.activity
					* this.concentrationExtendedProtectedInput;
		} else {
			return 0;
		}
	}

	private double inhibitedFlux() {
		double conc, krelease;
		if (this.inhib != null) {
			conc = this.inhib.getConcentration();
			krelease = this.inhib.KInhib * Constants.Kduplex;
		} else {
			conc = 0;
			krelease = 0;
		}

		return -krelease
				* this.concentrationInhibited
				+ Constants.Kduplex
				* conc
				* (this.concentrationAlone + this.concentrationWithInput
						+ this.concentrationWithOutput
						+ this.concentrationWithProtecteInput + this.concentrationWithProtectedOutput)
				- this.concentrationInhibited
				* Constants.Kduplex
				* (Constants.ratioToeholdLeft * from.getConcentration()
						+ Constants.ratioToeholdLeft
						* from.getProtectedConcentration()
						+ Constants.ratioToeholdRight * to.getConcentration() + Constants.ratioToeholdRight
						* to.getProtectedConcentration());
	}

	/**
	 * 
	 * @return [template+In]/Km,in + [template+in+out]/Km,both
	 */
	public double getPolyUsage() {
		return (this.concentrationWithInput + this.concentrationWithProtecteInput)
				/ Constants.polKm
				+ (this.concentrationWithBoth
						+ this.concentrationWithBothProtectedInput
						+ this.concentrationWithBothProtectedOutput + this.concentrationWithBothProtectedBoth)
				/ Constants.polKmBoth;
	}

	public void setAllocatedPoly(Enzyme poly) {
		this.poly = poly;
	}

	public double getNickUsage() {
		return (this.concentrationExtended + this.concentrationExtendedProtectedInput)
				/ Constants.nickKm
				+ (this.concentrationWithInput
						+ this.concentrationWithProtecteInput
						+ this.concentrationWithBoth
						+ this.concentrationWithBothProtectedBoth
						+ this.concentrationWithBothProtectedInput + this.concentrationWithBothProtectedOutput)
				/ Constants.nickKmProducts;
	}

	public void setAllocatedNick(Enzyme nick) {
		this.nick = nick;
	}

	public double getExoSaturation() {
		double value = 0;
		if (Constants.exoSaturationByFreeTemplates) {
			value += this.concentrationAlone / this.exoKmFree;
			if (Constants.exoSaturationByTemplatesAll) {
				value += exoKmInput != -1 ? this.concentrationWithInput
						/ this.exoKmInput : 0.0;
				value += exoKmOutput != -1 ? this.concentrationWithOutput
						/ this.exoKmOutput : 0.0;
				value += exoKmBoth != -1 ? this.concentrationWithBoth
						/ this.exoKmBoth : 0.0;
				value += exoKmExtended != -1 ? this.concentrationExtended
						/ this.exoKmExtended : 0.0;
				value += exoKmInhib != -1 ? this.concentrationInhibited
						/ this.exoKmInhib : 0.0;
				value += exoKmBothProtectedInput != -1 ? this.concentrationWithBothProtectedInput
						/ this.exoKmBothProtectedInput
						: 0.0;
				value += exoKmBothProtectedOutput != -1 ? this.concentrationWithBothProtectedOutput
						/ this.exoKmBothProtectedOutput
						: 0.0;
				value += exoKmBothProtectedBoth != -1 ? this.concentrationWithBothProtectedBoth
						/ this.exoKmBothProtectedBoth
						: 0.0;
				value += exoKmExtendedProtectedInput != -1 ? this.concentrationExtendedProtectedInput
						/ this.exoKmExtendedProtectedInput
						: 0.0;
				value += exoKmInputProtected != -1 ? this.concentrationWithProtecteInput
						/ this.exoKmInputProtected
						: 0.0;
				value += exoKmOutputProtected != -1 ? this.concentrationWithProtectedOutput
						/ this.exoKmOutputProtected
						: 0.0;

			}
		}
		return value;
	}

	public double[] getStates() {
		double ans[] = { this.concentrationAlone, this.concentrationWithInput,
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
		this.concentrationAlone = values[0];
		this.concentrationWithInput = values[1];
		this.concentrationWithOutput = values[2];
		this.concentrationWithBoth = values[3];
		this.concentrationExtended = values[4];
		this.concentrationInhibited = values[5];
		this.concentrationWithProtecteInput = values[6];
		this.concentrationWithProtectedOutput = values[7];
		this.concentrationWithBothProtectedInput = values[8];
		this.concentrationWithBothProtectedOutput = values[9];
		this.concentrationWithBothProtectedBoth = values[10];
		this.concentrationExtendedProtectedInput = values[11];
	}

	public SimpleSequence getFrom() {
		return from;
	}

	public double totalConcentration() {
		return this.concentrationAlone + this.concentrationExtended
				+ this.concentrationInhibited + this.concentrationWithBoth
				+ this.concentrationWithInput + this.concentrationWithOutput
				+ this.concentrationExtendedProtectedInput
				+ this.concentrationWithBothProtectedBoth
				+ this.concentrationWithBothProtectedInput
				+ this.concentrationWithBothProtectedOutput
				+ this.concentrationWithProtectedOutput
				+ this.concentrationWithProtecteInput;
	}
}
