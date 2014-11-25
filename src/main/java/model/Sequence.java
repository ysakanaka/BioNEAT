package model;

import java.io.Serializable;

public abstract class Sequence implements Serializable {

	private final double K; // Kinetic parameter. Dissociation constants
	protected final double initialConcentration;
	protected double concentration;
	public boolean fakeProtected = false;
	public String seq = "";
	
	Sequence(double initialConcentration, double K, double min, double max)
			throws InvalidKException, InvalidConcentrationException {
		if (initialConcentration < 0) {
			throw new InvalidConcentrationException();
		}
		this.initialConcentration = initialConcentration;
		this.setConcentration(initialConcentration);
		if (K < min) {
			K = min;
		}
		if (K > max) {
			K = max;
		}
		if (K < min || K > max) {
			System.out.println("");
			throw new InvalidKException(K);
		}
		this.K = K;
	}

	public double getK() {
		return K;
	}

	public double getInitialConcentration() {
		return initialConcentration;
	}

	public void setConcentration(double concentration)
			throws InvalidConcentrationException {
		if (concentration < 0) {
			// System.err.println("Reality hits you hard, bro."+(this.getClass()
			// == SimpleSequence.class?
			// "Simple":"Inhibiting")+"Sequence "+getID());
			this.concentration = concentration;
			throw new InvalidConcentrationException();
		}
		this.concentration = concentration;
	}

	public double getConcentration() {
		return concentration;
	}

	public double getProtectedConcentration() {
		return 0;
	}

	public abstract double getKmin();

	public abstract double getKmax();

	public abstract int getID();

	public abstract String toString();

	public boolean isProtected() {
		return false;
	}

	public void setProtectedConcentration(double conc) {

	}
	
	public abstract double getExoSaturation();

	public void resetConcentration() {
		this.concentration = this.initialConcentration;
	}

}
