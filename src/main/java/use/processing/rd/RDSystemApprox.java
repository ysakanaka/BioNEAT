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
		  for (int x = 0; x < conc[0].length; x++){
			    for (int y = 0; y < conc[0][x].length; y++){
			    	beads.add(new Bead(this,(float)(x*RDConstants.wsize)/(float) conc[0].length,(float)(y*RDConstants.hsize)/(float) conc[0][x].length,(float)RDConstants.wsize/(float)(2.0f * conc[0].length),temps));
			    }
		  }
		}
	
	public void update(){
		
	    dd.updateConc();
	    
	}
	
}
