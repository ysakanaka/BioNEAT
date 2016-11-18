package use.processing.parallel;
import java.util.ArrayList;

import use.processing.bead.Aggregate;
import use.processing.bead.Bead;

public class BeadUpdateMechanism extends GenericThreadComputation<Boolean>{
  
  //data to treat
  ArrayList<Aggregate> aggrs;
  ArrayList<Bead> beads;
  public float[][][] concTemp; //shared object
  
  public BeadUpdateMechanism(int total, int id, float[][][] _concTemp){
    super(total,id);
    concTemp = _concTemp;
  }
  
  public void setTargets(ArrayList<Aggregate> a, ArrayList<Bead> b){
  aggrs = a;
  beads = b;
  }
  
  public Boolean call(){
	  System.err.println("Warning, "+this.getClass()+" deprecated class. No effect.");
//  for(Aggregate aggr : aggrs) aggr.update();
//    for(Bead bead : beads) {
//      bead.update(.conc);
//      
//    }
    return new Boolean(true);
  }
  
}