import java.util.Collection;
import java.util.Collections;

/**
 * An index to store elements and the locations those elements were found.
 *
 * @param <T> the type of element to store
 */
public interface Index<T> {

	/**
	 * Adds the element and position.
	 *
	 * @param element  the element found
	 * @param position the position the element was found
	 * @return {@code true} if the index changed as a result of the call
	 */
	public boolean add(T element, int position);

	/**
	 * Adds the elements in order with starting position 1.
	 *
	 * @param elements the element to add
	 * @return {@code true} if the index changed as a result of the call
	 *
	 * @see #addAll(T[], int)
	 */
	public default boolean addAll(T[] elements) {
		return addAll(elements, 1);
	};

	/**
	 * Adds the elements in order with the provided starting position.
	 *
	 * @param elements the elements to add
	 * @param start    the starting position of the first element
	 * @return {@code true} if the index changed as a result of the call
	 *
	 * @see #add(Object, int)
	 */
	public default boolean addAll(T[] elements, int start) {
	    boolean modified = false;
		for (int i = 0; i < elements.length; i++) {
            if (add(elements[i], i + start))
				modified = true;
		}
		return modified;
	}

	/**
	 * Returns the number of positions stored for the given element.
	 *
	 * @param element the element to lookup
	 * @return 0 if the element is not in the index or has no positions, otherwise
	 *         the number of positions stored for that element
	 */
	public int numPositions(T element);

	/**
	 * Returns the number of element stored in the index.
	 *
	 * @return 0 if the index is empty, otherwise the number of element in the index
	 */
	public int numElements();

	/**
	 * Determines whether the element is stored in the index.
	 *
	 * @param element the element to lookup
	 * @return {@true} if the element is stored in the index
	 */
	public boolean contains(T element);

	/**
	 * Determines whether the element is stored in the index and the position is
	 * stored for that element.
	 *
	 * @param element  the element to lookup
	 * @param position the position of that element to lookup
	 * @return {@true} if the element and position is stored in the index
	 */
	public boolean contains(T element, int position);

	/**
	 * Returns an unmodifiable view of the elements stored in the index.
	 *
	 * @return an unmodifiable view of the elements stored in the index
	 * @see Collections#unmodifiableCollection(Collection)
	 */
	public Collection<T> getElements();

	/**
	 * Returns an unmodifiable view of the positions stored in the index for the
	 * provided element, or an empty collection if the element is not in the index.
	 *
	 * @param element the element to lookup
	 * @return an unmodifiable view of the positions stored for the element
	 */
	public Collection<Integer> getPositions(T element);

}
