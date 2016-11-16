package reactionnetwork;

import java.io.Serializable;

public class Connection implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int innovation;
	public Node from;
	public Node to;
	public boolean enabled = true;
	public double parameter = 0;

	public Connection(Node from, Node to) {
		this.from = from;
		this.to = to;
	}
	
	@Override
	public boolean equals(Object o){
		if(o.getClass()!= this.getClass()){
			return false;
		}
		Connection c = (Connection) o;
		return c.innovation == innovation && c.from.equals(from) && c.to.equals(to) && c.enabled == enabled
				&& c.parameter == parameter;
	}
	
	@Override
	public int hashCode(){
		int result = 349;
		int prime = 37;
		result = prime*result + innovation;
		result = prime*result + from.hashCode();
		result = prime*result + to.hashCode();
		result = prime*result + (enabled?1:0);
		long f = Double.doubleToLongBits(parameter);
		result = prime * result + (int) (f ^ (f>>>32));
		return result;
	}

}
