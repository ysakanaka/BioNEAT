package use.processing.rd;

import java.util.ArrayList;

import use.processing.bead.Bead;
import use.processing.targets.BeadPositionTarget;

public class RDBeadPositionFitnessResult extends RDPatternFitnessResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected BeadPositionTarget target;
	protected ArrayList<Bead> beads;
	protected boolean min = false;
	
	public static final RDBeadPositionFitnessResult minFitness = new RDBeadPositionFitnessResult(true);
	
	protected RDBeadPositionFitnessResult(boolean min){
		super(new float[1][1][1], new boolean[1][1],0,0.0);
		this.min = min;
	}
			
	public RDBeadPositionFitnessResult(float[][][] conc, boolean[][] pattern, int beadIndex, double randomFit, BeadPositionTarget target, ArrayList<Bead> beads){
		super(conc,pattern,beadIndex,randomFit); //Mostly for display
		this.target = target;
		this.beads = beads;
	}

	@Override
	public double getFitness() {
		if(min) return 0.0;
		
		double total = 0.0;
		for(Bead b : beads) total += target.distance(b);
		return 1.0/(1.0+total/(double) RDConstants.maxBeads);
	}

}
