package use.processing.rd;

import java.util.ArrayList;

import com.google.common.collect.HashBasedTable;

import use.processing.bead.Bead;
import use.processing.targets.BeadPositionTarget;

public class RDBeadPositionFitnessResult extends RDPatternFitnessResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected BeadPositionTarget target;
	protected ArrayList<Bead> beadslist;
	protected boolean min = false;
	
	public static final RDBeadPositionFitnessResult minFitness = new RDBeadPositionFitnessResult(true);
	
	protected RDBeadPositionFitnessResult(boolean min){
		super(new float[1][1][1], new boolean[1][1],HashBasedTable.<Integer,Integer,ArrayList<Bead>>create(),0.0);
		this.min = min;
	}
			
	public RDBeadPositionFitnessResult(float[][][] conc, boolean[][] pattern, HashBasedTable<Integer,Integer,ArrayList<Bead>> beadSpots, double randomFit, BeadPositionTarget target, ArrayList<Bead> beads){
		super(conc,pattern,beadSpots); //Mostly for display
		this.target = target;
		this.beadslist = beads; //TODO: I can improve that, now...
	}

	@Override
	public double getFitness() {
		if(min) return 0.0;
		
		double total = 0.0;
		for(Bead b : beadslist) total += target.distance(b);
		return 1.0/(1.0+total/(double) RDConstants.maxBeads);
	}

}
