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
		
		// pick agent that maximizes IF over a set of generated agents
		//int agentSearchSpace = 100;
		//int asscnt = 0;
		//while (asscnt < agentSearchSpace)
		fitness = 0;
		int sizeAE = concHistory[0].length * concHistory[0][0].length * concHistory[0][0][0].length; // 3*80*80 world size
		int sizeA = concHistory[0].length * concHistory[0][0].length * concHistory[0][0][0].length/5/5; // 3*80*80/25 = 3*16*16 agent size
		double maxFitness = 0;
		//int agent_jump = 5;
		for (int ax = 0; ax < sizeAE - 5; ax = ax + 5) { 
		for (int ay = 0; ay < sizeAE - 5; ay = ay + 5) {
			//asscnt++;
			double localFitness = 0;
			//System.err.println(concHistory[0].length+" "+concHistory[0][0].length+" "+concHistory[0][0][0].length+" ");
			double[] concPrev = new double[sizeAE]; //80 side
			double[] concNow = new double[sizeAE];
			//System.err.println(sizeAE);
			//System.err.println(sizeA);
			double[] concPrevA = new double[sizeA]; //5 side
			double[] concNowA = new double[sizeA];
			//int sizeE = concHistory[0].length * concHistory[0][0].length * concHistory[0][0][0].length/5/5;
			//System.err.println(sizeE);
			double[] concPrevE = new double[sizeAE-sizeA]; //75 side
			double[] concNowE = new double[sizeAE-sizeA];
			
			for (int i = 0; i < concHistory.length - 1; i++) // timestep //TODO -1 not necessary
			{
				//whole world history (A+E)
				for (int j = 0; j < concHistory[0].length; j++) // species
					for (int x = 0; x < concHistory[0][0].length; x++)
						for (int y = 0; y < concHistory[0][0][0].length; y++) 
						{
							// fitness += Math.pow(concHistory[i + 1][j][x][y] -
							// concHistory[i][j][x][y], 2); //test
							concPrev[j * x * y] = concHistory[i][j][x][y];
							concNow[j * x * y] = concHistory[i + 1][j][x][y];
						}
				
				//A
				for (int j = 0; j < concHistory[0].length; j++) {
					int cx = 0;
					for (int x = ax; x < concHistory[0][0].length && x < ax + 5 ; x++) {
						int cy = 0;
						for (int y = ay; y < concHistory[0][0][0].length && y < ay + 5 ; y++) {
							try {
								concPrevA[j * cx * cy] = concHistory[i][j][x][y];
								concNowA[j * cx * cy] = concHistory[i + 1][j][x][y];
							} catch (Exception e) {
								System.out.println("concA: "+i+" "+j+" "+x+" "+y+" ");
								e.printStackTrace();
							}
							cy++;
						}
						cx++;
					}
				}
				
				//E
				for (int j = 0; j < concHistory[0].length; j++) {
					int cx = 0;
					int x = ax;
					while (x < concHistory[0][0].length) {
						int cy = 0;
						int y = ay;
						while (y < concHistory[0][0][0].length) {
							concPrevE[j * cx * cy] = concHistory[i][j][x][y];
							concNowE[j * cx * cy] = concHistory[i + 1][j][x][y];
							cy++;
							while(ay <= y && y < ay+sizeA) y++;
						}
						cx++;
						while(ax <= x && x < ax+sizeA) x++;
					}
				}
				
				double fitInc = calculateMI(concPrev, concNow) - calculateMI(concPrevA, concNowA) - calculateMI(concPrevE, concNowE);
				localFitness += fitInc;
			}
			localFitness = Math.max(0.0, localFitness);
			
			localFitness = localFitness/ //concHistory[0].length/
					concHistory[0][0].length/concHistory[0][0][0].length;
			if (maxFitness < localFitness) {
				System.out.println("\t new fitness = "+fitness+", old fitness = "+maxFitness);
				maxFitness = localFitness;
			}
		}
		}
		fitness = maxFitness;
		System.err.println("fitness = "+fitness);
		// now paint it into a file

		//float[][][] conc = new float[5][200][200];
		//float[][][][] concHistory = new float[1][][][];
		
//		for (int i = 0; i< conc.length; i++){
//			for (int j = 0; j<conc[i].length; j++){
//				for(int k = 0; k<conc[i][j].length; k++){
//					conc[i][j][k] = i*j*k/1000.0f;
//				}
//			}
//		}
		concHistory[0] = conc;
		String myName = "rdfit"; 
		int mySuffix = nextFileName.getAndAdd(1);
		for(int i= 0; i<concHistory.length; i++) {
			RDImagePrinter ip = new RDImagePrinter(concHistory[i]);
			BufferedImage bi = new BufferedImage((int) (conc[0].length* RDConstants.spaceStep),(int) (conc[0][0].length* RDConstants.spaceStep), BufferedImage.TYPE_INT_RGB); 
			Graphics g = bi.createGraphics();
			ip.paintComponent(g);
			
			try {
				ImageIO.write(bi,"png",new File("image-"+myName+"-"+mySuffix+"-"+i+".png"));
				} catch (Exception e) {}
			g.dispose();
		}		
		//int myName = nextFileName.getAndAdd(1);
		
// just previous code: 
//		for (int i = 0; i < concHistory.length; i++) {
//			System.err.println(concHistory[i][1][3][1]+" "+concHistory[i][0][2][3]+" "+concHistory[i][0][1][5]); //TODO fix: why is concHistory the same for all values of i -- je dois etre trop fatigue la, je vois pas
//			RDImagePrinter ip = new RDImagePrinter(concHistory[i]);
//			BufferedImage bi = new BufferedImage(concHistory[0][0].length, concHistory[0][0][0].length,
//					BufferedImage.TYPE_INT_ARGB); // ip.getSize().width,
//													// ip.getSize().height,
//			Graphics g = bi.createGraphics();
//			ip.paint(g); // this == JComponent
//			// g.dispose();
//			try {
//				//ImageIO.write(bi, "png", new File("image-" + myName + "-" + i + ".png"));
//				ImageIO.write(bi, "jpg", new File("samefileallthetime.jpg"));
//			} catch (Exception e) {
//				System.err.println("can't write in file, my apologies");
//			}
//			g.dispose();
//		}

// a while ago code:
//		int myName = nextFileName.getAndAdd(1);
//		for(int i= 0; i<concHistory.length; i++){
//		RDImagePrinter ip = new RDImagePrinter(concHistory[i]);
//		BufferedImage bi = new BufferedImage((int) (conc[0].length* RDConstants.spaceStep),(int) (conc[0][0].length* RDConstants.spaceStep), BufferedImage.TYPE_INT_ARGB); 
//		Graphics g = bi.createGraphics();
//		ip.paintComponent(g);  //this == JComponent
//		
//		try{ImageIO.write(bi,"png",new File("image-"+myName+"-"+i+".png"));}catch (Exception e) {}
//		g.dispose();
//		}
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

	public static void main2 (String[] args){
		float[][][] conc = new float[5][200][200];
		float[][][][] concHistory = new float[1][][][];
		
		for (int i = 0; i< conc.length; i++){
			for (int j = 0; j<conc[i].length; j++){
				for(int k = 0; k<conc[i][j].length; k++){
					conc[i][j][k] = i*j*k/1000.0f;
				}
			}
		}
		concHistory[0] = conc;
		String myName = "mimimi"; 
		for(int i= 0; i<concHistory.length; i++){
			RDImagePrinter ip = new RDImagePrinter(concHistory[i]);
			BufferedImage bi = new BufferedImage((int) (conc[0].length* RDConstants.spaceStep),(int) (conc[0][0].length* RDConstants.spaceStep), BufferedImage.TYPE_INT_RGB); 
			Graphics g = bi.createGraphics();
			ip.paintComponent(g);
			
			try{ImageIO.write(bi,"png",new File("image-"+myName+"-"+i+".png"));}catch (Exception e) {}
			g.dispose();
			}
	}

}
