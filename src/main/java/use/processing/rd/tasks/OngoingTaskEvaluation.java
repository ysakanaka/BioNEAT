package use.processing.rd.tasks;

public abstract class OngoingTaskEvaluation {

	protected String name;
	protected int evaluation = 0;
	
	public OngoingTaskEvaluation(String name){
		this.name = name;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		OngoingTaskEvaluation ote = new OngoingTaskEvaluation("test"){
			public double evaluateNow(float[][][] species){
				evaluation++;
				return 0.0;
			}
		};
		
		for(int i = 0; i<3; i++)
		System.out.println(ote.getName()+" "+ote.evaluateNow(null));

	}
	
	public String getName(){
		return getBaseName()+"_"+evaluation;
	}
	
	public String getBaseName(){
		return name;
	}
	
	public abstract double evaluateNow(float[][][] species);

}
