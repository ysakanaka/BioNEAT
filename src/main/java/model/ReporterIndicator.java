package model;

import model.chemicals.SequenceVertex;

/**
 * Used to indicate that the target of the template is
 * not a real species
 * @author dextra
 *
 */
public class ReporterIndicator extends SequenceVertex {

	public static final ReporterIndicator indicator = new ReporterIndicator(0);
	public static final double reporterConcentration = 200.0;
	
	public ReporterIndicator(Integer i) {
		super(i);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
