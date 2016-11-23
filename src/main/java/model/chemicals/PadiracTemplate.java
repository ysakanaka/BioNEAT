package model.chemicals;

import model.OligoGraph;
import model.Constants;

public class PadiracTemplate extends Template<String> {
  
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public PadiracTemplate(OligoGraph<SequenceVertex, String> parent, double totalConcentration,SequenceVertex from, SequenceVertex to, SequenceVertex inhib){
    super(parent, totalConcentration, 1.0, 1.0, 1.0, from, to, inhib);
    numberOfStates = 0;
  }
  
  @Override
   public double[] flux() {
    double[] ans = {}; //We only consider the total concentration
    return ans;
  }
  
   @Override
   public double[] getStates() {
    double[] ans = new double[]{}; //We only consider the total concentration
    return ans;
  }
  
  @Override
  public void setStates(double[] values) throws InvalidConcentrationException {
    return; //Nothing should happen
  }
  
  @Override
   public double inhibSequenceFlux() {
    return 0.0;
  }

  @Override
  public double inputSequenceFlux() {
   return 0.0;
  }


//Simplified version from Hagiya et al.
  public double outputSequenceFlux() {
    double concin = this.from.getConcentration();
    double concout = this.to.getConcentration();
    double kinhib;
    double inhib;
    
    double kin = ((Double)this.parent.K.get(this.from)).doubleValue();
    double kout = ((Double)this.parent.K.get(this.to)).doubleValue();
    
    if (this.inhib != null) {
      inhib = this.inhib.getConcentration();
      kinhib = ((Double)this.parent.K.get(this.inhib)).doubleValue();
    }
    else {
      inhib = 0.0D;
      kinhib = 0.0D;
    }
    double pol;
    if (this.parent.isInhibitor(this.to))
      pol = this.polyboth * Constants.displ;
    else {
      pol = this.poly;
    }
    pol*=parent.polConc;
    double nick = this.nick*parent.nickConc;
    
    //padirac dimensional params
    double a = pol* nick / (pol + nick);
    double b = nick /(nick + pol)*kin;
    double c = (kinhib>0?b*kout/(kinhib*concout+kout*kinhib):0);
    double debug =  a * concentrationAlone * concin/ (b + concin + c * inhib);
    //System.out.println(a+" "+b+" "+c+" "+debug);
   return debug;
  }
  
  //For RD
  public double outputSequenceFlux(double tempConc, double concin, double concout, double concinhib){
   double kinhib;
    
    double kin = ((Double)this.parent.K.get(this.from)).doubleValue();
    double kout = ((Double)this.parent.K.get(this.to)).doubleValue();
    
    if (this.inhib != null) {
      kinhib = ((Double)this.parent.K.get(this.inhib)).doubleValue();
    }
    else {
      kinhib = 0.0D;
    }
    double pol;
    if (this.parent.isInhibitor(this.to))
      pol = Constants.polVm/Constants.polKmBoth * Constants.displ;
    else {
      pol =Constants.polVm/Constants.polKm;
    }
    pol*=parent.polConc;
    double nick = Constants.nickVm/Constants.nickKm*parent.nickConc;
   // System.out.println(parent+" "+pol+" "+nick);
    //padirac dimensional params
    double a = pol* nick / (pol + nick);
    double b = nick /(nick + pol)*kin;
    double c = (kinhib>0?b*kout/(kinhib*concout+kout*kinhib):0);
    double debug =  a * tempConc * concin/ (b + concin + c * concinhib);
    //System.out.println(a+" "+b+" "+c+" "+debug);
   return debug;
  }
}