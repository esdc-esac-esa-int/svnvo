package esavo.uws.utils.test.utils;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorEnumeration<E> implements Enumeration<E> {
	
	private Iterator<E> iterator;
	
	public IteratorEnumeration(Iterator<E> it){
		this.iterator = it;
	}

	@Override
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

	@Override
	public E nextElement() {
		return iterator.next();
	}

}
