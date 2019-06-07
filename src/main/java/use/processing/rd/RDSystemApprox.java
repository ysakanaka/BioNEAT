package use.processing.rd;

import java.util.ArrayList;

import com.google.common.collect.HashBasedTable;

import model.chemicals.Template;
import use.processing.bead.Aggregate;
import use.processing.bead.Bead;
import use.processing.parallel.DiffusionDispatcher;

/**
 * Class designed to estimate the theoretical concentration in different spots.
 * @author naubertkato
 *
 */
public class RDSystemApprox extends RDSystem {

	@Override
	public void initBeads(int nBeads){
		  ArrayList<Template<String>> temps = new ArrayList<Template<String>>(os.getTemplates());
		  for (int x = 0; x < RDConstants.wsize; x++){
			    for (int y = 0; y < RDConstants.hsize; y++){
			    	beads.add(new Bead(this,(float) x,(float) y,0.0f,temps));
			    }
		  }
		}
	
	@Override
	public void update(){
		
	    dd.updateConc();
	    
	}
	
}
