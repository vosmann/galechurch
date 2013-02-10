package hr.fer.zemris.ktlab.sap.algorithms.gc;

public class Alignment {
	
	/** Type of the alignment (deletion-10, insertion-01, substitution-11, etc.) */
	public int type;
	
	
	/** The key of the first sentence in the source language in this alignment. */
	public int x1;
	/** The key of the first sentence in the destination language in this alignment. */
	public int y1;
	/** The key of the second sentence in the source language in this alignment. */
	public int x2;
	/** The key of the second sentence in the destination language in this alignment. */
	public int y2;	
	/** The cost (distance) of this alignment. */
	public int cost;
	/** A reference to the cheapest predecessor in the dynamic programming framework. */
	public Alignment cheapestPredecessor;
	
	public Alignment() {
		super();
		
		this.x1 = -1;
		this.y1 = -1;
		this.x2 = -1;
		this.y2 = -1;
		this.cost = 0;
		this.cheapestPredecessor = null;
	}	
}
