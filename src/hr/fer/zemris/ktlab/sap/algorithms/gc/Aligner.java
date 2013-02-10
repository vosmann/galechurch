package hr.fer.zemris.ktlab.sap.algorithms.gc;

import java.util.LinkedList;
import java.util.List;
import hr.fer.zemris.ktlab.sap.util.DataModel;
import hr.fer.zemris.ktlab.sap.algorithms.gc.Alignment;
import hr.fer.zemris.ktlab.sap.algorithms.gc.ProbabilityComputation;

public class Aligner {
	
	/** The <code>DataModel</code> object being aligned. */
	private DataModel dm;
	
	/** 
	 * Number of elements (usually sentences) in the text in the source language
	 * (L1). 
	 */
	private int numberOfElements1;
	/** 
	 * Number of elements (usually sentences) in the translation to the 
	 * destination language (L2). 
	 */
	private int numberOfElements2;
	
	/**
	 * Holds the number of the paragraph in the source language currently being
	 * aligned.
	 */
	private int currentParagraph1;
	/**
	 * Holds the number of the paragraph in the destination language currently 
	 * being aligned.
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
	 * 	language.
	 */
	private List<Integer> keys1;
	
	/** A reference to the keys of the <code>Elements</code> in the destination
	 * 	language.
	 */
	private List<Integer> keys2;
	
	/** Holds the paragraph numbers (identifiers) of all paragraphs deleted from the source language. */ 
	private List<Integer> paragraphsDeleted;
	
	/** Holds the paragraph numbers (identifiers) of all paragraphs inserted to the destination language. */
	private List<Integer> paragraphsInserted;
	
	/** Is zero until sentence alignment is completed. Used to prevent paragraph alignment in case sentence
	 *  alignment has already been done. */ 
	private int sentenceAlignmentDone;
	
	/**
	 * The public constructor for the Aligner class. 
	 * @param dm
	 * 		The DataModel object for which the Aligner is being instanced.
	 */
	public Aligner(DataModel dm) {
		super();
		this.dm = dm;
		this.keys1 = dm.getKeys1();
		this.keys2 = dm.getKeys2();
		this.numberOfElements1 = this.keys1.size();
		this.numberOfElements2 = this.keys2.size();
		this.currentElement1 = 0;
		this.currentElement2 = 0;
		this.currentParagraph1 = dm.getParagraphForElement(keys1.get(currentElement1));
		this.currentParagraph2 = dm.getParagraphForElement(keys2.get(currentElement2));
		this.paragraphsDeleted = new LinkedList<Integer>();
		this.paragraphsInserted = new LinkedList<Integer>();
		this.sentenceAlignmentDone = 0;
	}

	/** Returns a reference to the <code>DataModel</code> being sentence aligned by this <code>Aligner</code>. */
	public DataModel getDataModel() {
		return this.dm;
	}
	
	/**
	 * Adds a paragraph number (identifier) to the list of paragraphs omitted in the translation.
	 * @param aDeletedParagraph
	 * 		The integer representing a paragraph number (identifier)
	 */
	public void addDeletedParagraph(int aDeletedParagraph) {
		paragraphsDeleted.add(aDeletedParagraph);
	}
	
	/**
	 * Adds a paragraph number (identifier) to the list of paragraphs inserted in the translation, without
	 * a corresponding paragraph in the source language.
	 * @param anInsertedParagraph
	 * 		The integer representing a paragraph number (identifier)
	 */
	public void addInsertedParagraph(int anInsertedParagraph) {
		paragraphsInserted.add(anInsertedParagraph);
	}
	
	/** 
	 * Writes the alignment into the <code>DataModel</code> object being aligned using its 
	 * built-in methods for adding connections. 
	 * @param align
	 * 		The sentence alignment to be transcribed into the <code>DataModel</code> object.
	 */
	private void setDataModelConnection( Alignment align ) {
		// Prostor za optimizaciju? Prvo ispitivati najvjerovatnije sravnjenje (11)?
		if ( (align.x2 == -1) && (align.y2 == -1) ) {
			if (align.y1 == -1) {
				return; /* Deletion (10) makes no connections in the DataModel */
			}
			else if (align.x1 == -1) {
				return; /* Insertion (01) makes no connection in the DataModel */
			}
			else { /* Substitution (11) */
				dm.addConnection(align.x1, align.y1);
			}
		} else {
			if (align.x2 == -1) { /* Expansion (12) */
				dm.addConnection(align.x1, align.y1);
				dm.addConnection(align.x1, align.y2);
			} else if (align.y2 == -1) { /* Contraction (21) */
				dm.addConnection(align.x1, align.y1);
				dm.addConnection(align.x2, align.y1);
			} else { /* Merger (22) */
				dm.addConnection(align.x1, align.y1);
				dm.addConnection(align.x1, align.y2);
				dm.addConnection(align.x2, align.y1);
				dm.addConnection(align.x2, align.y2);
			}
		}
	}

	
	/** 
	 * Initializes a two-dimensional array of references to point to newly created <code>Alignment</code> objects. 
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
	
	/**
	 * Aligns the elements (sentences) inside all corresponding paragraphs. Assumes an equal number of paragraphs
	 * in each language. Additional paragraphs are ignored. 
	 */
	public long alignSentences() {
		// The beginning of the execution time measurement
		long startTime = System.currentTimeMillis();
		
		
		int currentParagraphStart1 = 0;
		int currentParagraphStart2 = 0;
		int currentParagraphSize1 = 0;
		int currentParagraphSize2 = 0;
		
		// This loop aligns the whole DataModel object. It assumes an equal number of paragraphs in both languages.
		// All additional paragraphs (paragraphs without pairs in the other language) are ignored.
		do {
			
			// Search for a paragraph that wasn't deleted in the destination language.
			do {
				currentParagraph1 = dm.getParagraphForElement(keys1.get(currentElement1));
				currentParagraphStart1 = currentElement1;
				currentParagraphSize1 = 0;
				// Counts the number of elements in the current paragraph (source language).
				while ((currentElement1 < numberOfElements1)&&(dm.getParagraphForElement(keys1.get(currentElement1))==currentParagraph1)) {
					currentParagraphSize1++;
					currentElement1++;
				}
			} while ( paragraphsDeleted.contains(currentParagraph1) );
			
			// Search for a paragraph that wasn't inserted in the destination language.
			do {
				currentParagraph2 = dm.getParagraphForElement(keys2.get(currentElement2));
				currentParagraphStart2 = currentElement2;
				currentParagraphSize2 = 0;
				// Counts the number of elements in the current paragraph (destination language).
				while ((currentElement2 < numberOfElements2)&&(dm.getParagraphForElement(keys2.get(currentElement2))==currentParagraph2)) {
					currentParagraphSize2++;
					currentElement2++;
				}	
			} while ( paragraphsInserted.contains(currentParagraph2) );
			
			Alignment[][] distance = new Alignment[currentParagraphSize1+1][currentParagraphSize2+1];
			initializeAlignmentArray(distance, currentParagraphSize1+1, currentParagraphSize2+1);
						
			for (int i=0; i<=currentParagraphSize1; ++i) {
				for (int j=0; j<=currentParagraphSize2; ++j) {
					
					// Finding costs (distances) for all currently possible alignments
					int D1 = (i>0 && j>0)    // 1-1 alignment (substitution)
							 ? 
							 ( distance[i-1][j-1].cost + ProbabilityComputation.d(dm.getElement(keys1.get(currentParagraphStart1+i-1)).length(),
									 											  dm.getElement(keys2.get(currentParagraphStart2+j-1)).length(), 
									 											  11) )
							 : Integer.MAX_VALUE;
							 
					int D2 = (i>0)  			// 1-0 alignment (deletion) 
							 ? 
							 ( distance[i-1][j].cost + ProbabilityComputation.d(dm.getElement(keys1.get(currentParagraphStart1+i-1)).length(),
									 											0, 
									 											10) )
							 : Integer.MAX_VALUE;
							 
					int D3 = (j>0)  			// 0-1 alignment (insertion) 
							 ? 
							 ( distance[i][j-1].cost + ProbabilityComputation.d(0,
									 											dm.getElement(keys2.get(currentParagraphStart2+j-1)).length(), 
									 											01) )
							 : Integer.MAX_VALUE;
							 
					int D4 = (i>1 && j>0)  			// 2-1 alignment (contraction) 
							 ? 
							 ( distance[i-2][j-1].cost + ProbabilityComputation.d( dm.getElement(keys1.get(currentParagraphStart1+i-2)).length()
									 											  +dm.getElement(keys1.get(currentParagraphStart1+i-1)).length(),
									  											   dm.getElement(keys2.get(currentParagraphStart2+j-1)).length(), 
									  											  21) )
							 : Integer.MAX_VALUE;
							 
					int D5 = (i>0 && j>1)  			// 1-2 alignment (expansion) 
							 ? 
							 ( distance[i-1][j-2].cost + ProbabilityComputation.d( dm.getElement(keys1.get(currentParagraphStart1+i-1)).length(),
									 											   dm.getElement(keys2.get(currentParagraphStart2+j-2)).length()	
									 											  +dm.getElement(keys2.get(currentParagraphStart2+j-1)).length(), 
									  											  12) )
							 : Integer.MAX_VALUE;
							 
					int D6 = (i>1 && j>1)  			// 2-2 alignment (merger) 
							 ? 
							 ( distance[i-2][j-2].cost + ProbabilityComputation.d( dm.getElement(keys1.get(currentParagraphStart1+i-2)).length()
									 											  +dm.getElement(keys1.get(currentParagraphStart1+i-1)).length(),
									 											   dm.getElement(keys2.get(currentParagraphStart2+j-2)).length()	
									 											  +dm.getElement(keys2.get(currentParagraphStart2+j-1)).length(), 
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
						distance[i][j].x1 = keys1.get(currentParagraphStart1+i-1);
						distance[i][j].y1 = keys2.get(currentParagraphStart2+j-1);
					} else if (Dmin == D2) {  // Deletion
						distance[i][j].cheapestPredecessor = distance[i-1][j];
					  	distance[i][j].x1 = keys1.get(currentParagraphStart1+i-1);
					} else if (Dmin == D3) {  // Insertion
						distance[i][j].cheapestPredecessor = distance[i][j-1];
					  	distance[i][j].y1 = keys2.get(currentParagraphStart2+j-1);
					} else if (Dmin == D4) {  // Contraction
						distance[i][j].cheapestPredecessor = distance[i-2][j-1];
						distance[i][j].x1 = keys1.get(currentParagraphStart1+i-2);
						distance[i][j].x2 = keys1.get(currentParagraphStart1+i-1);
						distance[i][j].y1 = keys2.get(currentParagraphStart2+j-1);
					} else if (Dmin == D5) {  // Expansion
						distance[i][j].cheapestPredecessor = distance[i-1][j-2];
						distance[i][j].x1 = keys1.get(currentParagraphStart1+i-1);
						distance[i][j].y1 = keys2.get(currentParagraphStart2+j-2);
						distance[i][j].y2 = keys2.get(currentParagraphStart2+j-1);
					} else /* Dmin == D6 */ {  // Merger
						distance[i][j].cheapestPredecessor = distance[i-2][j-2];
						distance[i][j].x1 = keys1.get(currentParagraphStart1+i-2);
						distance[i][j].x2 = keys1.get(currentParagraphStart1+i-1);
						distance[i][j].y1 = keys2.get(currentParagraphStart2+j-2);
						distance[i][j].y2 = keys2.get(currentParagraphStart2+j-1);
					}
				}
			}
			// Transcribing the alignments from the dynamic programming framework to the DataModel object. 
			Alignment roadBackHome = distance[currentParagraphSize1][currentParagraphSize2];
			while ( roadBackHome.cheapestPredecessor != null ) {
				setDataModelConnection(roadBackHome);
				roadBackHome = roadBackHome.cheapestPredecessor;
			}
			
		} while ( (currentElement1 < numberOfElements1)&&(currentElement2 < numberOfElements2) );
		
		// Set the sentence alignment completion flag.
		this.sentenceAlignmentDone = 1;
		
		// The end of the execution measurement time
		long endTime = System.currentTimeMillis();

		return (endTime - startTime);
	}
	
	/**
	 * Aligns paragraphs. Paragraphs are aligned by concatenating paragraphs in 1-2, 2-1 and 2-2 alignments.
	 * Deleted (1-0) and inserted (0-1) paragraphs are remembered and skipped in the following sentence
	 * alignment. 
	 */
	public long alignParagraphs() {
		// The beginning of the execution time measurement
		long startTime = System.currentTimeMillis();
		
		
		// Paragraph alignment cannot be performed after sentence alignment.
		if (sentenceAlignmentDone!=0) return 0; 
		ParagraphAligner pa = new ParagraphAligner(this);
	
		pa.alignParagraphOriented();
		
		// The end of the execution time measurement
		long endTime = System.currentTimeMillis();
		return ( endTime - startTime );
	}
}