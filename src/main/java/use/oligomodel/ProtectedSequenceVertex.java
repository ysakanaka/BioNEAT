package use.oligomodel;

import model.chemicals.SequenceVertex;

public class ProtectedSequenceVertex extends SequenceVertex {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean protectedSequence = true;
	public double unprotected = 0;
	
	public ProtectedSequenceVertex(Integer i, double init) {
		super(i, init);
		
	}

	public double getProtectedConcentration(){
		return Math.max(this.concentration - unprotected,0);
	}
	

}
