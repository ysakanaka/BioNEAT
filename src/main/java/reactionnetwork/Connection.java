package reactionnetwork;

public class Connection {
	public int innovation;
	public Node from;
	public Node to;
	public boolean enabled = true;
	public double parameter;

	public Connection(Node from, Node to) {
		this.from = from;
		this.to = to;
	}

}
