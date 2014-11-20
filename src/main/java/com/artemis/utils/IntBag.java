package com.artemis.utils;

/**
 * Collection type a bit like ArrayList but does not preserve the order of its
 * entities, speedwise it is very good, especially suited for games.
 */

public class IntBag {
	private int[] data;
	private int size = 0;

	transient volatile int modCount;

	/**
	 * Constructs an empty Bag with an initial capacity of 64.
	 *
	 */
	public IntBag() {
		this(64);
	}

	/**
	 * Constructs an empty Bag with the specified initial capacity.
	 *
	 * @param capacity the initial capacity of Bag
	 */
	@SuppressWarnings("unchecked")
	public IntBag(int capacity) {
		data = new int[capacity];
	}

	/**
	 * Removes the element at the specified position in this Bag. does this by
	 * overwriting it was last element then removing last element
	 *
	 * @param index the index of element to be removed
	 * @return element that was removed from the Bag
	 */
	public int remove(int index) {
		++modCount;
		int e = data[index]; // make copy of element to remove so it can be returned
		data[index] = data[--size]; // overwrite item to remove with last element
		return e;
	}


	/**
	 * Remove and return the last object in the bag.
	 *
	 * @return the last object in the bag, null if empty.
	 */
	public int removeLast() {
		if (size > 0) {
			++modCount;
			int e = data[--size];
			return e;
		}

		return 0;
	}

	/**
	 * Returns the element at the specified position in Bag.
	 *
	 * @param index index of the element to return
	 * @return the element at the specified position in bag
	 */
	public int get(int index) {
		return data[index];
	}

	/**
	 * Returns the element at the specified position in Bag. This method
	 * ensures that the bag grows if the requested index is outside the bounds
	 * of the current backing array.
	 *
	 * @param index
	 *			index of the element to return
	 *
	 * @return the element at the specified position in bag
	 *
	 */
	public int safeGet(int index) {
		if(index >= data.length) {
			grow((index * 7) / 4 + 1);
		}

		return data[index];
	}

	/**
	 * Returns the number of elements in this bag.
	 *
	 * @return the number of elements in this bag
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns the number of elements the bag can hold without growing.
	 *
	 * @return the number of elements the bag can hold without growing.
	 */
	public int getCapacity() {
		return data.length;
	}

	/**
	 * Checks if the internal storage supports this index.
	 */
	public boolean isIndexWithinBounds(int index) {
		return index < getCapacity();
	}

	/**
	 * Returns true if this list contains no elements.
	 *
	 * @return true if this list contains no elements
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Adds the specified element to the end of this bag. if needed also
	 * increases the capacity of the bag.
	 *
	 * @param e element to be added to this list
	 */
	public boolean add(int e) {

		// is size greater than capacity increase capacity
		if (size == data.length) {
			grow();
		}

		++modCount;
		data[size++] = e;
		return true;
	}


	/**
	 * Set element at specified index in the bag.
	 *
	 * @param index position of element
	 * @param e the element
	 */
	public void set(int index, int e) {
		if(index >= data.length) {
			grow(index*2);
		}
		++modCount;
		size = Math.max(index+1, size);
		data[index] = e;
	}

	private void grow() {
		int newCapacity = (data.length * 3) / 2 + 1;
		grow(newCapacity);
	}

	@SuppressWarnings("unchecked")
	private void grow(int newCapacity) {
		int[] oldData = data;
		data = new int[newCapacity];
		System.arraycopy(oldData, 0, data, 0, oldData.length);
	}

	public void ensureCapacity(int index) {
		if(index >= data.length) {
			grow(index*2);
		}
	}

	/**
	 * Removes all of the elements from this bag. The bag will be empty after
	 * this call returns.
	 */
	public void clear() {

		for (int i = 0; i < size; i++) {
			++modCount;
			data[i] = 0;
		}

		size = 0;
	}

	@Override
	public String toString() {
		String result = "[";
		if(size>0)
			result += data[0];
		for (int i = 1; i < size; i++) {
			result += ", " + data[i];
		}
		return result + "]";
	}

	public int[] toArray() {
		int[] result = new int[size];
		System.arraycopy(data, 0, result, 0, size);
		return result;
	}
}
