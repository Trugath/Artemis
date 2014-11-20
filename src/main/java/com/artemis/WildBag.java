package com.artemis;

import com.artemis.utils.Bag;


/**
 * Let's the user set the size.
 * <p>
 * Setting the size does not resize the bag, nor will it clean up contents
 * beyond the given size. Only use this if you know what you are doing!
 * </p>
 *
 * @author junkdog
 *
 * @param <T>
 *			object type this bag holds
 */
public class WildBag<T> extends Bag<T> {

	/**
	 * Returns this bag's underlying array.
	 * <p>
	 * Use with care.
	 * </p>
	 *
	 * @return the underlying array
	 *
	 * @see Bag#size()
	 */
	public Object[] getData() {
		return data;
	}

	/**
	 * Set the size.
	 * <p>
	 * This will not resize the bag, nor will it clean up contents beyond the
	 * given size. Use with caution.
	 * </p>
	 *
	 * @param size
	 *			the size to set
	 */
	void setSize(int size) {
		this.size = size;
	}

}
