package model;


public class InhibitingSequence extends Sequence{
	/**
	 * A sequence inhibiting a template producing the sequence <b>to<\b> from the sequence <b>from<\b>.
	 * Those sequences are assumed to be SimpleSequences for now.
	 */

	//public final int ID; // Seems to be useless
	public final double KInhib;  // New in Model 06

	private SimpleSequence from;
	private SimpleSequence to;
	
	public double exoKm = Constants.exoKmInhib; //Note: -1 means no saturation

public InhibitingSequence(double initialConcentration,double K,double KInhib,SimpleSequence from,SimpleSequence to) throws InvalidKException, InvalidConcentrationException{
	super(initialConcentration,K,Constants.inhibKmin,Constants.inhibKmax);
	//this.ID=total; //No practical use anymore
	this.KInhib= KInhib;
	this.from = from;
	this.to = to;
	this.seq = "GAA"; //just the beginning and the end
}


public double getKmin(){
	return Constants.inhibKmin;
}

public double getKmax(){
	return Constants.inhibKmax;
}

public Sequence getFrom(){
	return from;
}

public Sequence getTo(){
	return to;
}

public int getID(){
	return from.getID()*10+to.getID();
}

//New in Model 06
public String toString(){
	return "InhibitingSequence "+from.getID()+"->"+to.getID();
}
public double getExoSaturation(){
	double value = 0;
	if(exoKm != -1){
		value += concentration/exoKm;
	}
	
	return value;
}
}
