package use.processing.rd;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Panel;

//import javax.swing.JPanel;

public class RDImagePrinter extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8782485912207043987L;

	protected float[][][] conc;

	public RDImagePrinter(float[][][] conc){ //float
			this.conc=conc;
		}

	public Dimension getPreferredSize() {
		return new Dimension(RDConstants.wsize, RDConstants.hsize);
	}

	public void paintComponent(Graphics g) {
		Color base = g.getColor();
		
		for (int x = 0; x < conc[0].length; x++) {
			for (int y = 0; y < conc[0][x].length; y++) {
				float val = 0.0f;
				float val2 = 0.0f;
				float val3 = 0.0f;
				if(RDConstants.showBeads){
			    	  //Table<Integer,Integer,ArrayList<Bead>> beads = res.getBeads();
			    	  //int pos = res.getBeadIndex();
			    	  boolean here = conc[0+RDConstants.speciesOffset][x][y]>RDConstants.cutOff;
			    	  
			    	  val = here?1.0f:0.0f;
			    	  val2 = here?1.0f:0.0f;
			    	  val3 = here?1.0f:0.0f;
			      } else {
			      val = Math.min(1.0f,conc[0+RDConstants.speciesOffset][x][y]/RDConstants.concScale);
			      if (conc.length >= 2+RDConstants.speciesOffset) val2 = Math.min(1.0f,conc[1+RDConstants.speciesOffset][x][y]/RDConstants.concScale);
			      if (conc.length >= 3+RDConstants.speciesOffset) val3 = Math.min(1.0f,conc[2+RDConstants.speciesOffset][x][y]/RDConstants.concScale);
			      }
				g.setColor(new Color(Math.max(0.0f, val), Math.max(0.0f, val2), Math.max(0.0f, val3)));
				g.fillRect((int) (x * RDConstants.spaceStep), (int) (y * RDConstants.spaceStep),
						(int) RDConstants.spaceStep, (int) RDConstants.spaceStep);
			}
		}
		g.setColor(base);
	}

}
