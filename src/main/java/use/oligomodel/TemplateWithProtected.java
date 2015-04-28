package use.oligomodel;

import java.io.Serializable;

import model.Constants;
import model.OligoGraph;
import model.SlowdownConstants;
import model.chemicals.Enzyme;
import model.chemicals.InvalidConcentrationException;
import model.chemicals.SequenceVertex;
import model.chemicals.Template;


public class TemplateWithProtected<E> extends Template<E>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4394186475468043658L;
	 public int numberOfStates = 12;
	
	public double concentrationExtendedProtectedInput;
	public double concentrationWithBothProtectedInput;
	public double concentrationWithBothProtectedOutput;
	public double concentrationWithBothProtectedBoth;
	public double concentrationWithProtectedOutput;
	public double concentrationWithProtecteInput;
	private boolean protectedInput;
	private boolean protectedOutput;
	

	public void reset(double totalConcentration, SequenceVertex from,
			SequenceVertex to, SequenceVertex inhib) {
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
		
		this.protectedInput = ProtectedSequenceVertex.class.isAssignableFrom(from.getClass());
		this.protectedOutput = ProtectedSequenceVertex.class.isAssignableFrom(to.getClass());
	}

	public TemplateWithProtected(OligoGraph<SequenceVertex, E> parent, double totalConcentration, double stackSlowdown, double dangleL, double dangleR, SequenceVertex from, SequenceVertex to, SequenceVertex inhib)
	   {
		super(parent, totalConcentration, stackSlowdown, dangleL, dangleR, from, to, inhib);
		this.concentrationExtendedProtectedInput = 0;
		this.concentrationWithBothProtectedInput = 0;
		this.concentrationWithBothProtectedOutput = 0;
		this.concentrationWithBothProtectedBoth = 0;
		this.concentrationWithProtectedOutput = 0;
		this.concentrationWithProtecteInput = 0;
		this.from = from;
		this.to = to;
		this.inhib = inhib;
		this.protectedInput = ProtectedSequenceVertex.class.isAssignableFrom(from.getClass());
		if(to!=null){
			this.protectedOutput = ProtectedSequenceVertex.class.isAssignableFrom(to.getClass());
		} else {
			this.protectedOutput = false;
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
		double fromProtec = 0;
		double toProtec = 0;
		if (protectedInput) fromProtec = ((ProtectedSequenceVertex) from).getProtectedConcentration();
		if (protectedOutput) toProtec = ((ProtectedSequenceVertex) to).getProtectedConcentration();
		
		return ((Double)this.parent.K.get(this.inhib)).doubleValue() * truealpha*Constants.Kduplex //Add dangle
				* this.concentrationInhibited
				- Constants.Kduplex
				* conc
				* (this.concentrationAlone + this.concentrationWithInput
						+ this.concentrationWithOutput
						+ this.concentrationWithProtectedOutput + this.concentrationWithProtecteInput)
				+ this.concentrationInhibited*Constants.Kduplex*(Constants.ratioToeholdLeft*from.getConcentration()+Constants.ratioToeholdLeft*fromProtec+Constants.ratioToeholdRight*to.getConcentration()+Constants.ratioToeholdRight*toProtec);
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
		debug = ((Double)this.parent.K.get(from)).doubleValue()*Constants.Kduplex
				* (dangleLSlowdown*this.concentrationWithInput + stackSlowdown/dangleRSlowdown*this.concentrationWithBoth + stackSlowdown/dangleRSlowdown*this.concentrationWithBothProtectedOutput)
				+ kinhib
				* inhib
				* this.concentrationWithInput
				- Constants.Kduplex
				* conc
				* (this.concentrationAlone + this.concentrationWithOutput + this.concentrationWithProtectedOutput + Constants.ratioToeholdLeft*this.concentrationInhibited);
		// System.out.println("Conc: "+conc+" inhib: "+inhib+" kinhib: "+kinhib+" kdup: "+Constants.Kduplex+" Template alone: "+this.concentrationAlone+" w/input: "+this.concentrationWithInput+" w/output: "+this.concentrationWithOutput+" w/both: "+this.concentrationWithBoth);
		return debug;
	}

	public double inputProtectedSequenceFlux() {
		if (this.protectedInput) {
			double conc = ((ProtectedSequenceVertex) from).getProtectedConcentration();
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
			debug = ((Double)this.parent.K.get(from)).doubleValue()*Constants.Kduplex
					* (dangleLSlowdown*this.concentrationWithProtecteInput
							+ stackSlowdown/dangleRSlowdown*this.concentrationWithBothProtectedInput + stackSlowdown/dangleRSlowdown*this.concentrationWithBothProtectedBoth)
					+ kinhib
					* inhib
					* this.concentrationWithProtecteInput
					- Constants.Kduplex
					* conc
					* (this.concentrationAlone + this.concentrationWithOutput + this.concentrationWithProtectedOutput + Constants.ratioToeholdLeft*this.concentrationInhibited);
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
		double pol; //to represent that we are in the double-stranded case.
		// with the default values, it is 1 anyway.
		if (this.parent.isInhibitor(this.to))
		      pol = this.polyboth * Constants.displ;
		    else {
		      pol = this.poly;
		    }
		    pol*=parent.polConc;

		double debug = ((Double)this.parent.K.get(to)).doubleValue()*Constants.Kduplex
				* (dangleRSlowdown*this.concentrationWithOutput + stackSlowdown/dangleLSlowdown*this.concentrationWithBoth + stackSlowdown/dangleLSlowdown*this.concentrationWithBothProtectedInput)
				+ kinhib
				* inhib
				* this.concentrationWithOutput
				- Constants.Kduplex
				* conc
				* (this.concentrationAlone + this.concentrationWithInput + this.concentrationWithProtecteInput + Constants.ratioToeholdRight*this.concentrationInhibited)
				+ pol
				* (this.concentrationWithBoth + this.concentrationWithBothProtectedInput);
		// System.out.println("OutputSequenceFlux: "+debug);
		return debug;
	}

	public double outputProtectedSequenceFlux() {
		if (this.protectedOutput) {
			double conc = ((ProtectedSequenceVertex) to).getProtectedConcentration();
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
			double pol; //to represent that we are in the double-stranded case.
			// with the default values, it is 1 anyway.
			if (this.parent.isInhibitor(this.to))
			      pol = this.polyboth * Constants.displ;
			    else {
			      pol = this.poly;
			    }
			    pol*=parent.polConc;

			double debug = ((Double)this.parent.K.get(to)).doubleValue()*Constants.Kduplex
					* (dangleRSlowdown*this.concentrationWithProtectedOutput
							+ stackSlowdown/dangleLSlowdown*this.concentrationWithBothProtectedOutput + stackSlowdown/dangleLSlowdown*this.concentrationWithBothProtectedBoth)
					+ kinhib
					* inhib
					* this.concentrationWithProtectedOutput
					- Constants.Kduplex
					* conc
					* (this.concentrationAlone + this.concentrationWithInput + this.concentrationWithProtecteInput + Constants.ratioToeholdRight*this.concentrationInhibited)
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
		double concprotectIn = protectedInput?((ProtectedSequenceVertex) from).getProtectedConcentration():0.0;
		double concOut = to.getConcentration();
		double concprotectOut = protectedOutput?((ProtectedSequenceVertex) to).getProtectedConcentration():0.0;
		double concInhib;
		double kreleaseInhib;
		if (this.inhib != null) {
			concInhib = this.inhib.getConcentration();
			kreleaseInhib = ((Double)this.parent.K.get(this.inhib)).doubleValue() * truealpha*Constants.Kduplex;
		} else {
			concInhib = 0;
			kreleaseInhib = 0;
		}

		double debug = ((Double)this.parent.K.get(this.from)).doubleValue()*Constants.Kduplex
				* dangleLSlowdown*(this.concentrationWithInput + this.concentrationWithProtecteInput)
				+ ((Double)this.parent.K.get(this.to)).doubleValue()*Constants.Kduplex
				* dangleRSlowdown*(this.concentrationWithOutput + this.concentrationWithProtectedOutput)
				+ kreleaseInhib
				* this.concentrationInhibited
				- this.concentrationAlone
				* Constants.Kduplex
				* (concIn + concprotectIn + concOut + concprotectOut + concInhib)
				- this.poly*parent.polConc*this.concentrationAlone*Constants.ratioSelfStart; 
		// System.out.println("AloneFlux = "+ debug);
		return debug;
	}

	protected double inputFlux() {
		double concIn = from.getConcentration();
		double concOut = to.getConcentration();
		double concprotectOut = protectedOutput?((ProtectedSequenceVertex) to).getProtectedConcentration():0.0;
		double concInhib;
		if (this.inhib != null) {
			concInhib = this.inhib.getConcentration();
		} else {
			concInhib = 0;
		}
		return Constants.Kduplex
				* concIn
				* (this.concentrationAlone + Constants.ratioToeholdLeft*this.concentrationInhibited )
				+ ((Double)parent.K.get(to)).doubleValue()*Constants.Kduplex
				* stackSlowdown/dangleLSlowdown*(this.concentrationWithBoth + this.concentrationWithBothProtectedOutput)
				- this.concentrationWithInput
				* (((Double)this.parent.K.get(this.from)).doubleValue()*Constants.Kduplex*dangleLSlowdown + this.poly*parent.polConc + Constants.Kduplex
						* (concOut + concprotectOut) + Constants.Kduplex
						* concInhib);
	}

	private double inputProtectedFlux() {
		if (this.protectedInput) {
			double concprotectIn = protectedInput?((ProtectedSequenceVertex) from).getProtectedConcentration():0.0;
			double concOut = to.getConcentration();
			double concprotectOut = protectedOutput?((ProtectedSequenceVertex) to).getProtectedConcentration():0.0;
			double concInhib;
			if (this.inhib != null) {
				concInhib = this.inhib.getConcentration();
			} else {
				concInhib = 0;
			}
			return Constants.Kduplex
					* concprotectIn
					* (this.concentrationAlone + Constants.ratioToeholdLeft*this.concentrationInhibited) 
					+ ((Double)parent.K.get(to)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleLSlowdown
					* (this.concentrationWithBothProtectedInput + this.concentrationWithBothProtectedBoth)
					- this.concentrationWithProtecteInput
					* (((Double)parent.K.get(from)).doubleValue()*Constants.Kduplex*dangleLSlowdown + this.poly*parent.polConc + Constants.Kduplex
							* (concOut + concprotectOut) + Constants.Kduplex
							* concInhib);
		} else {
			return 0;
		}

	}

	protected double outputFlux() {

		double concIn = from.getConcentration();
		double concprotectIn = protectedInput?((ProtectedSequenceVertex) from).getProtectedConcentration():0.0;
		double concOut = to.getConcentration();
		double concInhib;
		if (this.inhib != null) {
			concInhib = this.inhib.getConcentration();
		} else {
			concInhib = 0;
		}
		double debug = concOut
				* Constants.Kduplex
				* (this.concentrationAlone + Constants.ratioToeholdRight*this.concentrationInhibited) 
				+ ((Double)parent.K.get(from)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleRSlowdown
				* (this.concentrationWithBoth + this.concentrationWithBothProtectedInput)
				- this.concentrationWithOutput
				* (((Double)parent.K.get(to)).doubleValue()*Constants.Kduplex*dangleRSlowdown + Constants.Kduplex * (concIn + concprotectIn) + Constants.Kduplex
						* concInhib)+this.poly*parent.polConc*this.concentrationAlone*Constants.ratioSelfStart;
		// System.out.println("OutputFlux =="+debug);
		return debug;
	}

	protected double outputProtectedFlux() {
		if (this.protectedOutput) {
			double concIn = from.getConcentration();
			double concprotectIn = protectedInput?((ProtectedSequenceVertex) from).getProtectedConcentration():0.0;
			double concprotectOut = protectedOutput?((ProtectedSequenceVertex) to).getProtectedConcentration():0.0;
			double concInhib;
			if (this.inhib != null) {
				concInhib = this.inhib.getConcentration();
			} else {
				concInhib = 0;
			}
			double debug = concprotectOut
					* Constants.Kduplex
					* (this.concentrationAlone + Constants.ratioToeholdRight*this.concentrationInhibited)
					+ ((Double)parent.K.get(from)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleRSlowdown
					* (this.concentrationWithBothProtectedBoth + this.concentrationWithBothProtectedOutput)
					- this.concentrationWithProtectedOutput
					* (((Double)parent.K.get(to)).doubleValue()*Constants.Kduplex*dangleRSlowdown + Constants.Kduplex * (concIn + concprotectIn) + Constants.Kduplex
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
		double pol = this.parent.isInhibitor(this.to) ? this.polyboth * Constants.displ : 
		      this.poly;
	    pol*=parent.polConc;
		return Constants.Kduplex * concIn * this.concentrationWithOutput
				+ Constants.Kduplex * concOut * this.concentrationWithInput
				+ this.nick*parent.nickConc * this.concentrationExtended
				- this.concentrationWithBoth * (((Double)parent.K.get(from)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleRSlowdown + ((Double)parent.K.get(to)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleLSlowdown + pol);
	}

	protected double bothProtectedInputFlux() {
		if (this.protectedInput) {
			double concprotectIn = protectedInput?((ProtectedSequenceVertex) from).getProtectedConcentration():0.0;
			double concOut = to.getConcentration();
			double pol = this.parent.isInhibitor(this.to) ? this.polyboth * Constants.displ : 
			      this.poly;
		    pol*=parent.polConc;
			return Constants.Kduplex * concprotectIn
					* this.concentrationWithOutput + Constants.Kduplex
					* concOut * this.concentrationWithProtecteInput
					+ this.nick*parent.nickConc * this.concentrationExtendedProtectedInput
					- this.concentrationWithBothProtectedInput
					* (((Double)parent.K.get(from)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleRSlowdown + ((Double)parent.K.get(to)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleLSlowdown + pol);
		} else {
			return 0;
		}
	}

	protected double bothProtectedOutputFlux() {
		if (this.protectedOutput) {
			double concIn = from.getConcentration();
			double concprotectOut = protectedOutput?((ProtectedSequenceVertex) to).getProtectedConcentration():0.0;
			double pol = this.parent.isInhibitor(this.to) ? this.polyboth * Constants.displ : 
			      this.poly;
		    pol*=parent.polConc;
			return Constants.Kduplex * concIn
					* this.concentrationWithProtectedOutput + Constants.Kduplex
					* concprotectOut * this.concentrationWithInput
					- this.concentrationWithBothProtectedOutput
					* (((Double)parent.K.get(from)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleRSlowdown + ((Double)parent.K.get(to)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleLSlowdown + pol);
		} else {
			return 0;
		}
	}

	protected double bothProtectedBothFlux() {
		if (this.protectedInput && this.protectedOutput) {
			double concprotectIn = protectedInput?((ProtectedSequenceVertex) from).getProtectedConcentration():0.0;
			double concprotectOut = protectedOutput?((ProtectedSequenceVertex) to).getProtectedConcentration():0.0;
			double pol = this.parent.isInhibitor(this.to) ? this.polyboth * Constants.displ : 
			      this.poly;
		    pol*=parent.polConc;
			return Constants.Kduplex * concprotectIn
					* this.concentrationWithProtectedOutput + Constants.Kduplex
					* concprotectOut * this.concentrationWithProtecteInput
					- this.concentrationWithBothProtectedBoth
					* (((Double)parent.K.get(from)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleRSlowdown + ((Double)parent.K.get(to)).doubleValue()*Constants.Kduplex*stackSlowdown/dangleLSlowdown + pol);
		} else {
			return 0;
		}
	}

	protected double extendedFlux() {
		double poldispl = this.parent.isInhibitor(this.to) ? this.polyboth * Constants.displ : 
		      this.poly;
	    poldispl*=parent.polConc;
		return this.poly //because single stranded after the input
				* this.concentrationWithInput
				+ poldispl
				* (this.concentrationWithBoth + this.concentrationWithBothProtectedOutput)
				- this.nick*parent.nickConc * this.concentrationExtended;
	}

	protected double extendedProtectedInputFlux() {
		if (this.protectedInput) {
			double poldispl = this.parent.isInhibitor(this.to) ? this.polyboth * Constants.displ : 
			      this.poly;
		    poldispl*=parent.polConc;
			return this.poly
					* this.concentrationWithProtecteInput
					+ poldispl
					* (this.concentrationWithBothProtectedInput + this.concentrationWithBothProtectedBoth)
					- this.nick*parent.nickConc * this.concentrationExtendedProtectedInput;
		} else {
			return 0;
		}
	}

	protected double inhibitedFlux() {
		double conc, krelease;
		if (this.inhib != null) {
			conc = this.inhib.getConcentration();
			krelease =((Double)parent.K.get(inhib)).doubleValue()*truealpha*Constants.Kduplex;
		} else {
			conc = 0;
			krelease = 0;
		}
		double totalInput = from.getConcentration();
		double totalOutput = to.getConcentration();
		if(protectedInput) totalInput += ((ProtectedSequenceVertex) from).getProtectedConcentration();
		if(protectedOutput) totalOutput += ((ProtectedSequenceVertex) to).getProtectedConcentration();

		return -krelease
				* this.concentrationInhibited
				+ Constants.Kduplex
				* conc
				* (this.concentrationAlone + this.concentrationWithInput
						+ this.concentrationWithOutput
						+ this.concentrationWithProtecteInput + this.concentrationWithProtectedOutput)
				- this.concentrationInhibited*Constants.Kduplex*(Constants.ratioToeholdLeft*totalInput+Constants.ratioToeholdRight*totalOutput);
	}

	/**
	 * 
	 * @return [template+In]/Km,in + [template+in+out]/Km,both
	 */
	@Override
	public double getPolyUsage(){
		return (this.concentrationWithInput+this.concentrationWithProtecteInput)/Constants.polKm+(this.concentrationWithBoth+this.concentrationWithBothProtectedInput+this.concentrationWithBothProtectedOutput+this.concentrationWithBothProtectedBoth)/Constants.polKmBoth;
	}
	
	
	@Override
	public double getNickUsage(){
		return (this.concentrationExtended+this.concentrationExtendedProtectedInput)/Constants.nickKm
				+(this.concentrationWithInput+this.concentrationWithProtecteInput+this.concentrationWithBoth+this.concentrationWithBothProtectedBoth+this.concentrationWithBothProtectedInput+this.concentrationWithBothProtectedOutput)/Constants.nickKmBoth;
	}
	
	@Override
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
