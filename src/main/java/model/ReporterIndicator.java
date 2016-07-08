package model;

import model.chemicals.SequenceVertex;

/**
 * Used to indicate that the target of the template is
 * not a real species
 * @author naubert
 *
 */
public class ReporterIndicator extends SequenceVertex {

	public static final ReporterIndicator indicator = new ReporterIndicator(0);
	public static final double reporterConcentration = 200.0;
	
	public ReporterIndicator(Integer i) {
		super(i);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
