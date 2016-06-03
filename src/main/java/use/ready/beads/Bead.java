package use.ready.beads;

import utils.MyPair;

/**
 * Just a convenience data structure to hold data for reaction-diffusion systems.
 * @author naubertkato
 *
 */
public class Bead {
	
	public double x;
	public double y;
	public double radius;
	/**
	 * The chemical species stuck on the bead.
	 */
	public String[] species; 
	public double[] conc;
	
	/**
	 * Realistic implementation
	 * @param x
	 * @param y
	 * @param radius
	 * @param species
	 */
	public Bead(double x, double y, double radius, MyPair<String, Double>[] species){
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.species = new String[species.length];
		this.conc = new double[species.length];
		for (int i=0; i< species.length; i++){
			try{
			this.species[i] = species[i].getLeft();
			this.conc[i] = species[i].getRight().doubleValue();
			} catch(NullPointerException e){
				System.out.println(species.length);
				throw e;
			}
		}
	}
	
	/**
	 * For convenience/ quick tests
	 * @param x
	 * @param y
	 * @param radius
	 * @param species
	 * @param conc
	 */
	public Bead(double x, double y, double radius, String species, double conc){
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.species = new String[]{species};
		this.conc = new double[]{conc};
	}

}
