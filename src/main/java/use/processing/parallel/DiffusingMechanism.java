package use.processing.parallel;
import model.chemicals.*;
import use.processing.rd.RDConstants;
import use.processing.rd.RDSystem;
import model.Constants;

public class DiffusingMechanism extends GenericThreadComputation<Boolean>{
  
  public float[][][] concTemp; //shared object
  protected RDSystem system;
  
  public DiffusingMechanism(int total, int id, float[][][] _concTemp, RDSystem system){
    super(total,id);
    concTemp = _concTemp;
    this.system = system;
  }

  protected float diffuse(int species, int x, int y){
    float nextConc = (1.0f - 4.0f*system.diffRate[species])*system.conc[species][x][y]; //conc comes from the global englobing class (called Test)
          nextConc += system.diffRate[species]*(system.conc[species][system.neighbors[x][y][0]][y]+system.conc[species][system.neighbors[x][y][1]][y]
                          +system.conc[species][x][system.neighbors[x][y][2]]+system.conc[species][x][system.neighbors[x][y][3]]);
    return nextConc;
 
  }
  
  protected void react(int x, int y){ 
  
  //Computing derivatives: for each template, add its local contribution
  if(system.beadsOnSpot.get(x, y)!=null){ //Beads have templates
    for(Template<String> t : system.getOS().getTemplates()){
      if (PadiracTemplate.class.isAssignableFrom(t.getClass())){
        PadiracTemplate pt = (PadiracTemplate) t;
        double conctemp = system.beadsOnSpot.get(x, y).size()*pt.totalConcentration;//system.conc[system.tempAddress.get(t)][x][y];
        double concin = system.conc[system.seqAddress.get(pt.getFrom())][x][y];
        double concout = system.conc[system.seqAddress.get(pt.getTo())][x][y];
        double concinhib = pt.getInhib()!=null?system.conc[system.seqAddress.get(pt.getInhib())][x][y]:0.0;
        concTemp[system.seqAddress.get(pt.getTo())][x][y] += RDConstants.timePerStep*pt.outputSequenceFlux(conctemp,concin,concout,concinhib);
      }
    }
  }
  
  //Then add the contribution from exonuclease
  for(SequenceVertex s: system.getOS().getSequences()){
    double exoKm = s.isInhib()?Constants.exoKmInhib:Constants.exoKmSimple;
    if (!RDSystem.isProtected(system.seqAddress.get(s))) concTemp[system.seqAddress.get(s)][x][y] -= RDConstants.timePerStep*system.conc[system.seqAddress.get(s)][x][y]
    		*system.getOS().getGraph().exoConc*Constants.exoVm/exoKm;
  }
  
  }
  
  @Override
  public Boolean call(){
    int totalComputed = system.conc[0].length*system.conc[0][0].length;
    //System.out.println("Called with params: "+myID+" "+totalThreads+" totalComputed: "+totalComputed);
   
    for (int i = myID; i<totalComputed; i+=totalThreads){
      int x = i % system.conc[0].length;
      int y = i / system.conc[0].length;
      
      // System.out.println("Results for "+x+" "+y+": "+val);
       try{
         for (int species = 0; species < system.chemicalSpecies; species++){
           float val = diffuse(species,x,y);
           concTemp[species][x][y] = val;
           //System.out.println("Results for "+species+" "+x+" "+y+": "+val);
         }
         react(x,y);
       } catch(Exception e){
       System.err.println(e);
       }
      
    }
    
    return new Boolean(true);
  }

}