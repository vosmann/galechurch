package hr.fer.zemris.ktlab.sap.algorithms.gc;

import java.util.Iterator;
import java.util.List;
import hr.fer.zemris.ktlab.sap.util.DataModel;
import hr.fer.zemris.ktlab.sap.algorithms.gc.Alignment;
import hr.fer.zemris.ktlab.sap.algorithms.gc.ProbabilityComputation;

public class ParagraphAligner {
	
	/** The <code>Aligner</code> object that instanced this <code>ParagraphAligner</code> */
	private Aligner sentenceAligner;
	
	/** The <code>DataModel</code> object used for sentence alignment. */
	private DataModel originalDataModel;
	
	/** The temporary <code>DataModel</code> object used as help in paragraph alignment. */ 
	private DataModel paragraphDataModel;
	
	/** 
	 * Number of elements (paragraphs) in the text in the source language
	 * (L1). 
	 */
	private int numberOfElements1;
	/** 
	 * Number of elements (paragraphs) in the translation to the 
	 * destination language (L2). 
	 */
	private int numberOfElements2;
	
	/**
	 * Holds the number of the paragraph in the source language currently being
	 * aligned. Because paragraphs are viewed as sentences, all Elements have the
	 * same value of the paragraph field.
	 */
	private int currentParagraph1;
	/**
	 * Holds the number of the paragraph in the destination language currently 
	 * being aligned. Because paragraphs are viewed as sentences, all Elements have the
	 * same value of the paragraph field.
	 */
	private int currentParagraph2;
	
	/** Holds the position of the <code>Element</code> object currently being
	 *  examined in the list of sentences in the source language. 
	 */
	private int currentElement1;
	
	/** Holds the position of the <code>Element</code> object currently being
	 *  examined in the list of sentences in the destination language. 
	 */
	private int currentElement2;
	
	/** A reference to the keys of the <code>Elements</code> in the source
	 * 	language in the original DataModel (contains sentences in each <code>Element</code>).
	 */
	private List<Integer> originalKeys1;
	
	/** A reference to the keys of the <code>Elements</code> in the destination
	 * 	language in the original DataModel (contains sentences in each <code>Element</code>).
	 */
	private List<Integer> originalKeys2;
	
	/** A reference to the keys of the <code>Elements</code> in the source
	 * 	language in the paragraph DataModel (contains paragraphs in each <code>Element</code>).
	 */
	private List<Integer> keys1;
	
	/** A reference to the keys of the <code>Elements</code> in the destination
	 * 	language in the paragraph DataModel (contains paragraphs in each <code>Element</code>).
	 */
	private List<Integer> keys2;
	
	/** Points to the next paragraph in the source language in the paragraph alignment process. */ 
	private int elementPointer1;
	
	/** Points to the next paragraph in the destination language in the paragraph alignment process. */
	private int elementPointer2;
	
	/** Points to the paragraph being traversed in a single alignment in the source language. */
	private int paragraphPointer1;
	
	/** Points to the paragraph being traversed in a single alignment in the destination language. */
	private int paragraphPointer2;
	
	/**
	 * The public constructor for the ParagraphAligner class. 
	 * @param sentenceAligner
	 * 		The <code>Aligner</code> object for which this <code>ParagraphAligner</code> is being instanced.
	 */
	public ParagraphAligner(Aligner sentenceAligner) {
		super();
		this.sentenceAligner = sentenceAligner;
		// Setting up the info on the original DataModel
		this.originalDataModel = sentenceAligner.getDataModel();
		this.originalKeys1 = originalDataModel.getKeys1();
		this.originalKeys2 = originalDataModel.getKeys2();
		
		// Setting up the new, paragraph DataModel
		this.paragraphDataModel = new DataModel(); // Elements in this DataModel object will hold paragraphs instead of sentences.
		Integer paragraphLength = new Integer(0);
		Integer paragraphNumber = new Integer(0);
		int currentParagraph = originalDataModel.getParagraphForElement(originalKeys1.get(0));
		Iterator<Integer> iter = originalKeys1.iterator();
		int key = 0;
		
		while ( iter.hasNext() ) {
			
			key = iter.next();
			if ( originalDataModel.getParagraphForElement(key) != currentParagraph) {
				currentParagraph = originalDataModel.getParagraphForElement(key);
				paragraphDataModel.add1(paragraphLength.toString()+"-"+paragraphNumber.toString(), 0);
				paragraphLength = 0;
			}
			paragraphNumber = originalDataModel.getParagraphForElement(key); // Remember the number of the paragraph whose length is being summed up.
			paragraphLength += originalDataModel.getElement(key).length();
			
		}
		paragraphDataModel.add1(paragraphLength.toString()+"-"+paragraphNumber.toString(), 0);
		
		paragraphLength = 0;
		paragraphNumber = 0;
		
		currentParagraph = originalDataModel.getParagraphForElement(originalKeys2.get(0));
		iter = originalKeys2.iterator();
		while ( iter.hasNext() ) {
			key = iter.next();
			if ( originalDataModel.getParagraphForElement(key) != currentParagraph) {
				currentParagraph = originalDataModel.getParagraphForElement(key);
				paragraphDataModel.add2(paragraphLength.toString()+"-"+paragraphNumber.toString(), 0);
				paragraphLength = 0;
			}
			paragraphNumber = originalDataModel.getParagraphForElement(key); // Remember the number of the paragraph whose length is being summed up.
			paragraphLength += originalDataModel.getElement(key).length();
		}
		paragraphDataModel.add2(paragraphLength.toString()+"-"+paragraphNumber.toString(), 0);
		// paragraphDataModel is complete from here on.
		
		this.keys1 = paragraphDataModel.getKeys1();
		this.keys2 = paragraphDataModel.getKeys2();
		this.numberOfElements1 = this.keys1.size();
		this.numberOfElements2 = this.keys2.size();
		this.currentElement1 = 0;
		this.currentElement2 = 0;
		this.currentParagraph1 = paragraphDataModel.getParagraphForElement(keys1.get(currentElement1));
		this.currentParagraph2 = paragraphDataModel.getParagraphForElement(keys2.get(currentElement2));
		this.elementPointer1 = originalKeys1.size()-1; // The last element 
		this.elementPointer2 = originalKeys2.size()-1; // The last element
		this.paragraphPointer1 = originalDataModel.getParagraphForElement( originalKeys1.get(elementPointer1) ); // The last paragraph
		this.paragraphPointer2 = originalDataModel.getParagraphForElement( originalKeys2.get(elementPointer2) ); // The last paragraph
	}

	/**
	 * Writes the alignment into the <code>DataModel</code> object being aligned by changing 
	 * the necessary paragraph numbers (identifiers). 
	 * @param align
	 * 		The paragraph alignment to be made. 
	 */
	private void setParagraphConnection( Alignment align ) {
		if ( (align.x2 == -1) && (align.y2 == -1) ) {
			
			if (align.y1 == -1) { /* Deletion (10) makes no connections in the DataModel */
				// Add paragraph to the list of paragraphs that won't be sentence-aligned
				sentenceAligner.addDeletedParagraph( getParagraphNumber( paragraphDataModel.getElement(align.x1) ) );
				// Traverse the paragraph in the source language without making any modifications to the paragraph numbers
				while ( (elementPointer1>-1)&&(paragraphPointer1==originalDataModel.getParagraphForElement( originalKeys1.get(elementPointer1) )) ) {
					elementPointer1--;
				}
				if (elementPointer1>-1) paragraphPointer1 = originalDataModel.getParagraphForElement( originalKeys1.get(elementPointer1) );
				
			}
			else if (align.x1 == -1) { /* Insertion (01) makes no connection in the DataModel */
				// Add paragraph to the list of paragraphs that won't be sentence-aligned
				sentenceAligner.addInsertedParagraph( getParagraphNumber( paragraphDataModel.getElement(align.y1) ) );
				// Traverse the paragraph in the destination language without making any modifications to the paragraph numbers. 
				while ( (elementPointer2>-1)&&(paragraphPointer2==originalDataModel.getParagraphForElement( originalKeys2.get(elementPointer2) )) ) {
					elementPointer2--;
				}
				if (elementPointer2>-1) paragraphPointer2 = originalDataModel.getParagraphForElement( originalKeys2.get(elementPointer2) ); 
			}
			else { /* Substitution (11) */
				// SOURCE LANGUAGE
				// Traverse the paragraph in the source language without making any modifications to the paragraph numbers
				while ( (elementPointer1>-1)&&(paragraphPointer1==originalDataModel.getParagraphForElement( originalKeys1.get(elementPointer1) )) ) {
					elementPointer1--;
				}
				if (elementPointer1>-1) paragraphPointer1 = originalDataModel.getParagraphForElement( originalKeys1.get(elementPointer1) );
				
				// DESTINATION LANGUAGE
				// Traverse the paragraph in the destination language without making any modifications to the paragraph numbers. 
				while ( (elementPointer2>-1)&&(paragraphPointer2==originalDataModel.getParagraphForElement( originalKeys2.get(elementPointer2) )) ) {
					elementPointer2--;
				}
				if (elementPointer2>-1) paragraphPointer2 = originalDataModel.getParagraphForElement( originalKeys2.get(elementPointer2) ); 
			}
		} else {
			
			if (align.x2 == -1) { /* Expansion (12) */
				// SOURCE LANGUAGE
				int toOverwriteWith = 0;
				// Traverse the paragraph in the source language without making any modifications to the paragraph numbers.
				while ( (elementPointer1>-1)&&(originalDataModel.getParagraphForElement( originalKeys1.get(elementPointer1) )==paragraphPointer1) ) {
					elementPointer1--;  // the condition above works because of Java's  short circuit mechanism in evaluating logical expressions
				}
				if (elementPointer1>-1) paragraphPointer1 = originalDataModel.getParagraphForElement(originalKeys1.get(elementPointer1));
				
				// DESTINATION LANGUAGE
				toOverwriteWith = paragraphPointer2;
				// Traverse the second paragraph in the destination language in this alignment
				while ( originalDataModel.getParagraphForElement( originalKeys2.get(elementPointer2) )==paragraphPointer2 ) {
					elementPointer2--;
				}
				
				if (elementPointer2>-1) paragraphPointer2 = originalDataModel.getParagraphForElement(originalKeys2.get(elementPointer2)); 
				
				// Finally, change the paragraph numbers into the paragraph number of the next paragraph
				while ( (elementPointer2>-1)&&(originalDataModel.getParagraphForElement( originalKeys2.get(elementPointer2) )==paragraphPointer2) ) {
					originalDataModel.setParagraphForElement(originalKeys2.get(elementPointer2), toOverwriteWith);
					elementPointer2--;
				}
				if (elementPointer2>-1) paragraphPointer2 = originalDataModel.getParagraphForElement(originalKeys2.get(elementPointer2));
				
			} else if (align.y2 == -1) { /* Contraction (21) */
				// SOURCE LANGUAGE
				int toOverwriteWith = paragraphPointer1;
				// Traverse the paragraph in the source language without making any modifications to the paragraph numbers.
				while ( originalDataModel.getParagraphForElement( originalKeys1.get(elementPointer1) )==paragraphPointer1 ) {
					elementPointer1--;  // the condition above works because of Java's  short circuit mechanism in evaluating logical expressions
				}
				 
				if (elementPointer1>-1) paragraphPointer1 = originalDataModel.getParagraphForElement(originalKeys1.get(elementPointer1));
				
				// Finally, change the paragraph numbers into the paragraph number of the next paragraph
				while ( (elementPointer1>-1)&&(originalDataModel.getParagraphForElement( originalKeys1.get(elementPointer1) )==paragraphPointer1) ) {
					originalDataModel.setParagraphForElement(originalKeys1.get(elementPointer1), toOverwriteWith);
					elementPointer1--;
				}
				if (elementPointer1>-1) paragraphPointer1 = originalDataModel.getParagraphForElement(originalKeys1.get(elementPointer1));
				
				
				// DESTINATION LANGUAGE
				// Traverse the second paragraph in the destination language in this alignment
				while ( (elementPointer2>-1)&&(originalDataModel.getParagraphForElement( originalKeys2.get(elementPointer2) )==paragraphPointer2) ) {
					elementPointer2--;
				}
				if (elementPointer2>-1) paragraphPointer2 = originalDataModel.getParagraphForElement(originalKeys2.get(elementPointer2));
				
			} else { /* Merger (22) */
				int toOverwriteWith = 0;
				
				// SOURCE LANGUAGE
				// Traverse the paragraph in the source language without making any modifications to the paragraph numbers.
				while ( originalDataModel.getParagraphForElement( originalKeys1.get(elementPointer1) )==paragraphPointer1 ) {
					elementPointer1--;  // the condition above works because of Java's  short circuit mechanism in evaluating logical expressions
				}
				if (elementPointer1>-1) paragraphPointer1 = originalDataModel.getParagraphForElement(originalKeys1.get(elementPointer1));
				// Finally, change the paragraph numbers into the paragraph number of the next paragraph
				while ( (elementPointer1>-1)&&(originalDataModel.getParagraphForElement( originalKeys1.get(elementPointer1) )==paragraphPointer1) ) {
					originalDataModel.setParagraphForElement(originalKeys1.get(elementPointer1), toOverwriteWith);
					elementPointer1--;
				}
				if (elementPointer1>-1) paragraphPointer1 = originalDataModel.getParagraphForElement(originalKeys1.get(elementPointer1));
				
				// DESTINATION LANGUAGE
				toOverwriteWith = paragraphPointer2;
				// Traverse the second paragraph in the destination language in this alignment
				while ( originalDataModel.getParagraphForElement( originalKeys2.get(elementPointer2) )==paragraphPointer2 ) {
					elementPointer2--;
				}
				if (elementPointer2>-1) paragraphPointer2 = originalDataModel.getParagraphForElement(originalKeys2.get(elementPointer2));
				// Finally, change the paragraph numbers into the paragraph number of the next paragraph
				while ( (elementPointer2>-1)&&(originalDataModel.getParagraphForElement( originalKeys2.get(elementPointer2) )==paragraphPointer2) ) {
					originalDataModel.setParagraphForElement(originalKeys2.get(elementPointer2), toOverwriteWith);
					elementPointer2--;
				}
				if (elementPointer2>-1) paragraphPointer2 = originalDataModel.getParagraphForElement(originalKeys2.get(elementPointer2));
			}
		}
	}
	
	/** 
	 * Initializes a two-dimensional array of references to point to 
	 * newly created, empty <code>Alignment</code> objects. 
	 * @param array
	 * 		The array to be initialized.
	 * @param rows
	 * 		The first index of a two-dimensional array.
	 * @param columns
	 * 		The second index of a two-dimensional array.
	 */
	private void initializeAlignmentArray(Alignment array[][], int rows, int columns) {
		for (int i=0; i<rows; ++i) {
			for (int j=0; j<columns; ++j) {
				array[i][j] = new Alignment();
			}
		}
	}

	/** Returns the paragraph length stored in the String in an <code>Element</code> object. */
	public static Integer getParagraphLength(String string) {
		return new Integer(Integer.parseInt( string.split("-")[0] ));
	}
	
	/** Returns the paragraph number stored in the String in an <code>Element</code> object. */
	public static Integer getParagraphNumber(String string) {
		return new Integer(Integer.parseInt( string.split("-")[1] ));
	}
	
	/** 
	 * Aligns paragraphs in the <code>DataModel</code> object referred to by the enclosing
	 * <code>Aligner</code> object.
	 */
	public void alignParagraphOriented() {

		int currentParagraphStart1 = 0;
		int currentParagraphStart2 = 0;
		int currentParagraphSize1 = 0;
		int currentParagraphSize2 = 0;
		
		// This loop aligns the whole DataModel object. It assumes an equal number of paragraphs in both languages.
		// All additional paragraphs (paragraphs without pairs in the other language) are ignored.
		do {
			
			currentParagraph1 = paragraphDataModel.getParagraphForElement(paragraphDataModel.getKeys1().get(currentElement1));
			currentParagraphStart1 = currentElement1;
			currentParagraphSize1 = 0;
			// Counts the number of elements in the current paragraph (source language).
			while ((currentElement1 < numberOfElements1)&&(paragraphDataModel.getParagraphForElement(paragraphDataModel.getKeys1().get(currentElement1))==currentParagraph1)) {
				currentParagraphSize1++;
				currentElement1++;
			}
			
			currentParagraph2 = paragraphDataModel.getParagraphForElement(paragraphDataModel.getKeys2().get(currentElement2));
			currentParagraphStart2 = currentElement2;
			currentParagraphSize2 = 0;
			// Counts the number of elements in the current paragraph (destination language).
			while ((currentElement2 < numberOfElements2)&&(paragraphDataModel.getParagraphForElement(paragraphDataModel.getKeys2().get(currentElement2))==currentParagraph2)) {
				currentParagraphSize2++;
				currentElement2++;
			}
			
			Alignment[][] distance = new Alignment[currentParagraphSize1+1][currentParagraphSize2+1];
			initializeAlignmentArray(distance, currentParagraphSize1+1, currentParagraphSize2+1);
						
			for (int i=0; i<=currentParagraphSize1; ++i) {
				for (int j=0; j<=currentParagraphSize2; ++j) {
					
					// Finding costs (distances) for all currently possible alignments
					int D1 = (i>0 && j>0)    // 1-1 alignment (substitution)
							 ? 
							 ( distance[i-1][j-1].cost + ProbabilityComputation.d(getParagraphLength( paragraphDataModel.getElement(keys1.get(currentParagraphStart1+i-1)) ),
									 										      getParagraphLength( paragraphDataModel.getElement(keys2.get(currentParagraphStart2+j-1)) ), 
									 											  11) )
							 : Integer.MAX_VALUE;
							 
					int D2 = (i>0)  			// 1-0 alignment (deletion) 
							 ? 
							 ( distance[i-1][j].cost + ProbabilityComputation.d(getParagraphLength( paragraphDataModel.getElement(keys1.get(currentParagraphStart1+i-1)) ),
									 											0, 
									 											10) )
							 : Integer.MAX_VALUE;
							 
					int D3 = (j>0)  			// 0-1 alignment (insertion) 
							 ? 
							 ( distance[i][j-1].cost + ProbabilityComputation.d(0,
									 											getParagraphLength(paragraphDataModel.getElement(keys2.get(currentParagraphStart2+j-1)) ), 
									 											01) )
							 : Integer.MAX_VALUE;
							 
					int D4 = (i>1 && j>0)  			// 2-1 alignment (contraction) 
							 ? 
							 ( distance[i-2][j-1].cost + ProbabilityComputation.d( getParagraphLength( paragraphDataModel.getElement(keys1.get(currentParagraphStart1+i-2)) )
									 											  +getParagraphLength( paragraphDataModel.getElement(keys1.get(currentParagraphStart1+i-1)) ),
									 											   getParagraphLength( paragraphDataModel.getElement(keys2.get(currentParagraphStart2+j-1)) ), 
									  											  21) )
							 : Integer.MAX_VALUE;
							 
					int D5 = (i>0 && j>1)  			// 1-2 alignment (expansion) 
							 ? 
							 ( distance[i-1][j-2].cost + ProbabilityComputation.d( getParagraphLength( paragraphDataModel.getElement(keys1.get(currentParagraphStart1+i-1)) ),
									 											   getParagraphLength( paragraphDataModel.getElement(keys2.get(currentParagraphStart2+j-2)) )	
									 											  +getParagraphLength( paragraphDataModel.getElement(keys2.get(currentParagraphStart2+j-1)) ), 
									  											  12) )
							 : Integer.MAX_VALUE;
							 
					int D6 = (i>1 && j>1)  			// 2-2 alignment (merger) 
							 ? 
							 ( distance[i-2][j-2].cost + ProbabilityComputation.d( getParagraphLength( paragraphDataModel.getElement(keys1.get(currentParagraphStart1+i-2)) )
									 											  +getParagraphLength( paragraphDataModel.getElement(keys1.get(currentParagraphStart1+i-1)) ),
									 											   getParagraphLength( paragraphDataModel.getElement(keys2.get(currentParagraphStart2+j-2)) )	
									 											  +getParagraphLength( paragraphDataModel.getElement(keys2.get(currentParagraphStart2+j-1)) ), 
									  											  22) )
							 : Integer.MAX_VALUE;
							 
					// Finding the smallest cost (distance). 
					int Dmin = D1;
					if (D2<Dmin) Dmin = D2;
					if (D3<Dmin) Dmin = D3;
					if (D4<Dmin) Dmin = D4;
					if (D5<Dmin) Dmin = D5;
					if (D6<Dmin) Dmin = D6;
					
					// Setting up the best alignment.
					distance[i][j].cost = Dmin;
					if (Dmin == Integer.MAX_VALUE) {
						distance[i][j].cost = 0; // Necessary only for the [0][0] element of the array?
					} else if (Dmin == D1) {  // Substitution
						distance[i][j].cheapestPredecessor = distance[i-1][j-1];
						distance[i][j].x1 = paragraphDataModel.getKeys1().get(currentParagraphStart1+i-1);
						distance[i][j].y1 = paragraphDataModel.getKeys2().get(currentParagraphStart2+j-1);
					} else if (Dmin == D2) {  // Deletion
						distance[i][j].cheapestPredecessor = distance[i-1][j];
					  	distance[i][j].x1 = paragraphDataModel.getKeys1().get(currentParagraphStart1+i-1);
					} else if (Dmin == D3) {  // Insertion
						distance[i][j].cheapestPredecessor = distance[i][j-1];
					  	distance[i][j].y1 = paragraphDataModel.getKeys2().get(currentParagraphStart2+j-1);
					} else if (Dmin == D4) {  // Contraction
						distance[i][j].cheapestPredecessor = distance[i-2][j-1];
						distance[i][j].x1 = paragraphDataModel.getKeys1().get(currentParagraphStart1+i-2);
						distance[i][j].x2 = paragraphDataModel.getKeys1().get(currentParagraphStart1+i-1);
						distance[i][j].y1 = paragraphDataModel.getKeys2().get(currentParagraphStart2+j-1);
					} else if (Dmin == D5) {  // Expansion
						distance[i][j].cheapestPredecessor = distance[i-1][j-2];
						distance[i][j].x1 = paragraphDataModel.getKeys1().get(currentParagraphStart1+i-1);
						distance[i][j].y1 = paragraphDataModel.getKeys2().get(currentParagraphStart2+j-2);
						distance[i][j].y2 = paragraphDataModel.getKeys2().get(currentParagraphStart2+j-1);
					} else /* Dmin == D6 */ {  // Merger
						distance[i][j].cheapestPredecessor = distance[i-2][j-2];
						distance[i][j].x1 = paragraphDataModel.getKeys1().get(currentParagraphStart1+i-2);
						distance[i][j].x2 = paragraphDataModel.getKeys1().get(currentParagraphStart1+i-1);
						distance[i][j].y1 = paragraphDataModel.getKeys2().get(currentParagraphStart2+j-2);
						distance[i][j].y2 = paragraphDataModel.getKeys2().get(currentParagraphStart2+j-1);
					}
					
				}
			}
			// Transcribing the alignments from the dynamic programming framework to the DataModel object. 
			Alignment roadBackHome = distance[currentParagraphSize1][currentParagraphSize2];
			
			while ( roadBackHome.cheapestPredecessor != null ) {
				setParagraphConnection(roadBackHome);
				roadBackHome = roadBackHome.cheapestPredecessor;
			}
			
		} while ( (currentElement1 < numberOfElements1)&&(currentElement2 < numberOfElements2) );

	}

//	
}