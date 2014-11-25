package model;

import java.io.Serializable;

public class SimpleSequence extends Sequence implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4652075511900819680L;

	public final int ID;
	public boolean protectedSequence;
	public double unprotected = 0;
	public double exoKm = Constants.exoKmSimple; //Note: -1 means no saturation
	public double exoKmProtected = Constants.exoKmSimple;

	public SimpleSequence(int ID, double initialConcentration, double K,
			boolean protec) throws InvalidKException,
			InvalidConcentrationException {
		super(initialConcentration, K, Constants.simpleKmin,
				Constants.simpleKmax);
		this.ID = ID;
		this.protectedSequence = protec;
	}

	public SimpleSequence(int ID, double initialConcentration, double K)
			throws InvalidKException, InvalidConcentrationException {
		this(ID, initialConcentration, K, false);
	}

	public SimpleSequence(int ID, double initialConcentration, double K,
			String seq) throws InvalidKException, InvalidConcentrationException {
		this(ID, initialConcentration, K, false);
		this.seq = seq;
	}

	public void setSeqString(String str) {
		this.seq = seq;
	}

	public double getKmin() {
		return Constants.simpleKmin;
	}

	public double getKmax() {
		return Constants.simpleKmax;
	}

	public int getID() {
		return ID;
	}

	public String toString() {
		return "SimpleSequence " + ID;
	}

	public double getConcentration() {
		if (protectedSequence) {
			return unprotected;
		}
		return concentration;

	}

	public double getProtectedConcentration() {
		if (protectedSequence) {
			return concentration;
		}
		return 0;
	}

	public boolean isProtected() {
		return protectedSequence;
	}

	public void setProtectedConcentration(double conc) {
		if (protectedSequence) {
			this.concentration = conc;
		}
	}

	public void setConcentration(double conc) {
		if (protectedSequence) {
			this.unprotected = conc;
		} else {
			this.concentration = conc;
		}
	}

	public void resetConcentration() {
		super.resetConcentration();
		if (protectedSequence) {
			this.unprotected = 0;
		}
	}
	
	public double getExoSaturation(){
		double value = 0;
		if(exoKm != -1){
			value += (protectedSequence?unprotected:concentration)/exoKm;
		}
		if(exoKmProtected != -1){
			value += (protectedSequence?concentration/exoKmProtected:0);
		}
		return value;
	}

}
