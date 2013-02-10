package hr.fer.zemris.ktlab.sap.algorithms.gc;

/**
 * The class exclusively contains static members and methods. These are used
 * for computing probabilities and corresponding distance measures for the 
 * given alignments of sentences. 
 * 
 * @author Vjekoslav Osmann
 *
 */

public class ProbabilityComputation {
	
	/**
	 * The constant <code>c</code> 	is the expected number of characters
	 * in <code>t</code>, the translation, per character in <code>s</code>, 
	 * the sentence in the source language. 
	 */
	private static final double c = 1;
	/**
	 * The constant <code>sSquared</code> is the variance of the number of 
	 * characters in <code>t</code>, the translation, per character in   
	 * <code>s</code>, the sentence in the source language.
	 */
	private static final double sSquared = 6.8;
	
	/** -100 * log( prob of 0-1 match / prob of 1-1 match ) */
	private static final int penalty01 = 450;
	
	/** -100 * log( prob of 2-1 match / prob of 1-1 match ) */
	private static final int penalty21 = 230;
	
	/** -100 * log( prob of 2-2 match / prob of 1-1 match ) */
	private static final int penalty22 = 440;
	
	/**
	 * The value returned by the method <code>d</code> in case of failure.
	 */
	private static final int bigDistance = 2500;
	
	
	/**
	 * The parameter delta as defined in the article by Gale and Church. 
	 * @param len1
	 * 		The number of characters in the source sentence. 
	 * @param len2
	 * 		The number of characters in the translation.
	 * @return
	 * 		The value of delta for the given values of len1 and len2.
	 */
	private static double delta(int len1, int len2) {
		double mean = ( (double)len1 + (double)len2/c )/2;
		return (  (double)len2 - (double)len1 * c)
				 /(Math.sqrt( mean*sSquared));
	}
	
	/**
	 * Computes the the probability that a random variable with a normal 
	 * distribution equals the value given in the argument. Uses an 
	 * approximation by Abramowitz and Stegun.
	 * @param x
	 * 		Usually the argument is the parameter delta.
	 * @return
	 * 		Returns the area under a normal distribution from -infinity to x.
	 */
	private static double pnorm(double x) {
		double t, pd;
		t = 1 / (1 + 0.2316419 * x);
		pd = 1 - 0.3989423 * Math.exp(-x * x/2) * 
			 (	(  (  (1.330274429 * t - 1.821255978) * t
					 + 1.781477937) * t 
				  - 0.356563782) * t 
			   + 0.319381530) * t;
		return pd;
	}

	/**
	 * Computes the distance value between a sentence <code>length2</code>
	 * characters long and a sentence <code>length1</code> characters long. 
	 * @param length1
	 * 		Length of the source sentence.
	 * @param length2
	 * 		Length of the source sentence's translation.
	 * @param type
	 * 		Type of translation alignment (01, 10, 11, 12, 21, 22) signifying
	 * 		whether an insertion, a deletion, substitution (one-on-one 
	 * 		translation), expansion, contraction or a merger (two-on-two 
	 * 		translation), respectively, was used. 
	 * @return
	 * 		Returns -100 * log probability that a sentence of length 
	 * 		<code>length2</code>, in the translation, is indeed the translation of a 
	 * 		source sentence of length <code>length1</code>. 
	 */
	private static int match(int length1, int length2) {
		double delta = 0.0D;
		double p_delta_match = 0.0D;
		if ( (length1==0)&&(length2==0) ) return 0;
		delta = delta(length1, length2);
		if (delta < 0) delta = -delta;
		p_delta_match = 2 * ( 1-pnorm(delta));
		if ( p_delta_match>0 ) return ( (int)(-100 * Math.log(p_delta_match)) );
		else return bigDistance;
	}
	
	/**
	 * The distance measure method (called "two_side_distance" in the original
	 * C implementation attached to Gale&Church's article). An additional 
	 * distance penalty in case of 0-1, 1-0, 1-2, 2-1, 2-2 alignments is added
	 * to each corresponding computed distance measure.
	 * @param l1
	 * 		Number of characters (length) in the segment in the source 
	 * 		language. May be the length of one sentence or the sum of lengths
	 * 		of a pair of neighboring sentences. 
	 * @param l2
	 * 		Number of characters (length) in the segment in the translation. 
	 * 		May be the length of one sentence or the sum of lengths of a pair
	 * 		of neighboring sentences.
	 * @param type
	 * 		Type of translation alignment (01, 10, 11, 12, 21, 22) signifying
	 * 		whether an insertion, a deletion, substitution (one-on-one 
	 * 		translation), expansion, contraction or a merger (two-on-two 
	 * 		translation), respectively, was used. 
	 * @return
	 * 		The distance measure for two segments of text. 
	 */
	public static int d(int l1, int l2, int type) {
		switch (type) {
			case 01 : return ( match(l1, l2) + penalty01 ); 
			case 10 : return ( match(l1, l2) + penalty01 ); 
			case 11 : return ( match(l1, l2) ); 
			case 12 : return ( match(l1, l2) + penalty21 ); 
			case 21 : return ( match(l1, l2) + penalty21 );
			default : return ( match(l1, l2) + penalty22 ); // catches the 2-2 case
		}
	}
}