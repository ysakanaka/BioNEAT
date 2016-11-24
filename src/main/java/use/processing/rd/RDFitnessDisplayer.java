package use.processing.rd;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import erne.AbstractFitnessResult;
import erne.FitnessDisplayer;
import use.processing.bead.Bead;

public class RDFitnessDisplayer implements FitnessDisplayer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6417888179846282610L;

	@Override
	public JPanel drawVisualization(AbstractFitnessResult fitnessResult) {
		
		JPanel panel;
		
		if(RDPatternFitnessResult.class.isAssignableFrom(fitnessResult.getClass())){
			RDPatternFitnessResult res = (RDPatternFitnessResult) fitnessResult;
			panel= new RDImagePanel(res);
		} else {
			panel = new JPanel();
		}
		
		return panel;
	}
	
	public static void main(String[] args){
		Random rand = new Random();
        boolean[][] target = new boolean[(int)(RDConstants.wsize/RDConstants.spaceStep)][(int)(RDConstants.hsize/RDConstants.spaceStep)];
		
		for(int i = 0; i<target.length; i++){
			for(int j = 0; j<target[i].length;j++){
				target[i][j] = (i>=target.length*(0.5f-0.3f/2.0f)&&i<=target.length*(0.5f+0.3f/2.0f));
			}
		}
		float[][][] conc = new float[3][(int)(RDConstants.wsize/RDConstants.spaceStep)][(int)(RDConstants.hsize/RDConstants.spaceStep)];
		RDConstants.speciesOffset = 0; //or we won't be correct
		RDConstants.showBeads = false; //we want colors;
		for(int i = 0; i<conc.length; i++){
			for (int j = 0; j<conc[i].length; j++){
				for (int k = 0; k<conc[i][j].length; k++){
					conc[i][j][k] = rand.nextFloat();
				}
			}
		}
		
		RDPatternFitnessResult res = new RDPatternFitnessResult(conc,target,HashBasedTable.<Integer,Integer,ArrayList<Bead>>create(),0.0);
		
		JFrame display = new JFrame("Visual test");
		display.add(new RDFitnessDisplayer().drawVisualization(res));
		display.pack();
		display.setVisible(true);
	}
	
	public class RDImagePanel extends JPanel{

		/**
		 * 
		 */
		private static final long serialVersionUID = -8782485912207043987L;
		
		protected RDPatternFitnessResult res;
		
		public RDImagePanel(RDPatternFitnessResult res){
			this.res=res;
		}
		
		public Dimension getPreferredSize(){
			return new Dimension(RDConstants.wsize, RDConstants.hsize);
		}
		
		public void paintComponent(Graphics g) {
			Color base = g.getColor();
			float[][][] conc = res.getConc();
			boolean[][] patt = res.getPattern();
			  for (int x = 0; x < conc[0].length; x++){
			    for (int y = 0; y < conc[0][x].length; y++){
			      float val = 0.0f;
			      float val2 = 0.0f;
			      float val3 = 0.0f;
			      if(RDConstants.showBeads){
			    	  //Table<Integer,Integer,ArrayList<Bead>> beads = res.getBeads();
			    	  //int pos = res.getBeadIndex();
			    	  boolean here = res.getPositions()[x][y];
			    	  
			    	  val = here?1.0f:(patt[x][y]?RDConstants.greyTargetScale:0.0f);
			    	  val2 = here?1.0f:(patt[x][y]?RDConstants.greyTargetScale:0.0f);
			    	  val3 = here?1.0f:(patt[x][y]?RDConstants.greyTargetScale:0.0f);
			      } else {
			      val = Math.min(1.0f,conc[0+RDConstants.speciesOffset][x][y]/RDConstants.concScale);
			      if (conc.length >= 2+RDConstants.speciesOffset) val2 = Math.min(1.0f,conc[1+RDConstants.speciesOffset][x][y]/RDConstants.concScale);
			      if (conc.length >= 3+RDConstants.speciesOffset) val3 = Math.min(1.0f,conc[2+RDConstants.speciesOffset][x][y]/RDConstants.concScale);
			      }
			      g.setColor(new Color(Math.max(0.0f,val),Math.max(0.0f, val2),Math.max(0.0f,val3)));
			      g.fillRect((int)(x*RDConstants.spaceStep),(int)(y*RDConstants.spaceStep),(int)RDConstants.spaceStep,(int)RDConstants.spaceStep);
			    }
			  }
			  g.setColor(base);
		}
		
	}

}
