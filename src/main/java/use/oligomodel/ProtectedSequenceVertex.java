package use.oligomodel;

import model.chemicals.SequenceVertex;
import model.input.AbstractInput;

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
		return Math.max(this.concentration,0);
	}
	
	public void setProtectedConcentration(double value){
		this.concentration = value; // Does that make sense?
	}
	
	@Override
	public double getConcentration(){
		return unprotected;
	}
	
	@Override
	public void setConcentration(double value){
		unprotected = value;
	}
	
	@Override
	public void reset(){
		this.concentration = this.initialConcentration;
		this.unprotected = 0;
		for(AbstractInput ai : this.inputs){
			ai.reset();
		}
	}
	
	@Override
	public boolean equals(Object o){
		if(ProtectedSequenceVertex.class.isAssignableFrom(o.getClass())){
			return ((ProtectedSequenceVertex)o).ID == this.ID;
		} 
		return false;
	}

}
