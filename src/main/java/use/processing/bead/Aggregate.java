package use.processing.bead;
import java.util.ArrayList;

import use.processing.rd.RDConstants;
import use.processing.rd.RDSystem;

public class Aggregate {

  ArrayList<Bead> beads = new ArrayList<Bead>();
  RDSystem system;
  
  public Aggregate(RDSystem system){
	  this.system = system;
  }
  
  double getSize(){
    return Math.sqrt(beads.size());
  }
  
  public void update(){
  if(beads.size() == 0){
	  system.aggregates.remove(this);
    //System.out.println("Removed");
  }
  float factor = (float) (2 * RDConstants.beadScale*RDConstants.timePerStep / 2);
  float x = (float) (Math.sqrt(factor)/((float) beads.size()) * Bead.rand.nextGaussian());
  float y = (float) (Math.sqrt(factor)/((float) beads.size()) * Bead.rand.nextGaussian());
    for(Bead b : beads){
      b.update(x,y);
    }
  }
  
}