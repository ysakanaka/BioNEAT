package use.processing.rd;

import java.util.Map.Entry;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import erne.AbstractFitnessResult;
import infotheo.MutualInformation;
import use.processing.bead.Bead;

public class RDAgencyFitnessResult extends RDPatternFitnessResult {

	public static double log2 = Math.log(2);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected float[][][] conc;
	transient protected float[][][][] concHistory;
	// protected Table<Integer,Integer,ArrayList<Bead>> beads;//index of the
	// first template species, indicating bead positions;
	protected double fitness;

	transient static protected AtomicInteger nextFileName = new AtomicInteger();

	protected RDAgencyFitnessResult() {
		fitness = 0.0;
	}

	public RDAgencyFitnessResult(float[][][] conc, float[][][][] concHistory) {
		// this.concGlue = conc[RDConstants.glueIndex];
		this.conc = new float[Math.min(conc.length, RDConstants.glueIndex + 3)][][];
		this.concHistory = concHistory;
		for (int i = 0; i < this.conc.length; i++)
			this.conc[i] = conc[i];

		// TODO calculate happily fitness
		// this.beads = beads;
		fitness = 0;
		double[] concPrev = new double[concHistory[0].length * concHistory[0][0].length * concHistory[0][0][0].length];
		double[] concNow = new double[concHistory[0].length * concHistory[0][0].length * concHistory[0][0][0].length];
		for (int i = 0; i < concHistory.length - 1; i++) // timestep
		{
			for (int j = 0; j < concHistory[0].length 
					; j++) // species
				for (int x = 0; x < concHistory[0][0].length; x++)
					for (int y = 0; y < concHistory[0][0][0].length; y++) 
					{
						// fitness += Math.pow(concHistory[i + 1][j][x][y] -
						// concHistory[i][j][x][y], 2); //test
						concPrev[j * x * y] = concHistory[i][j][x][y];
						concNow[j * x * y] = concHistory[i + 1][j][x][y];
					}
			fitness += calculateMI(concPrev, concNow); // At+1, At / At+1, Et.
														// vary A over the space
			// Math.pow( // , 2)
			// ;
		}
		fitness = Math.max(0.0, fitness);
		fitness = fitness/ //concHistory[0].length/
				concHistory[0][0].length/concHistory[0][0][0].length;

		int myName = nextFileName.getAndAdd(1);
		for (int i = 0; i < concHistory.length; i++) {
			System.err.println(concHistory[i][1][3][1]+" "+concHistory[i][0][2][3]+" "+concHistory[i][0][1][5]); //TODO fix: why is concHistory the same for all values of i -- je dois etre trop fatigue la, je vois pas
			RDImagePrinter ip = new RDImagePrinter(concHistory[i]);
			BufferedImage bi = new BufferedImage(concHistory[0][0].length, concHistory[0][0][0].length,
					BufferedImage.TYPE_INT_ARGB); // ip.getSize().width,
													// ip.getSize().height,
			Graphics g = bi.createGraphics();
			ip.paint(g); // this == JComponent
			// g.dispose();
			try {
				//ImageIO.write(bi, "png", new File("image-" + myName + "-" + i + ".png"));
				ImageIO.write(bi, "jpg", new File("samefileallthetime.jpg"));
			} catch (Exception e) {
				System.err.println("can't write in file, my apologies");
			}
			g.dispose();
		}

	}

	/**
	 * For child classes, skipping fitness evaluation, just setting params.
	 * 
	 * @param conc
	 * @param pattern
	 * @param beads
	 */
	protected void init(float[][][] conc) {
		this.conc = new float[Math.min(conc.length, RDConstants.glueIndex + 3)][][];
		for (int i = 0; i < this.conc.length; i++)
			this.conc[i] = conc[i];
	}

	@Override
	public double getFitness() {
		return fitness;
	}

	public float[][][] getConc() {
		return conc;
	}

	public static RDAgencyFitnessResult getMinFitness() {
		return new RDAgencyFitnessResult();
	}
	
	/***
	 * Calculate mutual information between variables t and a with equal
	 * lengths. Regular version using JavaMI.
	 * 
	 * @param a
	 *            candidate variable a
	 * @param t
	 *            target variable
	 * @return
	 */
	static double calculateMI(double[] a, double[] t) {
		return MutualInformation.calculateMutualInformation(a, t);
		//return 0.0; //TODO
	}

	/***
	 * Calculate mutual information between variables t and a with equal
	 * lengths. Discrete version, where t and a take a finite number of values.
	 * 
	 * @param a
	 *            candidate variable a
	 * @param avals
	 *            number of values a can take (max(a) == avals)
	 * @param t
	 *            target variable
	 * @param tvals
	 *            number of values a can take (max(t) == tvals)
	 * @return
	 */
	static double calculateMIdiscrete(int[] a, int avals, int[] t, int tvals) {
		double numinst = a.length;
		double oneovernuminst = 1 / numinst;
		double sum = 0;

		// longs are required here because of big multiples in calculation
		long[][] crosscounts = new long[avals][tvals];
		long[] tcounts = new long[tvals];
		long[] acounts = new long[avals];
		// Compute counts for the two variables
		for (int i = 0; i < a.length; i++) {
			int av = a[i];
			int tv = t[i];
			acounts[av]++;
			tcounts[tv]++;
			crosscounts[av][tv]++;
		}

		for (int tv = 0; tv < tvals; tv++) {
			for (int av = 0; av < avals; av++) {
				if (crosscounts[av][tv] != 0) {
					// Main fraction: (n|x,y|)/(|x||y|)
					double sumtmp = (numinst * crosscounts[av][tv]) / (acounts[av] * tcounts[tv]);
					// Log bit (|x,y|/n) and update product
					sum += oneovernuminst * crosscounts[av][tv] * Math.log(sumtmp) * log2;
				}
			}
		}

		return sum;
	}

}
