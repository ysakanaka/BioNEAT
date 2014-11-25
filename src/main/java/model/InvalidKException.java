package model;


public class InvalidKException extends Exception{
	
	private static final long serialVersionUID = 1L;
	public double faultyK;
	
	InvalidKException(double K){
		faultyK = K;
	}

	public String toString(){
		return "Error: Sequence was created with K="+faultyK+" (out of bound)";
	}
	
}
