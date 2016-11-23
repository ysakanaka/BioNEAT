package use.processing.parallel;

import use.processing.rd.RDConstants;
import use.processing.rd.RDSystem;

public class DiffusionDispatcher extends Dispatcher<Boolean> {
	  
	  protected float[][][] concTempGlobal;
	  
	  public DiffusionDispatcher(RDSystem parent){
	    super(parent);

	  }
	  
	  public DiffusionDispatcher(int niceness, RDSystem parent){
	    super(niceness, parent);
	  }
	  
	  @Override
	   protected void makeThreads(){
	    concTempGlobal = new float[parent.chemicalSpecies][(int) (RDConstants.wsize/RDConstants.spaceStep)]
	    		[(int) (RDConstants.hsize/RDConstants.spaceStep)]; //has to be here to have the right size and be initialized before the threads
	    for (int i = 0; i<availableProc; i++){
	      threads.add(new DiffusingMechanism(availableProc,i,concTempGlobal,parent));
	    }
	   }
	    
	    public void updateConc(){
	      exec();
	      //has to be a deep copy
	      for(int species = 0; species < parent.chemicalSpecies; species++){
	        for (int x = 0; x < parent.conc[species].length; x++){
	          for (int y = 0; y < parent.conc[species][x].length; y++){
	            //System.out.println(x+" "+y+" "+conc[x][y]+" "+concTempGlobal[x][y]);
	        	  parent.conc[species][x][y] = Math.max(0.0f, concTempGlobal[species][x][y]);
	          }
	        }
	      }
	      
	     
	   
	  }

	}
