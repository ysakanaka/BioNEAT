package model;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class OligoSystem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4851790265713984176L;
	public ArrayList<ArrayList<Sequence>> sequences;
	public ArrayList<ArrayList<ArrayList<Template>>> templates;
	public Enzyme exo;
	public Enzyme poly = new Enzyme("pol",Constants.polVm/Constants.polKm,Constants.polKm,Constants.polKmBoth);
	public Enzyme nick = new Enzyme("nick",Constants.nickVm/Constants.nickKm,Constants.nickKm);
	public double[][] seqK;
	public double[][] inhK;
	public double[][] seqConcentration;
	public double[][][] template;
	public int[][] params;

	public abstract void setTotals(int total, int inhTotal);

	public abstract void reinitializeOiligoSystem();

}
