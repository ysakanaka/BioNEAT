package use.processing.rd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.common.collect.HashBasedTable;

import model.OligoSystem;
import model.chemicals.SequenceVertex;
import model.chemicals.Template;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkContainer;
import use.processing.bead.Aggregate;
import use.processing.bead.Bead;
import use.processing.parallel.DiffusionDispatcher;

/**
 * Object holding all the details of a specific RDsystem.
 * @author naubertkato
 *
 */
public class RDSystem implements ReactionNetworkContainer{
	

	public long totalBeads = 0; //for timing
	public long totalConc = 0;
	public long realTime = System.currentTimeMillis();
	
	public float[] diffRate;	

	public int chemicalSpecies;

	public float conc[][][]; //will be a problem if multiple evals in parallel. So we do only one (parallel) eval at a time.

	public HashMap<SequenceVertex,Integer> seqAddress; //Connect addr in conc and Seq in Graph  
	public HashMap<Template<String>,Integer> tempAddress; //Connect addr in conc and Temp in Graph  
	
	public HashBasedTable<Integer, Integer, ArrayList<Bead>> beadsOnSpot;

	public int[][][] neighbors;

	transient DiffusionDispatcher dd;
	//BeadDispatcher bd;
	
	//transient float concHistory[][][][]; //all time steps, (or once in a while)

	

	public ArrayList<Bead> beads = new ArrayList<Bead>();

	public ArrayList<Aggregate> aggregates = new ArrayList<Aggregate>();

	transient private OligoSystem<String> os;
	protected ReactionNetwork network;
	
	public void setOS(OligoSystem<String> os){
		this.os = os;
	}
	
	public OligoSystem<String> getOS(){
		return this.os;
	}
	
	public ReactionNetwork getReactionNetwork(){
		return network;
	}
	
	protected void initNetwork(){
		if(network != null) return;
		if(ReactionNetworkContainer.class.isAssignableFrom(os.getClass())){
			network= ((ReactionNetworkContainer) os).getReactionNetwork();
		}
		
		//we have a plain old os, TODO we need to make a reaction network out of him
		System.out.println("WARNING: RDSystem init Network: no reaction network provided");
		network = null;
	}
	
	public void setNetwork(ReactionNetwork network){
		if(this.network != null) System.out.println("WARNING: RDSystem network setting: network already exist; replacing.");
		
		this.network = network;
	}
	
	public void init(boolean GUI){
		if(os == null){
			System.err.println("ERROR: RDSystem init: no OligoSystem specified");
		}
		initNetwork();
		  chemicalSpecies = os.getDimension();
		  setSeqAddress();
		  
		  conc = new float[chemicalSpecies][(int) (RDConstants.wsize/RDConstants.spaceStep)][(int) (RDConstants.hsize/RDConstants.spaceStep)];
		  //concHistory = new float[RDConstants.maxTimeEval/RDConstants.bigTimeStep][chemicalSpecies][(int) (RDConstants.wsize/RDConstants.spaceStep)][(int) (RDConstants.hsize/RDConstants.spaceStep)];
		  beadsOnSpot = HashBasedTable.create((int) (RDConstants.wsize/RDConstants.spaceStep),(int) (RDConstants.hsize/RDConstants.spaceStep));
		  diffRate = new float[chemicalSpecies];
		  defaultDiff();
		  for(int i = RDConstants.glueIndex; i<(os.total+os.inhTotal); i++) diffRate[i] = RDConstants.fastDiff; //gradients are processed independently 
		  if(RDConstants.timing) realTime = System.currentTimeMillis();
		  initBeads(RDConstants.maxBeads);
		  if(RDConstants.timing) totalBeads+=System.currentTimeMillis() - realTime;
		  dd = new DiffusionDispatcher(GUI?RDConstants.nicenessGUIEval:RDConstants.nicenessEvoEval,this);
		  //bd = new BeadDispatcher(this);
		  neighbors = new int[conc[0].length][conc[0][0].length][4]; //Basic neighborhood.
		  initNeighbors();
		  setGradients();
		  for(int i = RDConstants.gradients?2:0; i<(os.total+os.inhTotal); i++) initConc(i,0,0);//blob of species 0 at the origin
		  
		  if(RDConstants.timing) realTime = System.currentTimeMillis();
		  for(Bead bead : beads) updateConcFromBead(bead);
		  if(RDConstants.timing) totalBeads+=System.currentTimeMillis() - realTime;
	}
	
	public void update(){
		if(RDConstants.timing) realTime = System.currentTimeMillis();
		//for(Bead bead : beads) cleanConcFromBead(bead);
		cleanConcFromBead();
		setNeighbors();
		for(Bead bead: beads)  updateConcFromBead(bead);
	    ArrayList<Aggregate> fakeAggre = new ArrayList<Aggregate>(aggregates);
	    for(Aggregate aggr : fakeAggre) aggr.update();
	    //bd.updateBeads();
	    for(Bead bead : beads) bead.updateMove((int)(conc[0].length*RDConstants.spaceStep),(int)(conc[0][0].length*RDConstants.spaceStep));
	    
	    for(Bead bead: beads) bead.updateGlue(conc);
	    
	    if(RDConstants.timing) {
	      totalBeads+=System.currentTimeMillis() - realTime;
	      realTime = System.currentTimeMillis();
	    }
	    dd.updateConc();
	    if(RDConstants.timing) totalConc += System.currentTimeMillis() - realTime;
	}
	
	public void setGradients(){
    	
    	for(int x = 0; x<conc[0].length;x++){
    		for (int y = 0; y<conc[0][x].length; y++){
    			float localVal =  (float) (RDConstants.concChemostat*Math.exp(-RDConstants.gradientScale*Math.sqrt((x*x+y*y)*(RDConstants.spaceStep*RDConstants.spaceStep))));

	    		//I could make this parallel as well. hum
			
    		    conc[0][x][y] =localVal;
    		    conc[1][conc[1].length-1-x][y] =localVal;
    	    	
    		}
    	}
    	
    }
	
	public void setSeqAddress(){

		  seqAddress = new HashMap<SequenceVertex,Integer>();
		  Iterator<SequenceVertex> it = os.getSequences().iterator();
		  int where = 0;
		  while (it.hasNext()){
		    seqAddress.put(it.next(),where);
		    where++;
		  }
		  tempAddress = new HashMap<Template<String>,Integer>();
		  Iterator<Template<String>> it2 = os.getTemplates().iterator();
		  while (it2.hasNext()){
		    tempAddress.put(it2.next(),where);
		    where++; //templates go after the seqs
		  }
		}

		public void defaultDiff(){
		  for (int species = 0; species<chemicalSpecies;species++){
		    diffRate[species]= 0.0f;
		  }
		}

		public int wrapCoord(int value){ //only works in a square env WARNING TODO
		 if (RDConstants.wrap){
		  int ret = (value % conc[0].length);
		  if (ret <0) return ret + conc[0].length;
		  
		  return ret;
		 } else {
		   return Math.min(conc[0].length-1,Math.max(0,value));
		 }
		}
		

		public void initNeighbors(){
		  for (int x = 0; x < conc[0].length; x++){
		    for (int y = 0; y < conc[0][x].length; y++){ // could use wrapcoord instead. Oh well.
		      neighbors[x][y][0] = RDConstants.wrap?(x==0?conc[0].length-1:x-1):Math.max(0,x-1); //previous x
		      neighbors[x][y][1] = RDConstants.wrap?(x==conc[0].length-1?0:x+1):Math.min(conc[0].length-1,x+1); //next x 
		      neighbors[x][y][2] = RDConstants.wrap?(y==0?conc[0][0].length-1:y-1):Math.max(0,y-1); //previous y
		      neighbors[x][y][3] = RDConstants.wrap?(y==conc[0][0].length-1?0:y+1):Math.min(conc[0].length-1,y+1); //next y 
		    }
		  }
		}

		public void initConc(int species, int xoffset, int yoffset){
		  
		  for(int i = 0; i< conc[species].length/RDConstants.ratio;i++){
		    for(int j = 0; j<conc[species][i].length/RDConstants.ratio; j++){
		      conc[species][(i+xoffset)%RDConstants.wsize][(j+yoffset)%RDConstants.hsize] = RDConstants.initConc;
		    }
		  }
		  
		}

		public void initBeads(int nBeads){
		  ArrayList<Template<String>> temps = new ArrayList<Template<String>>(os.getTemplates());
		  for(int i=0; i<nBeads; i++){
		    beads.add(new Bead(this,(float)(Bead.rand.nextDouble()*RDConstants.wsize),(float)(Bead.rand.nextDouble()*RDConstants.hsize),RDConstants.beadRadius,temps));
		  }
		}

		@Deprecated
		public void cleanConcFromBeadOld(Bead b){
			int realx = (int) (b.getX()/RDConstants.spaceStep);
			int realy = (int) (b.getY()/RDConstants.spaceStep);
			int radius = (int) (b.getRadius()/RDConstants.spaceStep);
			  
			  
			 for (int i = -radius; i <= radius; i++){
			  for (int j = -radius; j <= radius; j++){
			    for(Template<String> t: b.getTemplates()) conc[tempAddress.get(t)][wrapCoord(realx+i)][wrapCoord(realy+j)] = 0.0f; //if not moving too fast, should be ok. That's why we are a bit generous on the boundaries tested
			  }
			 }
			  
			}
		
		public void cleanConcFromBead(){
			
			  
			  
			 //for (int i = os.inhTotal+os.total; i < conc.length; i++){
			 // conc[i] = new float[conc[i].length][conc[i][0].length];
			 //}
			 
			 beadsOnSpot = HashBasedTable.create((int) (RDConstants.wsize/RDConstants.spaceStep),(int) (RDConstants.hsize/RDConstants.spaceStep));
			 //for(Bead b: beads){
			 //	 b.cleanSpot();
			 //}
			
			}
		
		public void setNeighbors(){
			for(Bead b: beads) b.resetNeighbors();
			for(int i = 0; i<beads.size()-1; i++){
				Bead b = beads.get(i);
				for (int j= i+1;j<beads.size(); j++){
					Bead bp = beads.get(j);
					if(b.distance(bp.getX(), bp.getY())<1.01*RDConstants.beadRadius){
						b.addNeighbor(bp);
						bp.addNeighbor(b);
					}
				}
				
			}
		}

            @Deprecated
			public void updateConcFromBead(Bead b){

			//first, figure out where it is

			int realx = (int) (b.getX()/RDConstants.spaceStep);
			int realy = (int) (b.getY()/RDConstants.spaceStep);
			int radius = (int) (b.getRadius()/RDConstants.spaceStep);

			for (int i = -radius/2-2; i <= radius/2+1; i++){
			  if (realx +i < 0 || realx+i >= conc[0].length) continue;
			  for (int j = -radius/2-2; j <= radius/2 +1; j++){
				 if (realy +j < 0 || realy+j >= conc[0][0].length) continue;
			    if (b.distance((realx+i)*RDConstants.spaceStep,(realy+j)*RDConstants.spaceStep)<=(radius*RDConstants.spaceStep)/2){
			      //for(Template<String> t: b.getTemplates()) conc[tempAddress.get(t)][wrapCoord(realx+i)][wrapCoord(realy+j)] += t.totalConcentration;
			      if(beadsOnSpot.get(realx+i, realy+j) == null) beadsOnSpot.put(realx+i, realy+j, new ArrayList<Bead>());
			      if(!beadsOnSpot.get(realx+i, realy+j).contains(b)) beadsOnSpot.get(realx+i, realy+j).add(b);
			    }
			  }
			}


			}

			public static boolean isProtected(Integer integer) {
				if (RDConstants.gradients && integer < RDConstants.speciesOffset) return true;
				return false;
			}
		
}
