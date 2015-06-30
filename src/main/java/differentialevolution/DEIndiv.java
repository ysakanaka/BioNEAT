package differentialevolution;

import java.util.ArrayList;
import java.util.Random;

import reactionnetwork.ReactionNetwork;

public class DEIndiv {

	protected int dim;
	protected double[] parameters;
	protected double fitnessScore;
	protected double crossRate;
	protected double ampFact;

	protected ReactionNetwork network;

	public DEIndiv(int dim, ArrayList<Double> min, ArrayList<Double> max, Random rnd) {
		this.dim = dim;
		this.parameters = new double[dim];
		for (int i = 0; i < dim; ++i) {
			this.parameters[i] = (rnd.nextDouble() * (max.get(i).doubleValue() - min.get(i).doubleValue()) + min.get(i).doubleValue());
		}
		// this.crossRate = rnd.nextDouble() * (1.0 - 0.0) + 0.0;
		// this.ampFact = rnd.nextDouble() * (1.0 - 0.1) + 0.1;

		this.crossRate = 0.9;
		this.ampFact = 0.9;

	}

	public DEIndiv(double[] param, Random rnd) {
		this.dim = param.length;
		this.parameters = new double[param.length];
		for (int i = 0; i < param.length; ++i) {
			this.parameters[i] = param[i];
		}
		// this.crossRate = rnd.nextDouble() * (1.0 - 0.0) + 0.0;
		// this.ampFact = rnd.nextDouble() * (1.0 - 0.1) + 0.1;
		this.crossRate = 0.9;
		this.ampFact = 0.9;
	}

	public DEIndiv(double[] param, double cross, double amp) {
		this.dim = param.length;
		this.parameters = new double[param.length];
		for (int i = 0; i < param.length; ++i) {
			this.parameters[i] = param[i];
		}
		// this.crossRate = cross;
		// this.ampFact = amp;
		this.crossRate = 0.9;
		this.ampFact = 0.9;

	}

	public DEIndiv breed(DEIndiv a, DEIndiv b, DEIndiv c, Random rnd, ArrayList<Double> min, ArrayList<Double> max) {
		double[] tmp = this.getParameters();
		double[] parX = new double[tmp.length];
		System.arraycopy(tmp, 0, parX, 0, tmp.length);
		double[] parA = a.getParameters();
		double[] parB = b.getParameters();
		double[] parC = c.getParameters();

		double F;
		double Cr;

		// Self-adaptation parameter
		// double Tau1 =0.1, Tau2=0.1;
		// double F_low = 0.1, F_High=0.9;
		//
		// if (rnd.nextDouble() < Tau1){
		// F = F_low + F_High * rnd.nextDouble();
		// }
		// else{
		// F = this.ampFact;
		// }
		//
		// if (rnd.nextDouble() < Tau2){
		// Cr = rnd.nextDouble();
		// }
		// else{
		// Cr = this.crossRate;
		// }
		F = 0.9;
		Cr = 0.9;

		int index = (int) (rnd.nextDouble() * this.dim);
		int counter = 0;

		while (counter++ < this.dim) {
			if ((rnd.nextDouble() < Cr) || (counter == (this.dim - 1)))
				parX[index] = parA[index] + F * (parB[index] - parC[index]);
			index = (++index % this.dim);
		}

		// Correct for boundary violation
		for (int i = 0; i < this.dim; ++i) {
			if (parX[i] < min.get(i).doubleValue()) {
				parX[i] = 2 * min.get(i).doubleValue() - parX[i];
			}
			if (parX[i] > max.get(i).doubleValue()) {
				parX[i] = 2 * max.get(i).doubleValue() - parX[i];
			}
		}

		return (new DEIndiv(parX, Cr, F));
	}

	public void setFitnessScore(double scr) {
		this.fitnessScore = scr;
	}

	public double getFitnessScore() {
		return (this.fitnessScore);
	}

	public void setCrossRate(double cr) {
		this.crossRate = cr;
	}

	public double getCrossRate() {
		return (this.crossRate);
	}

	public void setAmpFactor(double af) {
		this.ampFact = af;
	}

	public double getAmpFactor() {
		return (this.ampFact);
	}

	public double[] getParameters() {
		return (this.parameters);
	}

	public double getParAt(int i) {
		return (this.parameters[i]);
	}

	public ReactionNetwork getReactionNetwork() {
		return (this.network);
	}

	public void setReactionNetwork(ReactionNetwork network) {
		this.network = network;
	}

}
