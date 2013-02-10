package hr.fer.zemris.ktlab.sap.util;

//import hr.fer.zemris.ktlab.sap.gui.Statistics;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

/**
 * Model u koji se spremaju elementi koji opisuju dijelove teksta (cijeli tekst,
 * odlomak, rečenica...). Kod dodavanja novog elementa u model, dodjeljuje mu se
 * jedinstveni identifikator (ključ) na temelju kojeg mu se kasnije može
 * pristupati.
 * <p>
 * Elementi su grupirani u dva skupa u kojima je očuvan poredak. U model se
 * također mogu dodati i veze između svakog elementa iz jednog skupa i elemenata
 * iz drugog skupa. Povezivanje elemenata iz istog skupa nije dopušteno.
 * <p>
 * Klasa ima podršku za slanje informacija o promjenama njezinih podataka pomoću
 * <code>PropertyChangeSupport</code> klase i podršku za slanje informacija o
 * događajima koji se mogu vratiti (eng. undo) ili ponoviti (eng. redo) pomoću
 * klase <code>UndoableEditSupport</code>.
 * 
 * @see Element
 * @see Bookmark
 * @see PropertyChangeSupport
 * @see UndoableEditSupport
 * 
 * @author Željko Rumenjak
 */
public class DataModel implements Serializable {

	private static final long serialVersionUID = 4959459526484640183L;

	/**
	 * Ime događaja koji se šalje kada se doda element u prvi skup modela.
	 * <code>oldValue</code> je null, <code>newValue</code> sadrži ključ
	 * dodanog elementa.
	 */
	public static final String ELEMENT_ADDED_IN_SET1 = "ElementAddedInSet1";

	/**
	 * Ime događaja koji se šalje kada se doda element u drugi skup modela.
	 * <code>oldValue</code> je null, <code>newValue</code> sadrži ključ
	 * dodanog elementa.
	 */
	public static final String ELEMENT_ADDED_IN_SET2 = "ElementAddedInSet2";

	/**
	 * Ime događaja koji se šalje kada se element umetne u prvi skup modela.
	 * <code>oldValue</code> je null, <code>newValue</code> je lista
	 * integera (List<Integer>) sa 2 elementa. Prvi sadrži redni broj mjesta na
	 * koje je element umetnut, a drugi ključ umetnutog elementa.
	 */
	public static final String ELEMENT_INSERTED_IN_SET1 = "ElementInsertedInSet1";

	/**
	 * Ime događaja koji se šalje kada se element umetne u drugi skup modela.
	 * <code>oldValue</code> je null, <code>newValue</code> je lista
	 * integera (List<Integer>) sa 2 elementa. Prvi sadrži redni broj mjesta na
	 * koje je element umetnut, a drugi ključ umetnutog elementa.
	 */
	public static final String ELEMENT_INSERTED_IN_SET2 = "ElementInsertedInSet2";

	/**
	 * Ime događaja koji se šalje kada se ukloni element iz prvog skupa modela.
	 * <code>oldValue</code> je sadrži ključ elementa koji je uklonjen iz
	 * modela, <code>newValue</code> je null.
	 */
	public static final String ELEMENT_REMOVED_FROM_SET1 = "ElementRemovedFromSet1";

	/**
	 * Ime događaja koji se šalje kada se ukloni element iz drugog skupa modela.
	 * <code>oldValue</code> je sadrži ključ elementa koji je uklonjen iz
	 * modela, <code>newValue</code> je null.
	 */
	public static final String ELEMENT_REMOVED_FROM_SET2 = "ElementRemovedFromSet2";

	/**
	 * Ime događaja koji se šalje kada se promijeni vrijednost nekog elementa iz
	 * prvog skupa. <code>oldValue</code> je null, a <code>newValue</code>
	 * je ključ elementa čija vrijednost se promijenila.
	 */
	public static final String ELEMENT_UPDATED_IN_SET1 = "ElementUpdatedInSet1";

	/**
	 * Ime događaja koji se šalje kada se promijeni vrijednost nekog elementa iz
	 * drugog skupa. <code>oldValue</code> je null, a <code>newValue</code>
	 * je ključ elementa čija vrijednost se promijenila.
	 */
	public static final String ELEMENT_UPDATED_IN_SET2 = "ElementUpdatedInSet2";

	/**
	 * Ime događaja koji se šalje kada se doda nova veza u model.
	 * <code>oldValue</code> je ključ prvog elementa iz veze, a
	 * <code>newValue</code> je ključ drugog elementa iz veze.
	 */
	public static final String CONNECTION_ADDED = "ConnectionAdded";

	/**
	 * Ime događaja koji se šalje kada se ukloni veza iz modela.
	 * <code>oldValue</code> je ključ prvog elementa iz veze, a
	 * <code>newValue</code> je ključ drugog elementa iz veze.
	 */
	public static final String CONNECTION_REMOVED = "ConnectionRemoved";

	/**
	 * Ime događaja koji se šalje kada se element iz prvog skupa podijeli na dva
	 * elementa. <code>oldValue</code> je ključ početnog elementa, a
	 * <code>newValue</code> je ključ novog elementa.
	 */
	public static final String ELEMENT_SPLIT_IN_SET1 = "ElementSplitInSet1";

	/**
	 * Ime događaja koji se šalje kada se element iz drugog skupa podijeli na
	 * dva elementa. <code>oldValue</code> je ključ početnog elementa, a
	 * <code>newValue</code> je ključ novog elementa.
	 */
	public static final String ELEMENT_SPLIT_IN_SET2 = "ElementSplitInSet2";

	/**
	 * Ime događaja koji se šalje kada se dva elementa iz prvog skupa spoje.
	 * <code>oldValue</code> je ključ elementa koji se uklanja iz modela, a
	 * <code>newValue</code> je ključ elementa koji sadrži oba stara elementa.
	 */
	public static final String ELEMENTS_COMBINED_IN_SET1 = "ElementsCombinedInSet1";

	/**
	 * Ime događaja koji se šalje kada se dva elementa iz drugog skupa spoje.
	 * <code>oldValue</code> je ključ elementa koji se uklanja iz modela, a
	 * <code>newValue</code> je ključ elementa koji sadrži oba stara elementa.
	 */
	public static final String ELEMENTS_COMBINED_IN_SET2 = "ElementsCombinedInSet2";

	/**
	 * Ime događaja koji se šalje kada se doda nova knjižna oznaka.
	 * <code>oldValue</code> je null, a <code>newValue</code> je dodana
	 * knjižna oznaka (klasa <code>Bookmark</code>).
	 */
	public static final String BOOKMARK_ADDED = "BookmarkAdded";

	/**
	 * Ime događaja koji se šalje kada se ukloni knjižna oznaka.
	 * <code>oldValue</code> je uklonjena knjižna oznaka (klasa
	 * <code>Bookmark</code>), a <code>newValue</code> je null.
	 */
	public static final String BOOKMARK_REMOVED = "BookmarkRemoved";

	/**
	 * Ime događaja koji se šalje kada se nad modelom pozove metoda
	 * <code>clear()</code>, odnosno kada se model potpuno isprazni.
	 */
	public static final String GLOBAL_CLEAR = "GlobalClear";

	/** Omogućava slanje obavijesti o promjenama u modelu */
	transient private PropertyChangeSupport propertyChangeSupport;

	/** Omogućava slanje undo i redo događaja */
	transient private UndoableEditSupport undoSupport;

	/** Mapa u koju se spremaju svi elementi iz modela */
	private Map<Integer, Element> elements;

	/**
	 * Lista sa ključevima koji pripadaju elementima prvog skupa. Koristi se
	 * zbog očuvanje poretka.
	 */
	private List<Integer> keys1;

	/**
	 * Lista sa ključevima koji pripadaju elementima drugog skupa. Koristi se
	 * zbog očuvanje poretka.
	 */
	private List<Integer> keys2;

	/**
	 * Varijabla u koju je spremljen ključ koji će dobiti sljedeći element koji
	 * će se dodati u model
	 */
	private int nextKey = 0;

	/** Lista svih knjižnih oznaka */
	private List<Bookmark> bookmarks;

	/** Atributi prepoznati u ulaznoj xml datoteci */
	private Set<String> xmlAttributes;

	/** Mapa u koju se mogu spremati svojstva vezana za projekt */
	private Map<String, String> properties;

	/**
	 * Sve akcije koje se izvode za vrijeme dok je ova varijable
	 * <code>true</code> grupiraju se u jedan <code>CompoundEdit</code>
	 */
	private boolean compoundActions = false;

	/**
	 * Varijabla koja se provjerava prije slanja poruka slušateljima. Ako je
	 * varijabla <code>false</code> poruka se ne šalje.
	 */
	private boolean notifyListeners = true;

	/**
	 * Varijabla u koju se spremaju sve izvršene akcije za vrijeme dok je
	 * varijabla <code>compoundActions</code> jednaka <code>true</code>
	 */
	private CompoundEdit compoundEdit;

//	/** Sadrži različite statističke podatke */
//	private Statistics statistics = null;

	/**
	 * Javni konstruktor klase.
	 */
	public DataModel() {
		initializeTransients();
		elements = new HashMap<Integer, Element>();
		keys1 = new LinkedList<Integer>();
		keys2 = new LinkedList<Integer>();
		bookmarks = new LinkedList<Bookmark>();
		xmlAttributes = new HashSet<String>();
		properties = new HashMap<String, String>();
	}

	/**
	 * Metoda za inicijalizaciju svih klasa koje su <code>transient</code>.
	 */
	public void initializeTransients() {
		propertyChangeSupport = new PropertyChangeSupport(this);
		undoSupport = new UndoableEditSupport();
	}

	/**
	 * Omogućava dodavanje klase koja će biti obaviještena svaki put kad se
	 * dogodi bilo kakva promjena u modelu.
	 * <p>
	 * Klasa će primati događaje:
	 * <ul>
	 * <li>ELEMENT_ADDED_IN_SET1
	 * <li>ELEMENT_ADDED_IN_SET2
	 * <li>ELEMENT_INSERTED_IN_SET1
	 * <li>ELEMENT_INSERTED_IN_SET2
	 * <li>ELEMENT_REMOVED_FROM_SET1
	 * <li>ELEMENT_REMOVED_FROM_SET2
	 * <li>ELEMENT_UPDATED_IN_SET1
	 * <li>ELEMENT_UPDATED_IN_SET2
	 * <li>CONNECTION_ADDED
	 * <li>CONNECTION_REMOVED
	 * <li>ELEMENT_SPLIT_IN_SET1
	 * <li>ELEMENT_SPLIT_IN_SET2
	 * <li>ELEMENTS_COMBINED_IN_SET1
	 * <li>ELEMENTS_COMBINED_IN_SET2
	 * <li>BOOKMARK_ADDED
	 * <li>BOOKMARK_REMOVED
	 * <li>GLOBAL_CLEAR
	 * 
	 * @param listener
	 *            klasa koja će se obavještavati o navedenim događajima
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Omogućava uklanjanje klase koja se obavještava o promjenama u modelu.
	 * 
	 * @param listener
	 *            klasa koja se uklanja
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Dodaje klasu koja će se obavještavati svaki put kada se u modelu dogodi
	 * promjena koju je moguće vratiti ili ponoviti.
	 * 
	 * @param listener
	 *            klasa koja će se obavještavati o undo/redo promjenama
	 */
	public void addUndoableEditListener(UndoableEditListener listener) {
		undoSupport.addUndoableEditListener(listener);
	}

	/**
	 * Uklanja klasu koja sluša undo/redo promjene.
	 * 
	 * @param listener
	 *            klasa koja se uklanja
	 */
	public void removeUndoableEditListener(UndoableEditListener listener) {
		undoSupport.removeUndoableEditListener(listener);
	}

	/**
	 * Metoda koja obavještava sve klase koje slušaju promjene na modelu da se
	 * dogodila promjena.
	 * 
	 * @param propertyName
	 *            ime svojstva na kojem se dogodila promjena
	 * @param oldValue
	 *            stara vrijednost svojstva
	 * @param newValue
	 *            nova vrijednost svojstva
	 */
	private void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {

		if (notifyListeners) {
			propertyChangeSupport.firePropertyChange(propertyName, oldValue,
					newValue);
		}
	}

	/**
	 * Uključuje ili isključuje slanje poruka slušateljima kada se dogodi
	 * promjena u modelu.
	 * 
	 * @param notify
	 *            ako je <code>true</code> slušatelji se obavješatvaju o
	 *            promjenama, ako je <code>false</code> slušatelji se ne
	 *            obavještavaju o promjenama
	 */
	public void setNotifyListeners(boolean notify) {
		this.notifyListeners = notify;
	}

	/**
	 * Vraća da li se slušatelji obavještavaju o promjenama u modelu ili ne.
	 * 
	 * @return <code>true</code> ako se slušatelji obavješatvaju o promjenama,
	 *         <code>false</code> ako se slušatelji ne obavještavaju o
	 *         promjenama
	 */
	public boolean getNotifyListenersStatus() {
		return notifyListeners;
	}

	/**
	 * Metoda obavještava sve klase koje slušaju da li se dogodila promjena koja
	 * se može vratiti da se takva promjena dogodila. Metoda uzima u obzir
	 * vrijednost varijable <code>compoundActions</code>, tj. ako je ta
	 * varijabla <code>true</code> metoda neće obavijestiti slušatelje, nego
	 * će dodati događaj u <code>compoundEdit</code> koji se šalje kada
	 * <code>compoundActions</code> postane <code>false</code>. Ako je
	 * <code>compoundActions</code> <code>false</code> tada se slušatelji
	 * odmah obavještavaju.
	 * 
	 * @param edit
	 *            događaj koji sadrži informacije o promjeni koja se dogodila i
	 *            podršku da se ta akcija poništi ili ponovi
	 */
	private void postEdit(UndoableEdit edit) {
		if (compoundActions) {
			compoundEdit.addEdit(edit);
		} else {
			undoSupport.postEdit(edit);
		}
	}

	/**
	 * Služi za spajanje više nezavisnih akcija u jednu akciju kako bi se ona
	 * mogla vratiti ili ponoviti samo jednim pozivom <code>undo</code> ili
	 * <code>redo</code> metode u odgovarajućoj klasi.
	 * <p>
	 * <b>Metoda se poziva na početku takve akcije, a na kraju takve akcije
	 * potrebno je pozvati metodu <code>stopCompoundAction</code>.</b>
	 * 
	 * @see DataModel#stopCompoudAction()
	 */
	public void startCompoundAction() {
		if (compoundActions) {
			throw new IllegalStateException(
					"Trying to start new compound action while the previous one has not ended");
		}
		compoundEdit = new CompoundEdit();
		compoundActions = true;
	}

	/**
	 * Služi za spajanje više nezavisnih akcija u jednu akciju kako bi se ona
	 * mogla vratiti ili ponoviti samo jednim pozivom <code>undo</code> ili
	 * <code>redo</code> metode u odgovarajućoj klasi.
	 * <p>
	 * <b>Metoda se poziva na kraju takve akcije, a na početku takve akcije
	 * potrebno je pozvati metodu <code>startCompoundAction</code>.</b>
	 * 
	 * @see DataModel#startCompoudAction()
	 */
	public void stopCompoudAction() {
		if (!compoundActions) {
			throw new IllegalStateException(
					"There is no active compound action to stop");
		}

		compoundEdit.end();
		undoSupport.postEdit(compoundEdit);
		compoundActions = false;
	}

	/**
	 * Dodaje novi element u prvi skup iz modela i vraća ključ koji mu je
	 * pridružen.
	 * 
	 * @param text
	 *            tekstualni element koji se dodaje u model
	 * @return ključ pomoću kojeg se može dohvatiti spremljeni element
	 */
	public int add1(String text) {
		int lastKey = getLastKey();
		int key = getNextKey();
		Element element = new Element(key, text, Element.SET1);
		elements.put(key, element);
		keys1.add(key);
		UndoableEdit edit = new AddEdit(lastKey, key);

		firePropertyChange(ELEMENT_ADDED_IN_SET1, null, key);
		postEdit(edit);

		return key;
	}

	/**
	 * Dodaje novi element u prvi skup iz modela i vraća ključ koji mu je
	 * pridružen.
	 * 
	 * @param text
	 *            tekstualni element koji se dodaje u model
	 * @param paragraph
	 *            broj odlomka u kojem se element nalazi
	 * @return ključ pomoću kojeg se može dohvatiti spremljeni element
	 */
	public int add1(String text, int paragraph) {
		int lastKey = getLastKey();
		int key = getNextKey();
		Element element = new Element(key, text, Element.SET1, paragraph);
		elements.put(key, element);
		keys1.add(key);
		UndoableEdit edit = new AddEdit(lastKey, key);

		firePropertyChange(ELEMENT_ADDED_IN_SET1, null, key);
		postEdit(edit);

		return key;
	}

	/**
	 * Dodaje novi element u prvi skup iz modela i vraća ključ koji mu je
	 * pridružen.
	 * 
	 * @param text
	 *            tekstualni element koji se dodaje u model
	 * @param paragraph
	 *            broj odlomka u kojem se element nalazi
	 * @param attributes
	 *            atributi pročitani iz ulazne datoteke
	 * @return ključ pomoću kojeg se može dohvatiti spremljeni element
	 */
	public int add1(String text, int paragraph, Map<String, String> attributes) {
		int lastKey = getLastKey();
		int key = getNextKey();
		Element element = new Element(key, text, Element.SET1, paragraph,
				attributes);
		elements.put(key, element);
		keys1.add(key);
		UndoableEdit edit = new AddEdit(lastKey, key);

		firePropertyChange(ELEMENT_ADDED_IN_SET1, null, key);
		postEdit(edit);

		return key;
	}

	/**
	 * Stvara novi element i umeće ga u prvi skup, na mjesto <code>index</code>.
	 * Za broj odlomka novog elementa postavlja se odlomak od elementa na čije
	 * mjesto on dolazi ili se taj broj ne postavlja ako broj odlomka tog
	 * elementa nije definiran.
	 * 
	 * @param index
	 *            mjesto na koje se element umeće
	 * @param text
	 *            tekstualni element koji se umeće u model
	 * @return ključ pomoću kojeg se može dohvatiti umetnuti element
	 */
	public int insertElement1(int index, String text) {
		int lastKey = getLastKey();
		int key = getNextKey();
		int paragraph = elements.get(keys1.get(index)).getParagraph();

		if (paragraph != -1) {
			elements.put(key, new Element(key, text, Element.SET1, paragraph));
		} else {
			elements.put(key, new Element(key, text, Element.SET1));
		}
		keys1.add(index, key);

		UndoableEdit edit = new InsertEdit(index, lastKey, key);

		firePropertyChange(ELEMENT_INSERTED_IN_SET1, null, constructPair(index,
				key));
		postEdit(edit);

		return key;
	}

	/**
	 * Dodaje novi element u drugi skup iz modela i vraća ključ koji mu je
	 * pridružen.
	 * 
	 * @param text
	 *            tekstualni element koji se dodaje u model
	 * @return ključ pomoću kojeg se može dohvatiti spremljeni element
	 */
	public int add2(String text) {
		int lastKey = getLastKey();
		int key = getNextKey();
		elements.put(key, new Element(key, text, Element.SET2));
		keys2.add(key);
		UndoableEdit edit = new AddEdit(lastKey, key);

		firePropertyChange(ELEMENT_ADDED_IN_SET2, null, key);
		postEdit(edit);

		return key;
	}

	/**
	 * Dodaje novi element u drugi skup iz modela i vraća ključ koji mu je
	 * pridružen.
	 * 
	 * @param text
	 *            tekstualni element koji se dodaje u model
	 * @param paragraph
	 *            broj odlomka u kojem se element nalazi
	 * @return ključ pomoću kojeg se može dohvatiti spremljeni element
	 */
	public int add2(String text, int paragraph) {
		int lastKey = getLastKey();
		int key = getNextKey();
		elements.put(key, new Element(key, text, Element.SET2, paragraph));
		keys2.add(key);
		UndoableEdit edit = new AddEdit(lastKey, key);

		firePropertyChange(ELEMENT_ADDED_IN_SET2, null, key);
		postEdit(edit);

		return key;
	}

	/**
	 * Dodaje novi element u drugi skup iz modela i vraća ključ koji mu je
	 * pridružen.
	 * 
	 * @param text
	 *            tekstualni element koji se dodaje u model
	 * @param paragraph
	 *            broj odlomka u kojem se element nalazi
	 * @param attributes
	 *            atributi pročitani iz ulazne datoteke
	 * @return ključ pomoću kojeg se može dohvatiti spremljeni element
	 */
	public int add2(String text, int paragraph, Map<String, String> attributes) {
		int lastKey = getLastKey();
		int key = getNextKey();
		elements.put(key, new Element(key, text, Element.SET2, paragraph,
				attributes));
		keys2.add(key);
		UndoableEdit edit = new AddEdit(lastKey, key);

		firePropertyChange(ELEMENT_ADDED_IN_SET2, null, key);
		postEdit(edit);

		return key;
	}

	/**
	 * Stvara novi element i umeće ga u drugi skup, na mjesto <code>index</code>.
	 * Za broj odlomka novog elementa postavlja se odlomak od elementa na čije
	 * mjesto on dolazi ili se taj broj ne postavlja ako broj odlomka tog
	 * elementa nije definiran.
	 * 
	 * @param index
	 *            mjesto na koje se element umeće
	 * @param text
	 *            tekstualni element koji se umeće u model
	 * @return ključ pomoću kojeg se može dohvatiti umetnuti element
	 */
	public int insertElement2(int index, String text) {
		int lastKey = getLastKey();
		int key = getNextKey();
		int paragraph = elements.get(keys2.get(index)).getParagraph();

		if (paragraph != -1) {
			elements.put(key, new Element(key, text, Element.SET2, paragraph));
		} else {
			elements.put(key, new Element(key, text, Element.SET2));
		}
		keys2.add(index, key);

		UndoableEdit edit = new InsertEdit(index, lastKey, key);

		firePropertyChange(ELEMENT_INSERTED_IN_SET2, null, constructPair(index,
				key));
		postEdit(edit);

		return key;
	}

	/**
	 * Postavlja vrijednost elementa kojemu je pridružen ključ <code>key</code>.
	 * 
	 * @param key
	 *            ključ elementa čija se vrijednost mijenja
	 * @param text
	 *            tekst elementa
	 * @return <code>true</code> ako je vrijednost uspješno promijenjena,
	 *         <code>false</code> inače
	 */
	public boolean setElement(int key, String text) {
		Element element = elements.get(key);
		if (element == null) {
			return false;
		}
		String oldText = element.getText();
		element.setText(text);

		UndoableEdit edit = new UpdateEdit(key, oldText);

		if (element.isInSet1()) {
			firePropertyChange(ELEMENT_UPDATED_IN_SET1, null, key);
		} else {
			firePropertyChange(ELEMENT_UPDATED_IN_SET2, null, key);
		}

		postEdit(edit);

		return true;
	}

	/**
	 * Uklanja element iz modela i sve veze koje pokazuju na njega. Metoda vraća
	 * <code>true</code> ako je element postojao u modelu, inače
	 * <code>false</code>.
	 * 
	 * @param key
	 *            ključ elementa koji se uklanja
	 * @return <code>true</code> ako je element postojao u modelu, inače
	 *         <code>false</code>
	 */
	public boolean remove(int key) {
		int index;
		Element element = elements.get(key);

		if (element == null) {
			return false;
		}

		if (elements.remove(key) == null) {
			return false;
		}

		for (Integer destination : element.getConnections()) {
			elements.get(destination).removeConnection(key);
		}

		CompoundEdit removeEdit = new CompoundEdit();

		for (int i = 0; i < bookmarks.size(); i++) {
			if (bookmarks.get(i).getKey() == element.getId()) {
				Bookmark removedBookmark = bookmarks.remove(i);

				removeEdit.addEdit(new RemoveBookmarkEdit(i, removedBookmark));
				firePropertyChange(BOOKMARK_REMOVED, removedBookmark, null);
				i--;
			}
		}

		if (element.isInSet1()) {
			index = keys1.indexOf(key);
			keys1.remove(index);
			firePropertyChange(ELEMENT_REMOVED_FROM_SET1, key, null);
		} else {
			index = keys2.indexOf(key);
			keys2.remove(index);
			firePropertyChange(ELEMENT_REMOVED_FROM_SET2, key, null);
		}

		UndoableEdit edit = new RemoveEdit(element, index);
		removeEdit.addEdit(edit);
		removeEdit.end();
		postEdit(removeEdit);

		return true;
	}

	/**
	 * Dodaje novu vezu između dva elementa na temelju njihovih ključeva.
	 * Elementi moraju postojati u modelu i ne smiju biti iz istog skupa.
	 * <p>
	 * Ako veza već postoji u modelu, ona se ne dodaje ponovno.
	 * 
	 * @param key1
	 *            ključ prvog elementa
	 * @param key2
	 *            ključ drugog elementa
	 */
	public void addConnection(int key1, int key2) {
		Element element1 = elements.get(key1);
		Element element2 = elements.get(key2);

		if (element1 == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key " + key1 + " in the model");
		}

		if (element2 == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key " + key2 + " in the model");
		}

		if (element1.isInSet1() == element2.isInSet1()) {
			throw new IllegalArgumentException(
					"Cannot connect elements from the same set");
		}

		boolean result1 = elements.get(key1).addConnection(key2);
		boolean result2 = elements.get(key2).addConnection(key1);

		if (result1 != result2) {
			throw new IllegalStateException(
					"Inconsistency in DataModel: connection was not added to both elements");
		}

		if (result1) {
			UndoableEdit edit = new AddConnectionEdit(key1, key2);

			firePropertyChange(CONNECTION_ADDED, key1, key2);
			postEdit(edit);
		} 
	}

	/**
	 * Uklanja vezu između dva elementa na temelju njihovih ključeva. Elementi
	 * moraju postojati u modelu.
	 * 
	 * Ako veza ne postoji u modelu, u modelu se ništa ne mijenja.
	 * 
	 * @param key1
	 *            ključ prvog elementa
	 * @param key2
	 *            ključ drugog elementa
	 */
	public void removeConnection(int key1, int key2) {
		Element element1 = elements.get(key1);
		Element element2 = elements.get(key2);

		if (element1 == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key " + key1);
		}

		if (element2 == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key " + key2);
		}

		if (elements.get(key1).removeConnection(key2)
				&& elements.get(key2).removeConnection(key1)) {

			UndoableEdit edit = new RemoveConnectionEdit(key1, key2);

			firePropertyChange(CONNECTION_REMOVED, key1, key2);
			postEdit(edit);
		}
	}

	/**
	 * Vraća jedinstveni identifikator (ključ) koji još nije pridružen niti
	 * jednom elementu iz modela.
	 * 
	 * @return jedinstveni identifikator
	 */
	private int getNextKey() {
		return nextKey++;
	}

	/**
	 * Vraća jedinstveni identifikator (ključ) koji je pridružen zadnje dodanom
	 * elementu.
	 * 
	 * @return jedinstveni identifikator
	 */
	private int getLastKey() {
		return nextKey;
	}

	/**
	 * Dohvaća element iz modela na temelju njegova ključa. Ako element sa
	 * zadanim ključem ne postoji u modelu, metoda vraća <code>null</code>.
	 * 
	 * @param key
	 *            ključ elementa koji se dohvaća
	 * @return vrijednost elementa ili <code>null</code> ako element ne
	 *         postoji u modelu
	 */
	public String getElement(int key) {
		Element element = elements.get(key);

		if (element == null) {
			return null;
		}

		return element.getText();
	}

	/**
	 * Dohvaća listu ključeva elemenata koji se nalaze u prvom skupu modela.
	 * Lista čuva poredak elemenata.
	 * 
	 * @return lista ključeva elemenata prvog skupa
	 */
	public List<Integer> getKeys1() {
		return keys1;
	}

	/**
	 * Dohvaća listu ključeva elemenata koji se nalaze u drugom skupu modela.
	 * Lista čuva poredak elemenata.
	 * 
	 * @return lista ključeva elemenata drugog skupa
	 */
	public List<Integer> getKeys2() {
		return keys2;
	}

	/**
	 * Dohvaća skup ključeva koji predstavljaju elemente s kojima je povezan
	 * element čiji ključ metoda prima.
	 * 
	 * @param key
	 *            ključ elementa za kojeg se dohvaćaju veze
	 * @return sve veze koje vode iz zadanog elementa
	 */
	public Set<Integer> getConnections(int key) {
		Element element = elements.get(key);

		if (element == null) {
			return null;
		}

		return element.getConnections();
	}

	/**
	 * Dodaje obilježje modela. Ako obilježje sa danim imenom već postoji u
	 * modelu, njegova vrijednost se zamjenjuje sa proslijeđenom vrijednošću.
	 * 
	 * @param propertyName
	 *            ime obilježja
	 * @param propertyValue
	 *            vrijednost obilježja
	 */
	public void setProperty(String propertyName, String propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	/**
	 * Dohvaća vrijednost obilježja sa zadanim imenom.
	 * 
	 * @param propertyName
	 *            ime obilježja čija vrijednost se dohvaća
	 * @return vrijednost obilježja sa zadanim imenom ili <code>null</code>
	 *         ako obilježje sa zadanim imenom ne postoji
	 */
	public String getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	/**
	 * Postavlja varijablu <code>splitSafely</code> koja označava da li je
	 * element najvjerojatnije podijeljen točno ili ne za element sa zadanim
	 * ključem.
	 * 
	 * @param key
	 *            ključ elementa za koji se svojstvo postavlja
	 * @param splitSafely
	 *            <code>true</code> ako je element najvjerojatnije podijeljen
	 *            točno, <code>false</code> inače
	 */
	public void setSplitSafelyForElement(int key, boolean splitSafely) {
		Element element = elements.get(key);

		if (element == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key: " + key);
		}

		element.setSplitSafely(splitSafely);
	}

	/**
	 * Provjerava da li je element sa zadanim ključem najvjerojatnije podijeljen
	 * točno ili ne
	 * 
	 * @param key
	 *            ključ elementa za koji se svojstvo provjerava
	 * @return <code>true</code> ako je element najvjerojatnije podijeljen
	 *         točno, <code>false</code> inače
	 */
	public boolean isElementSplitSafely(int key) {
		Element element = elements.get(key);

		if (element == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key: " + key);
		}

		return element.isSplitSafely();
	}

	/**
	 * Postavlja broj odlomka u kojem se nalazi element sa zadanim ključem. Broj
	 * odlomka ne smije biti negativan.
	 * 
	 * @param key
	 *            ključ elementa za koji se postavlja broj odlomka
	 * @param paragraph
	 *            broj odlomka
	 */
	public void setParagraphForElement(int key, int paragraph) {
		Element element = elements.get(key);

		if (element == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key: " + key);
		}

		element.setParagraph(paragraph);
	}

	/**
	 * Dohvaća broj odlomka u kojem se nalazi element sa zadanim ključem.
	 * 
	 * @param key
	 *            ključ elementa za koji se dohvaća broj odlomka
	 * @return broj odlomka
	 */
	public int getParagraphForElement(int key) {
		Element element = elements.get(key);

		if (element == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key: " + key);
		}

		return element.getParagraph();
	}

	/**
	 * Postavlja atribute elementa za element sa zadanim ključem. Atributi su
	 * obilježja pročitana iz <i>taga</i> u kojem se nalazio element u ulaznoj
	 * xml datoteci.
	 * 
	 * @param key
	 *            ključ elementa za koji se atributi postavljaju
	 * @param attributes
	 *            atributi pročitani iz ulazne datoteke ili <code>null</code>
	 *            ako atributi nisu definirani
	 */
	public void setAttributesForElement(int key, Map<String, String> attributes) {
		Element element = elements.get(key);

		if (element == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key: " + key);
		}

		element.setAttributes(attributes);
	}

	/**
	 * Dohvaća atribute elementa sa zadanim ključem. Atributi su obilježja
	 * pročitana iz <i>taga</i> u kojem se nalazio element u ulaznoj xml
	 * datoteci.
	 * 
	 * @param key
	 *            ključ elementa za koji se atributi dohvaćaju
	 * @return atributi elementa iz ulazne datoteke ili <code>null</code> ako
	 *         atributi nisu definirani
	 */
	public Map<String, String> getAttributesForElement(int key) {
		Element element = elements.get(key);

		if (element == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key: " + key);
		}

		return element.getAttributes();
	}

	/**
	 * Dodaje novo ime atributa iz ulazne xml datoteke u model. Ako ime koje se
	 * dodaje već postoji u modelu, ono se ne dodaje.
	 * 
	 * @param attributeName
	 *            ime atributa koje se dodaje u model
	 * @return <code>true</code> ako je ime dodano u model, <code>false</code>
	 *         ako je ime već postojalo u modelu.
	 */
	public boolean addXmlAttributeName(String attributeName) {
		return xmlAttributes.add(attributeName);
	}

	/**
	 * Dohvaća skup imena atributa koji su prepoznati u ulaznoj xml datoteci.
	 * 
	 * @return skup imena atributa
	 */
	public Set<String> getXmlAttributeNames() {
		return xmlAttributes;
	}

	/**
	 * Dodaje novu knjižnu oznaku.
	 * 
	 * @param bookmark
	 *            knjižna oznaka koja se dodaje
	 */
	public void addBookmark(Bookmark bookmark) {
		bookmarks.add(bookmark);

		UndoableEdit edit = new AddBookmarkEdit(bookmark);

		firePropertyChange(BOOKMARK_ADDED, null, bookmark);
		postEdit(edit);
	}

	/**
	 * Uklanja knjižnu oznaku.
	 * 
	 * @param bookmark
	 *            knjižna oznaka koja se uklanja
	 * @return <code>true</code> ako je oznaka uspješno uklonjena,
	 *         <code>false</code> inače
	 */
	public boolean removeBookmark(Bookmark bookmark) {
		int index = bookmarks.indexOf(bookmark);
		if (index != -1) {
			bookmarks.remove(index);
			UndoableEdit edit = new RemoveBookmarkEdit(index, bookmark);

			firePropertyChange(BOOKMARK_REMOVED, bookmark, null);
			postEdit(edit);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Dohvaća sve knjižne oznake (bookmark).
	 * 
	 * @return knjižne oznake
	 */
	public List<Bookmark> getBookmarks() {
		return bookmarks;
	}

//	/**
//	 * Sprema statističke podatke u model.
//	 * 
//	 * @param statistics
//	 *            objekt koji sadrži statističke podatke
//	 */
//	public void setStatistics(Statistics statistics) {
//		this.statistics = statistics;
//	}

//	/**
//	 * Dohvaća statističke podatke iz modele.
//	 * 
//	 * @return objekt koji sadrži statističke podatke ili <code>null</code>
//	 *         ako statistički podaci nisu nikad spremljeni u model
//	 */
//	public Statistics getStatistics() {
//		return statistics;
//	}

	/**
	 * Dijeli element na dva elementa. Prvih <code>length</code> znakova iz
	 * elementa koji se dijeli ostaje u njemu i stvara se novi element koji
	 * sadrži ostatak znakova iz početnog elementa.
	 * <p>
	 * Novi element se dodaje u model na mjesto iza elementa koji se dijeli i
	 * metoda vraća njegov ključ.
	 * 
	 * @param key
	 *            ključ elementa koji se dijeli
	 * @param length
	 *            broj znakova koji ostaju u početnom elementu
	 * 
	 * @param safeSplit
	 *            označava da je najvjerojatnije podjela napravljena točno, ako
	 *            je <code>true</code> ili da postoji veća vjerojatnost da je
	 *            podjela netočna ako je <code>false</code>
	 * @return ključ novostvorenog elementa
	 */
	public int splitElement(int key, int length, boolean safeSplit) {
		Element element = elements.get(key);

		if (element == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key: " + key);
		}

		int lastKey = getLastKey();
		int newKey = getNextKey();
		if (element.isInSet1()) {
			elements.put(newKey, new Element(newKey, element.getText()
					.substring(length).trim(), Element.SET1, element
					.getParagraph(), safeSplit));
			keys1.add(keys1.indexOf(key) + 1, newKey);
		} else {
			elements.put(newKey, new Element(newKey, element.getText()
					.substring(length).trim(), Element.SET2, element
					.getParagraph(), safeSplit));
			keys2.add(keys2.indexOf(key) + 1, newKey);
		}

		element.setText(element.getText().substring(0, length).trim());

		UndoableEdit edit = new SplitEdit(lastKey, key, newKey);

		if (element.isInSet1()) {
			firePropertyChange(ELEMENT_SPLIT_IN_SET1, key, newKey);
		} else {
			firePropertyChange(ELEMENT_SPLIT_IN_SET2, key, newKey);
		}

		postEdit(edit);

		return newKey;
	}

	/**
	 * Spaja dva elementa na temelju njihovih ključeva. Novi element dobiva
	 * ključ prvog elementa od elemenata koji se spajaju, a drugi element se
	 * briše iz modela.
	 * <p>
	 * Elementi moraju biti iz istog skupa.
	 * 
	 * @param key1
	 *            ključ prvog elementa, taj ključ dobiva novi element
	 * @param key2
	 *            ključ drugog elementa, taj se element briše iz modela
	 */
	public void combineElements(int key1, int key2) {
		Element element1 = elements.get(key1);
		Element element2 = elements.get(key2);

		if (element1 == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key: " + key1);
		}

		if (element2 == null) {
			throw new IllegalArgumentException(
					"Unable to find element with key: " + key2);
		}

		if (element1.isInSet1() != element2.isInSet1()) {
			throw new IllegalArgumentException(
					"Cannot combine elements from different sets");
		}

		UndoableEdit edit = new CombineEdit(element1, element2);

		element1.setText(element1.getText() + element2.getText());

		// Uklanja se element2 i sve njegove veze
		for (Integer destination : element2.getConnections()) {
			elements.get(destination).removeConnection(key2);
		}

		if (element2.isInSet1()) {
			keys1.remove(Integer.valueOf(key2));
		} else {
			keys2.remove(Integer.valueOf(key2));
		}

		elements.remove(key2);

		if (element1.isInSet1()) {
			firePropertyChange(ELEMENTS_COMBINED_IN_SET1, key2, key1);
		} else {
			firePropertyChange(ELEMENTS_COMBINED_IN_SET2, key2, key1);
		}
		postEdit(edit);
	}

	/**
	 * Uklanja sve podatke iz modela, nakon što se ova metoda izvrši model će
	 * biti prazan.
	 */
	public void clear() {
		elements.clear();
		keys1.clear();
		keys2.clear();
		bookmarks.clear();
		xmlAttributes.clear();
		properties.clear();
		nextKey = 0;
//		statistics = null;
		compoundEdit = null;
		compoundActions = false;
		notifyListeners = true;

		firePropertyChange(GLOBAL_CLEAR, null, null);
	}

	/**
	 * Služi za osvježavanje pogleda. Metoda ponovno šalje događaje o dodavanju
	 * svih elemenata, veza i knjižnih oznaka koje su već u modelu.
	 */
	public void reloadAll() {
		for (Integer key : keys1) {
			firePropertyChange(ELEMENT_ADDED_IN_SET1, null, key);
		}

		for (Integer key : keys2) {
			firePropertyChange(ELEMENT_ADDED_IN_SET2, null, key);
		}

		for (Integer leftKey : keys1) {
			Element element = elements.get(leftKey);

			for (Integer rightKey : element.getConnections()) {
				firePropertyChange(CONNECTION_ADDED, leftKey, rightKey);
			}
		}

		for (Bookmark bookmark : bookmarks) {
			firePropertyChange(BOOKMARK_ADDED, null, bookmark);
		}
	}

	/**
	 * Dodaje podatke iz modela koji se proslijeđuje kao parametar u model nad
	 * kojim se metoda poziva. U model se dodaju svi elementi i detalji o njima,
	 * veze između elemenata te knjižne oznake. Podaci koji su se nalazili u
	 * modelu nad kojim se metoda poziva prije njenog pozivanja ostaju
	 * nepromijenjeni.
	 * 
	 * @param model
	 *            model iz kojeg se dodaju elementi
	 */
	public void addAll(DataModel model) {
		for (Integer key : model.getKeys1()) {
			String text = model.getElement(key);
			int paragraph = model.getParagraphForElement(key);
			Map<String, String> attributes = model.getAttributesForElement(key);
			add1(text, paragraph, attributes);
		}

		for (Integer key : model.getKeys2()) {
			String text = model.getElement(key);
			int paragraph = model.getParagraphForElement(key);
			Map<String, String> attributes = model.getAttributesForElement(key);
			add2(text, paragraph, attributes);
		}

		for (Integer leftKey : model.getKeys1()) {
			Set<Integer> connections = model.getConnections(leftKey);

			for (Integer rightKey : connections) {
				addConnection(leftKey, rightKey);
			}
		}

		for (Bookmark bookmark : model.getBookmarks()) {
			addBookmark(bookmark);
		}

		xmlAttributes.addAll(model.xmlAttributes);
		properties.putAll(model.properties);
//		setStatistics(model.getStatistics());
	}

	/**
	 * Metoda vraća listu koja sadrži dva broja koja su predana metodi. Vraćena
	 * lista predstavlja uređeni par predanih brojeva.
	 * 
	 * @param a
	 *            prvi član
	 * @param b
	 *            drugi član
	 * @return uređeni par (a,b)
	 */
	private List<Integer> constructPair(int a, int b) {
		List<Integer> pair = new ArrayList<Integer>(2);
		pair.add(a);
		pair.add(b);
		return pair;
	}

	/*
	 * PRIVATNE KLASE ZA UNDO I REDO
	 */

	/**
	 * Privatna klasa čija instanca se stvara svaki put kada se dodaje novi
	 * element u model. Klasa sadrži sve potrebne informacije kako bi mogla
	 * poništiti tu akciju ili je ponoviti.
	 * <p>
	 * Klasa također sadrži i metode (<code>undo()</code> i
	 * <code>redo()</code>) koje omogućavaju vraćanje i ponovno izvršavanje
	 * te akcije nad modelom.
	 */
	private class AddEdit extends AbstractUndoableEdit {

		private static final long serialVersionUID = 4822611980638370628L;

		private Element element = null;

		private int lastKey;

		private int currentKey;

		/**
		 * Javni konstruktor klase.
		 * 
		 * @param lastKey
		 *            ključ elementa koji je dodan prije trenutnog elmenta
		 * @param currentKey
		 *            ključ trenutnog elementa (onog koji se dodaje)
		 */
		public AddEdit(int lastKey, int currentKey) {
			super();
			this.lastKey = lastKey;
			this.currentKey = currentKey;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			element = elements.remove(currentKey);

			if (element != null) {

				nextKey = lastKey;

				if (element.isInSet1()) {
					keys1.remove(Integer.valueOf(currentKey));
					firePropertyChange(ELEMENT_REMOVED_FROM_SET1, currentKey,
							null);
				} else {
					keys2.remove(Integer.valueOf(currentKey));
					firePropertyChange(ELEMENT_REMOVED_FROM_SET2, currentKey,
							null);
				}

			} else {
				throw new IllegalStateException(
						"Cannot find element with key: " + currentKey);
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			if (element == null) {
				throw new IllegalStateException("Cannot redo");
			}
			nextKey = currentKey;

			elements.put(currentKey, element);
			if (element.isInSet1()) {
				keys1.add(currentKey);
				firePropertyChange(ELEMENT_ADDED_IN_SET1, null, currentKey);
			} else {
				keys2.add(currentKey);
				firePropertyChange(ELEMENT_ADDED_IN_SET2, null, currentKey);
			}
		}

		public String getPresentationName() {
			return "add element";
		}
	}

	/**
	 * Privatna klasa čija instanca se stvara svaki put kada se novi element
	 * umeće u model. Klasa sadrži sve potrebne informacije kako bi mogla
	 * poništiti tu akciju ili je ponoviti.
	 * <p>
	 * Klasa također sadrži i metode (<code>undo()</code> i
	 * <code>redo()</code>) koje omogućavaju vraćanje i ponovno izvršavanje
	 * te akcije nad modelom.
	 */
	private class InsertEdit extends AbstractUndoableEdit {

		private static final long serialVersionUID = -4492838466431281987L;

		private Element element = null;

		private int lastKey;

		private int currentKey;

		private int index;

		/**
		 * Javni konstruktor klase.
		 * 
		 * @param index
		 *            mjesto na koje je element umetnut
		 * @param lastKey
		 *            ključ elementa koji je dodan prije trenutnog elmenta
		 * @param currentKey
		 *            ključ trenutnog elementa (onog koji se umeće)
		 */
		public InsertEdit(int index, int lastKey, int currentKey) {
			super();
			this.lastKey = lastKey;
			this.currentKey = currentKey;
			this.index = index;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			element = elements.remove(currentKey);

			if (element != null) {

				nextKey = lastKey;

				if (element.isInSet1()) {
					keys1.remove(Integer.valueOf(currentKey));
					firePropertyChange(ELEMENT_REMOVED_FROM_SET1, currentKey,
							null);
				} else {
					keys2.remove(Integer.valueOf(currentKey));
					firePropertyChange(ELEMENT_REMOVED_FROM_SET2, currentKey,
							null);
				}

			} else {
				throw new IllegalStateException(
						"Cannot find element with key: " + currentKey);
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			if (element == null) {
				throw new IllegalStateException("Cannot redo");
			}
			nextKey = currentKey;

			elements.put(currentKey, element);
			if (element.isInSet1()) {
				keys1.add(index, currentKey);
				firePropertyChange(ELEMENT_INSERTED_IN_SET1, null,
						constructPair(index, currentKey));
			} else {
				keys2.add(index, currentKey);
				firePropertyChange(ELEMENT_INSERTED_IN_SET2, null,
						constructPair(index, currentKey));
			}
		}

		public String getPresentationName() {
			return "insert element";
		}
	}

	/**
	 * Privatna klasa čija instanca se stvara svaki put kada se promjeni tekst
	 * nekog elementa iz modela. Klasa sadrži sve potrebne informacije kako bi
	 * mogla poništiti tu akciju ili je ponoviti.
	 * <p>
	 * Klasa također sadrži i metode (<code>undo()</code> i
	 * <code>redo()</code>) koje omogućavaju vraćanje i ponovno izvršavanje
	 * te akcije nad modelom.
	 */
	private class UpdateEdit extends AbstractUndoableEdit {

		private static final long serialVersionUID = 2872223561875597464L;

		private int key;

		private String text;

		/**
		 * Javni konstruktor klase.
		 * 
		 * @param key
		 *            ključ elementa čiji tekst se promijenio
		 * @param text
		 *            tekst elementa prije promjene
		 */
		public UpdateEdit(int key, String text) {
			super();
			this.key = key;
			this.text = text;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Element element = elements.get(key);
			String newText = element.getText();
			element.setText(text);
			text = newText;

			if (element.isInSet1()) {
				firePropertyChange(ELEMENT_UPDATED_IN_SET1, null, key);
			} else {
				firePropertyChange(ELEMENT_UPDATED_IN_SET2, null, key);
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			Element element = elements.get(key);
			String newText = element.getText();
			element.setText(text);
			text = newText;

			if (element.isInSet1()) {
				firePropertyChange(ELEMENT_UPDATED_IN_SET1, null, key);
			} else {
				firePropertyChange(ELEMENT_UPDATED_IN_SET2, null, key);
			}
		}

		public String getPresentationName() {
			return "update element";
		}
	}

	/**
	 * Privatna klasa čija instanca se stvara svaki put kada se element uklanja
	 * iz modela. Klasa sadrži sve potrebne informacije kako bi mogla poništiti
	 * tu akciju ili je ponoviti.
	 * <p>
	 * Klasa također sadrži i metode (<code>undo()</code> i
	 * <code>redo()</code>) koje omogućavaju vraćanje i ponovno izvršavanje
	 * te akcije nad modelom.
	 */
	private class RemoveEdit extends AbstractUndoableEdit {

		private static final long serialVersionUID = -2086917409174406467L;

		private Element element;

		private int key;

		private int index;

		/**
		 * Javni konstruktor klase.
		 * 
		 * @param element
		 *            element koji je uklonjen
		 * @param index
		 *            mjesto na kojemu se element nalazio
		 */
		public RemoveEdit(Element element, int index) {
			super();
			this.element = element;
			this.key = element.getId();
			this.index = index;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			elements.put(key, element);

			if (element.isInSet1()) {
				keys1.add(index, key);
				firePropertyChange(ELEMENT_INSERTED_IN_SET1, null,
						constructPair(index, key));
			} else {
				keys2.add(index, key);
				firePropertyChange(ELEMENT_INSERTED_IN_SET2, null,
						constructPair(index, key));
			}

			for (Integer destination : element.getConnections()) {
				elements.get(destination).addConnection(key);
				if (element.isInSet1()) {
					firePropertyChange(CONNECTION_ADDED, key, destination);
				} else {
					firePropertyChange(CONNECTION_ADDED, destination, key);
				}
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			for (Integer destination : element.getConnections()) {
				elements.get(destination).removeConnection(key);
			}

			elements.remove(key);

			if (element.isInSet1()) {
				keys1.remove(Integer.valueOf(key));
				firePropertyChange(ELEMENT_REMOVED_FROM_SET1, key, null);
			} else {
				keys2.remove(Integer.valueOf(key));
				firePropertyChange(ELEMENT_REMOVED_FROM_SET2, key, null);
			}
		}

		public String getPresentationName() {
			return "remove element";
		}
	}

	/**
	 * Privatna klasa čija instanca se stvara svaki put kada se dodaje nova veza
	 * u model. Klasa sadrži sve potrebne informacije kako bi mogla poništiti tu
	 * akciju ili je ponoviti.
	 * <p>
	 * Klasa također sadrži i metode (<code>undo()</code> i
	 * <code>redo()</code>) koje omogućavaju vraćanje i ponovno izvršavanje
	 * te akcije nad modelom.
	 */
	private class AddConnectionEdit extends AbstractUndoableEdit {

		private static final long serialVersionUID = 4055847232169765554L;

		private int key1;

		private int key2;

		/**
		 * Javni konstruktor klase.
		 * 
		 * @param key1
		 *            ključ elementa iz prvog skupa
		 * @param key2
		 *            ključ elementa iz drugog skupa
		 */
		public AddConnectionEdit(int key1, int key2) {
			super();
			this.key1 = key1;
			this.key2 = key2;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			elements.get(key1).removeConnection(key2);
			elements.get(key2).removeConnection(key1);

			firePropertyChange(CONNECTION_REMOVED, key1, key2);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			elements.get(key1).addConnection(key2);
			elements.get(key2).addConnection(key1);

			firePropertyChange(CONNECTION_ADDED, key1, key2);
		}

		public String getPresentationName() {
			return "add connection";
		}
	}

	/**
	 * Privatna klasa čija instanca se stvara svaki put kada se veza uklanja iz
	 * modela. Klasa sadrži sve potrebne informacije kako bi mogla poništiti tu
	 * akciju ili je ponoviti.
	 * <p>
	 * Klasa također sadrži i metode (<code>undo()</code> i
	 * <code>redo()</code>) koje omogućavaju vraćanje i ponovno izvršavanje
	 * te akcije nad modelom.
	 */
	private class RemoveConnectionEdit extends AbstractUndoableEdit {

		private static final long serialVersionUID = 8447029557329398943L;

		private int key1;

		private int key2;

		/**
		 * Javni konstruktor klase.
		 * 
		 * @param key1
		 *            ključ elementa iz prvog skupa
		 * @param key2
		 *            ključ elementa iz drugog skupa
		 */
		public RemoveConnectionEdit(int key1, int key2) {
			super();
			this.key1 = key1;
			this.key2 = key2;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			elements.get(key1).addConnection(key2);
			elements.get(key2).addConnection(key1);

			firePropertyChange(CONNECTION_ADDED, key1, key2);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			elements.get(key1).removeConnection(key2);
			elements.get(key2).removeConnection(key1);

			firePropertyChange(CONNECTION_REMOVED, key1, key2);
		}

		public String getPresentationName() {
			return "remove connection";
		}
	}

	/**
	 * Privatna klasa čija instanca se stvara svaki put kada se dodaje nova
	 * knjižna oznaka u model. Klasa sadrži sve potrebne informacije kako bi
	 * mogla poništiti tu akciju ili je ponoviti.
	 * <p>
	 * Klasa također sadrži i metode (<code>undo()</code> i
	 * <code>redo()</code>) koje omogućavaju vraćanje i ponovno izvršavanje
	 * te akcije nad modelom.
	 */
	private class AddBookmarkEdit extends AbstractUndoableEdit {

		private static final long serialVersionUID = -2473137427451594375L;

		private Bookmark bookmark;

		/**
		 * Javni konstruktor klase.
		 * 
		 * @param bookmark
		 *            knjižna oznaka koja je dodana
		 */
		public AddBookmarkEdit(Bookmark bookmark) {
			super();
			this.bookmark = bookmark;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			bookmarks.remove(bookmark);

			firePropertyChange(BOOKMARK_REMOVED, bookmark, null);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			bookmarks.add(bookmark);

			firePropertyChange(BOOKMARK_ADDED, null, bookmark);
		}

		public String getPresentationName() {
			return "add bookmark";
		}
	}

	/**
	 * Privatna klasa čija instanca se stvara svaki put kada se knjižna oznaka
	 * uklanja iz modela. Klasa sadrži sve potrebne informacije kako bi mogla
	 * poništiti tu akciju ili je ponoviti.
	 * <p>
	 * Klasa također sadrži i metode (<code>undo()</code> i
	 * <code>redo()</code>) koje omogućavaju vraćanje i ponovno izvršavanje
	 * te akcije nad modelom.
	 */
	private class RemoveBookmarkEdit extends AbstractUndoableEdit {

		private static final long serialVersionUID = 3589309595705507829L;

		private Bookmark bookmark;
		private int index;

		/**
		 * Javni konstruktor klase.
		 * 
		 * @param index
		 *            mjesto na kojemu se knjižna oznaka nalazila
		 * @param bookmark
		 *            knjižna oznaka koja je uklonjena
		 */
		public RemoveBookmarkEdit(int index, Bookmark bookmark) {
			super();
			this.index = index;
			this.bookmark = bookmark;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			bookmarks.add(index, bookmark);

			firePropertyChange(BOOKMARK_ADDED, null, bookmark);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			bookmarks.remove(bookmark);

			firePropertyChange(BOOKMARK_REMOVED, bookmark, null);
		}

		public String getPresentationName() {
			return "remove bookmark";
		}
	}

	/**
	 * Privatna klasa čija instanca se stvara svaki put kada se element iz
	 * modela podijeli na dva elementa. Klasa sadrži sve potrebne informacije
	 * kako bi mogla poništiti tu akciju ili je ponoviti.
	 * <p>
	 * Klasa također sadrži i metode (<code>undo()</code> i
	 * <code>redo()</code>) koje omogućavaju vraćanje i ponovno izvršavanje
	 * te akcije nad modelom.
	 */
	private class SplitEdit extends AbstractUndoableEdit {

		private static final long serialVersionUID = -4614557236516634413L;

		private int lastKey;

		private int key1;

		private int key2;

		private Element element1;

		private Element element2;

		private String text1;

		/**
		 * Javni konstruktor klase.
		 * 
		 * @param lastKey
		 *            ključ elementa koji je dodan prije elementa koji se dodaje
		 *            kao rezultat podjele
		 * @param key1
		 *            ključ elementa iz prvog skupa
		 * @param key2
		 *            ključ elementa iz drugog skupa
		 */
		public SplitEdit(int lastKey, int key1, int key2) {
			super();
			this.lastKey = lastKey;
			this.key1 = key1;
			this.key2 = key2;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			element1 = elements.get(key1);
			element2 = elements.get(key2);

			text1 = element1.getText();

			element1.setText(element1.getText() + element2.getText());

			nextKey = lastKey;

			elements.remove(key2);

			if (element2.isInSet1()) {
				keys1.remove(Integer.valueOf(key2));
				firePropertyChange(ELEMENTS_COMBINED_IN_SET1, key2, key1);
			} else {
				keys2.remove(Integer.valueOf(key2));
				firePropertyChange(ELEMENTS_COMBINED_IN_SET2, key2, key1);
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			lastKey = key2;

			elements.put(key2, element2);

			element1.setText(text1);

			if (element2.isInSet1()) {
				keys1.add(keys1.indexOf(key1) + 1, key2);
				firePropertyChange(ELEMENT_SPLIT_IN_SET1, key1, key2);
			} else {
				keys2.add(keys2.indexOf(key1) + 1, key2);
				firePropertyChange(ELEMENT_SPLIT_IN_SET2, key1, key2);
			}
		}

		public String getPresentationName() {
			return "split element";
		}

	}

	/**
	 * Privatna klasa čija instanca se stvara svaki put kada se dva elementa iz
	 * modela spoje u jedan. Klasa sadrži sve potrebne informacije kako bi mogla
	 * poništiti tu akciju ili je ponoviti.
	 * <p>
	 * Klasa također sadrži i metode (<code>undo()</code> i
	 * <code>redo()</code>) koje omogućavaju vraćanje i ponovno izvršavanje
	 * te akcije nad modelom.
	 */
	private class CombineEdit extends AbstractUndoableEdit {

		private static final long serialVersionUID = 8919319141573952183L;

		private int key1;

		private int key2;

		private Element element1;

		private Element element2;

		private String text1;

		/**
		 * Javni konstruktor klase.
		 * 
		 * @param element1
		 *            element sa kojim se spaja drugi element
		 * @param element2
		 *            element koji se spaja sa prvim elementom
		 */
		public CombineEdit(Element element1, Element element2) {
			super();
			this.key1 = element1.getId();
			this.key2 = element2.getId();
			this.element1 = element1;
			this.element2 = element2;
			text1 = element1.getText();
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			element1.setText(text1);

			elements.put(key2, element2);

			if (element2.isInSet1()) {
				keys1.add(keys1.indexOf(key1) + 1, key2);
				firePropertyChange(ELEMENT_SPLIT_IN_SET1, key1, key2);
			} else {
				keys2.add(keys2.indexOf(key1) + 1, key2);
				firePropertyChange(ELEMENT_SPLIT_IN_SET2, key1, key2);
			}

			for (Integer destination : element2.getConnections()) {
				elements.get(destination).addConnection(key2);

				if (element2.isInSet1()) {
					firePropertyChange(CONNECTION_ADDED, key2, destination);
				} else {
					firePropertyChange(CONNECTION_ADDED, destination, key2);
				}
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			element1.setText(element1.getText() + element2.getText());

			for (Integer destination : element2.getConnections()) {
				elements.get(destination).removeConnection(key2);
			}

			elements.remove(key2);

			if (element2.isInSet1()) {
				keys1.remove(Integer.valueOf(key2));
				firePropertyChange(ELEMENTS_COMBINED_IN_SET1, key2, key1);
			} else {
				keys2.remove(Integer.valueOf(key2));
				firePropertyChange(ELEMENTS_COMBINED_IN_SET2, key2, key1);
			}
		}

		public String getPresentationName() {
			return "combine elements";
		}
	}
}
