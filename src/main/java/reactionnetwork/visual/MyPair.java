package reactionnetwork.visual;

public class MyPair<L, R> {

	private final L left;
	private final R right;

	public MyPair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		return left.hashCode() ^ right.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof MyPair))
			return false;
		@SuppressWarnings("unchecked")
		MyPair<L,R> pairo = (MyPair<L,R>) o;
		return this.left.equals(pairo.getLeft())
				&& this.right.equals(pairo.getRight());
	}

}
