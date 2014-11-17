package com.artemis.utils;

import java.util.*;

/**
 * Collection type a bit like ArrayList but does not preserve the order of its
 * entities, speedwise it is very good, especially suited for games.
 */

public class Bag<E> implements ImmutableBag<E>, Collection<E> {
	private E[] data;
	private int size = 0;

	/**
	 * Constructs an empty Bag with an initial capacity of 64.
	 * 
	 */
	public Bag() {
		this(64);
	}

	/**
	 * Constructs an empty Bag with the specified initial capacity.
	 * 
	 * @param capacity
	 *            the initial capacity of Bag
	 */
	@SuppressWarnings("unchecked")
	public Bag(int capacity) {
		data = (E[])new Object[capacity];
	}

	/**
	 * Removes the element at the specified position in this Bag. does this by
	 * overwriting it was last element then removing last element
	 * 
	 * @param index
	 *            the index of element to be removed
	 * @return element that was removed from the Bag
	 */
	public E remove(int index) {
		E e = data[index]; // make copy of element to remove so it can be returned
		data[index] = data[--size]; // overwrite item to remove with last element
		data[size] = null; // null last element, so gc can do its work
		return e;
	}
	
	
	/**
	 * Remove and return the last object in the bag.
	 * 
	 * @return the last object in the bag, null if empty.
	 */
	public E removeLast() {
		if(size > 0) {
			E e = data[--size];
			data[size] = null;
			return e;
		}
		
		return null;
	}

	/**
	 * Removes the first occurrence of the specified element from this Bag, if
	 * it is present. If the Bag does not contain the element, it is unchanged.
	 * does this by overwriting it was last element then removing last element
	 * 
	 * @param o
	 *            element to be removed from this list, if present
	 * @return <tt>true</tt> if this list contained the specified element
	 */
    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < size; i++) {
            E e2 = data[i];

            if (o == e2) {
                data[i] = data[--size]; // overwrite item to remove with last element
                data[size] = null; // null last element, so gc can do its work
                return true;
            }
        }

        return false;
    }

    /**
	 * Check if bag contains this element.
	 * 
	 * @param o
	 * @return
	 */
    @Override
    public boolean contains(Object o) {
        for(int i = 0; size > i; i++) {
            if(o == data[i]) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        for( Object o : c ) {
            if( !contains(o) )
                return false;
        }
        return true;
    }

    /**
	 * Removes from this Bag all of its elements that are contained in the
	 * specified Bag.
	 * 
	 * @param c collection containing elements to be removed from this Bag
	 * @return {@code true} if this Bag changed as a result of the call
	 */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;

        for (Object e1 : c) {
            for (int j = 0; j < size; j++) {
                E e2 = data[j];

                if (e1 == e2) {
                    remove(j);
                    modified = true;
                    break;
                }
            }
        }

        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        for (int i = 0; i < size; i++) {
            E e = data[i];
            if(!c.contains(e)) {
                remove(e);
                --i;
                modified = true;
            }
        }
        return modified;
    }

    /**
	 * Returns the element at the specified position in Bag.
	 * 
	 * @param index
	 *            index of the element to return
	 * @return the element at the specified position in bag
	 */
	public E get(int index) {
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
	 * 
	 * @param index
	 * @return
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
	 * @param e
	 *            element to be added to this list
	 */
	public boolean add(E e) {
		// is size greater than capacity increase capacity
		if (size == data.length) {
			grow();
		}

		data[size++] = e;
        return true;
	}


    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for(E e : c) {
            modified |= add(e);
        }
        return modified;
    }

    /**
	 * Set element at specified index in the bag.
	 * 
	 * @param index position of element
	 * @param e the element
	 */
	public void set(int index, E e) {
		if(index >= data.length) {
			grow(index*2);
		}
		size = index+1;
		data[index] = e;
	}

	private void grow() {
		int newCapacity = (data.length * 3) / 2 + 1;
		grow(newCapacity);
	}
	
	@SuppressWarnings("unchecked")
	private void grow(int newCapacity) {
		E[] oldData = data;
		data = (E[])new Object[newCapacity];
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
		// null all elements so gc can clean up
		for (int i = 0; i < size; i++) {
			data[i] = null;
		}

		size = 0;
	}

    @Override
    public Object[] toArray() {
        return data.clone();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

        Object[] result = a;
        for(int i = 0; size > i; i++)
            result[i++] = data[i];

        if (a.length > size)
            a[size] = null;

        return a;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private int currentIndex = size <= 0 ? -1 : 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size && data[currentIndex] != null;
            }

            @Override
            public E next() {
                if(currentIndex < 0)
                    throw new NoSuchElementException("next on empty iterator");

                return data[currentIndex++];
            }

            @Override
            public void remove() {
                if(currentIndex < 0)
                    throw new NoSuchElementException("remove on empty iterator");
                Bag.this.remove(currentIndex--);
            }
        };
    }
}
