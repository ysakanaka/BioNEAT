package use.processing.multiobjective;

import java.io.Serializable;
import use.processing.rd.RDSystem;

public abstract class RDObjective implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5501550572733354783L;
	
	
	public final String name;

	public RDObjective(){
		this.name = this.getClass().getName();
	}
	
	
	public abstract double evaluateScore(RDSystem system, boolean[][] pattern, boolean[][] position);

}
