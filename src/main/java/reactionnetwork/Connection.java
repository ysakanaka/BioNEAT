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

}
