package model;

import java.io.Serializable;

public class Input implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public  Sequence seq = null;
	public int receivingSeq;
	public double ammount;
	public int timeStep;

	public Input(int receivingSeq, double ammount, int timeStep) {
		this.receivingSeq = receivingSeq;
		this.ammount = ammount;
		this.timeStep = timeStep;
	}
	
	public Input(int receivingSeq, Sequence seq, double ammount, int timeStep) {
		this.receivingSeq = receivingSeq;
		this.ammount = ammount;
		this.timeStep = timeStep;
		this.seq = seq;
	}
}
