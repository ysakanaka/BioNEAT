package model;

import java.io.Serializable;

public class Enzyme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3438778039400221205L;
	public String name;
	public double activity;
	public double basicKm;
	public double optionalValue = 0; //So far, enzymes have at most three different values, albeit having different meanings
	
	public Enzyme(String name, double activity, double basicKm){ // for the nicking enzyme
		this.name = name;
		this.activity = activity;
		this.basicKm = basicKm;
	}
	
	public Enzyme(String name, double activity, double basicKm, double optionalValue){
		this.name = name;
		this.activity = activity;
		this.basicKm = basicKm;
		this.optionalValue = optionalValue;
	}
}
