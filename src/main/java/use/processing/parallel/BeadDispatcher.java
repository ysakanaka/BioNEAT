package use.processing.parallel;

import java.util.ArrayList;

import use.processing.bead.Aggregate;
import use.processing.bead.Bead;
import use.processing.rd.RDSystem;
import use.processing.rd.RDConstants;

public class BeadDispatcher extends Dispatcher<Boolean> {
	  
	  protected float[][][] concTempGlobal;
	  
	  public BeadDispatcher(RDSystem parent){
	    super(parent);
	  }
	  
	  public BeadDispatcher(int niceness, RDSystem parent){
	    super(niceness,parent);
	  }
	  
	  @Override
	   protected void makeThreads(){
	    concTempGlobal = new float[parent.chemicalSpecies][(int) (RDConstants.wsize/RDConstants.spaceStep)][(int) (RDConstants.wsize/RDConstants.spaceStep)]; //has to be here to have the right size and be initialized before the threads
	    for (int i = 0; i<availableProc; i++){
	      threads.add(new BeadUpdateMechanism(availableProc,i,concTempGlobal));
	    }
	   }
	    
	    public void updateBeads(){
	      //Dispatch the targets among procs
	      
	      for (GenericThreadComputation<Boolean> t : threads){
	        if (BeadUpdateMechanism.class.isAssignableFrom(t.getClass())){
	          BeadUpdateMechanism tb = (BeadUpdateMechanism) t;
	          ArrayList<Aggregate> a = new ArrayList<Aggregate>();
	          ArrayList<Bead> b = new ArrayList<Bead>();
	          int ID = tb.getID();
	          for (int i = ID; i< parent.aggregates.size(); i+=availableProc){
	            a.add(parent.aggregates.get(i));
	          }
	          for (int i = ID; i< parent.beads.size(); i+=availableProc){
	            b.add(parent.beads.get(i));
	          }
	          tb.setTargets(a,b);
	        }
	      }
	      
	      exec();
	    }
	}
