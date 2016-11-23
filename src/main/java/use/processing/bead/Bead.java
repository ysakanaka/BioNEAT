package use.processing.bead;
import java.util.Random;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import model.chemicals.Template;
import model.Constants;
import use.processing.rd.RDConstants;
import use.processing.rd.RDSystem;

public class Bead implements Serializable{
 
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
  protected float x;
  protected float y;
  protected float radius;
  
  protected int realx;
  protected int realy;
  protected int oldx;
  protected int oldy;
  protected int realradius;
  
  transient protected RDSystem system;
  Aggregate parent = null;
  transient protected ArrayList<Template<String>> temps;
  
  public static Random rand = new Random();
  
  public HashSet<Bead> neighbors = new HashSet<Bead>();
  
  
  double connect;
  
  double detach; //detach every 3 time steps on average
  
  static float[][][] conc; //set by the outside
  
  public Bead(RDSystem system, float x, float y, float radius, ArrayList<Template<String>> temps){ //I should also define a set of conc
    this.system = system;
	this.x = x; //  0.5f*RDConstants.hsize; // for line debug
    this.y = y;
    this.radius = radius;
    this.temps = temps;
    connect = Constants.Kduplex;
    //Ok, os should have always the same 3 first species
    model.chemicals.SequenceVertex glu = system.os.getSequences().get(RDConstants.glueIndex);
    detach = Constants.Kduplex*system.os.getGraph().getK(glu);
    
    realx = (int) (x/RDConstants.spaceStep);
	realy = (int) (y/RDConstants.spaceStep);
	oldx = realx;
	oldy = realy;
	realradius = (int) (radius/RDConstants.spaceStep);
  }
  
  public float distance(float x, float y){
    return (float) Math.sqrt((x-this.x)*(x-this.x)+(y-this.y)*(y-this.y));
  }
  
  public ArrayList<Template<String>> getTemplates(){
    return temps;
  }
  
  public void updateMove(int maxX, int maxY){
  
   if(parent!=null){
     double dice = getNext(detach);
     //System.out.println(dice);
     if (dice < RDConstants.timePerStep){
       Aggregate a = parent;
       a.beads.remove(this);
       this.parent = null;
     }
   } else {
   double D =  RDConstants.beadScale;//test.kb * test.temperature / 6 / Math.PI / (radius*2*test.beadScale) / test.waterViscosity;
   double factor = 2 * D * RDConstants.timePerStep / 2;
   
   if (RDConstants.beadExclusion){
	   for (Bead b: neighbors){
      	 if(this!=b && b.distance(this.x,this.y)<0.75*radius) factor *= RDConstants.bounceRatePerBead;
	   }
//	   for(Bead b: system.beads){
//		   if(this!=b && b.distance(this.x,this.y)<0.75*radius) factor *= RDConstants.bounceRatePerBead;
//	   }
   }
   this.x +=(float) Math.sqrt(factor) * rand.nextGaussian();
   this.y +=(float) Math.sqrt(factor) * rand.nextGaussian();
   if(x >= maxX-radius/2f){
	      x=maxX-1-radius/2f;
	    }
	    if(x < radius/2.0f){
	      x=radius/2.0f;
	    }
	    if(y >= maxY-radius/2f){
	      y = maxY -1-radius/2f;
	    }
	    if(y < radius/2.0f){
	      y = radius/2.0f;
	    }
	    oldx = realx;
	    oldy = realy;
	    realx = (int) (x/RDConstants.spaceStep);
	    realy = (int) (y/RDConstants.spaceStep);
   }
  }
   //first, compute anti glue strength. ACTUALLY, we may want to have this as a separate stuff.
   //float totAntiglue = antiglueLeak; //comes from me
   //for(Bead b: beads){
   //  if(this!=b && b.distance(this.x,this.y)<2*radius ){
   //    totAntiglue += antiglueLeak;
   //  }
   //}
   //for(Bead b: system.beads){

  public void updateGlue(float[][][] conc){
	  double forward = conc[RDConstants.glueIndex][system.wrapCoord((int)(x/RDConstants.spaceStep))]
			   [system.wrapCoord((int)(y/RDConstants.spaceStep))]*connect; 
			
	 for (Bead b: neighbors){
     if(this!=b && b.distance(this.x,this.y)<1.01*radius && (b.parent ==null || parent !=b.parent)){ 
       double dice = getNext(forward);
       //System.out.println(dice);
       //float glue = getGlue(x,y);
       
       if(dice < RDConstants.timePerStep){
         if(this.parent==null && b.parent==null){
           Aggregate a = new Aggregate(system);
           a.beads.add(this);
           a.beads.add(b);
           this.parent = a;
           b.parent = a;
           system.aggregates.add(a);
         } else if (this.parent==null){
           b.parent.beads.add(this);
           this.parent = b.parent;
        } else if (b.parent==null){
           parent.beads.add(b);
           b.parent = parent;
         } else {
           //test.aggregates.remove(b.parent);
           Aggregate old = b.parent;
           for( Bead allBeads: old.beads){
             parent.beads.add(allBeads);
             allBeads.parent = parent;
           }
           old.beads.removeAll(old.beads);
         }
       }
     }
   }
   }
    
  
  public void update(float x, float y){
   
   this.x += x;
   this.y += y;
   oldx = realx;
   oldy = realy;
   realx = (int) (this.x/RDConstants.spaceStep);
   realy = (int) (this.y/RDConstants.spaceStep);

  }
  
  public void cleanSpot(){
	  for (int i = -realradius/2-2; i <= realradius/2+1; i++){
		  for (int j = -realradius/2 -2; j <= realradius/2+1; j++){
			if (system.beadsOnSpot.get(oldx+i, oldy+j) != null) {
				system.beadsOnSpot.get(oldx+i, oldy+j).remove(this);
			}
		  }
	  }
  }
  
  public void resetNeighbors(){
	  this.neighbors = new HashSet<Bead>();
  }
  
  public void addNeighbor(Bead b){
	  this.neighbors.add(b);
  }
  
  public float getX(){
    return x;
  }
  
  public float getY(){
    return y;
  }
  
  public float getRadius(){
    return radius;
  }
  
  public static double getNext(double lambda) {
	    return  Math.log(1-rand.nextDouble())/(-lambda);
  }
  
  
}