package use.processing.targets.misc;

import java.io.Serializable;

import use.processing.bead.Bead;

public class BeadLineTarget implements BeadPositionTarget, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected boolean vertical = true; //if not, then horizontal
	protected double pos;
	
	public BeadLineTarget(double pos){
		this.pos = pos;
	}
	
	public BeadLineTarget(double pos, boolean vertical){
		this.vertical = vertical;
		this.pos = pos;
	}

	@Override
	public double distance(Bead b) {
		double val;
		if(vertical){
			val = b.getX();
		} else {
			val = b.getY();
		}
		return Math.sqrt((pos-val)*(pos-val));
	}

}
