// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridshell.model;

import java.io.Serializable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.globus.cog.gridshell.interfaces.ShellHistory;

public class ShellHistoryImpl implements Serializable, ShellHistory {
	// Used to Log
	private static Logger logger = Logger.getLogger(ShellHistoryImpl.class);

	// The index to insert our next element
	private int index = 0;

	// The index that we can view or peek
	private int peekIndex = 0;

	// number of items in our history
	private int count;

	// Circular array used to store items in the history
	private Object[] history;

	// default history size
	public static final int DEFAULT_HISTORY_SIZE = 50;

	public ShellHistoryImpl() {
		this(DEFAULT_HISTORY_SIZE);
	}

	public ShellHistoryImpl(int historySize) {
		setHistory(new String[historySize]);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#appendHistory(java.lang.Object)
	 */
	public synchronized void appendHistory(Object newHistory) {
		// if history is 0 or smaller return since can't append 
		if (getHistory().length <= 0) {
			return;
		}

		try { // Shouldn't really get errors when appending history so use
			  // try/catch
			this.setHistoryAt(getIndex(), newHistory); // not throws
			setIndex((getIndex() + 1) % getHistory().length);
			// reset our peek index
			setPeekIndex(0);
		} catch (ShellHistoryException ex) {
			logger.error(ex);
		}		
	}

	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#get(int)
	 */
	public synchronized Object get(int i) throws ShellHistoryException {
		// if hisotry size is 0
		if (getHistory().length <= 0) {
			return null;
		}
		// if i is out of bounds
		if (-i > getCount() || i == 0 || i > getCount()) { 
			throw new ShellHistoryException("Error: Cannot access history at "
					+ i + " it is out of bounds");
		}
        // If i is negative and inbounds
		if (i < 0) { 
			int index = getIndex();
			int cnt = 0;
			int distance = -i;
			while (cnt < distance) {
				index = ((index == 0) ? getHistory().length : index) - 1;
				if (getHistoryAt(index) != null) {
					cnt++;
				}
			}			
			return getHistoryAt(index);
		} 
        // If i in bounds and positive (zero would throw exeption at
		// the top)
		else { 
			int count = i;
			int index = getIndex();
			while (count > 0) {
				index = ++index % getHistory().length;
				if (getHistoryAt(index) != null) {
					count--;
				}
			}
			return this.getHistoryAt(index);
		}
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#getCount()
	 */
	public synchronized int getCount() {
		return count;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#getLast()
	 */
	public synchronized Object getLast() throws ShellHistoryException {
		return this.get(-1);
	}	
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#setHistoryAt(int, java.lang.Object)
	 */
	public synchronized void setHistoryAt(int i, Object value)
			throws ShellHistoryException {
		if (getHistory().length <= 0)
			return;

		if (i >= 0 && i < getHistory().length) {
			if (getHistoryAt(i) == null)
				incCount();
			this.history[i] = value;
		} else {
			throw new ShellHistoryException("Error: Cannot set history at '"
					+ i + "' history size is only of length '"
					+ getHistory().length + "'");
		}
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#setHistorySize(int)
	 */
	public synchronized void setHistorySize(int value)
			throws ShellHistoryException {
		if (value >= 0) {
			if (getHistory() != null && getHistory().length != value) {
				setHistory(getHistoryOnSetSize(value));
			}
		} else {
			throw new ShellHistoryException(
					"Error: History must be a positive number (reseting to default)");
		}
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#getHistorySize()
	 */
	public synchronized int getHistorySize() {
		return this.getHistory().length;
	}
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public synchronized String toString() {
		String history = "";
		for (int i = 0; i < getHistory().length; i++) {
			try {
				history += getHistoryAt(i) + ", ";
			} catch (ShellHistoryException ex) {
			}
		}
		return "[" + history + "]" + " | count=" + getCount();
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#peek()
	 */
	public synchronized Object peek() {
		try {
		  if(getPeekIndex() != 0) {
		    return get(getPeekIndex());
		  }else {
		  	return null;
		  }
		} catch (ShellHistoryException e) {
		  logger.error(e);
		}
		return "";
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#incPeekIndex()
	 */
	public synchronized boolean incPeekIndex() {		
		int historyCount = getCount();
		if(peekIndex < historyCount) {
			peekIndex = peekIndex+1;
			return true;
		}else {
			return false;
		}
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#decPeekIndex()
	 */
	public synchronized boolean decPeekIndex() {
		int historyCount = getCount();
		if(peekIndex > -historyCount) {
			peekIndex = peekIndex - 1;
			return true;
		}else {
			return false;
		}
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#getHistory()
	 */
	public synchronized Object[] getHistory() {
		return this.history;
	}
	
	/**
	 * Gets the peek index
	 * @return
	 */
	private synchronized int getPeekIndex() {
		return this.peekIndex;
	}
	/**
	 * Gets the current index
	 * @return
	 */

	private synchronized int getIndex() {
		return this.index;
	}

	/**
	 * Gets this history at index i
	 * 
	 * @param i
	 *            int
	 * @throws ShellHistoryException
	 * @return Object
	 */
	private synchronized Object getHistoryAt(int i)
			throws ShellHistoryException {
		if (i >= 0 && i < getHistory().length) {
			return this.history[i];
		} else {
			throw new ShellHistoryException("Error: Cannot get history at '"
					+ i + "' history size is only of length '"
					+ getHistory().length + "'");
		}
	}

	/**
	 * returns the last history object and removes it (used in resizing history)
	 * 
	 * @throws ShellHistoryException
	 * @return Object
	 */
	private synchronized Object popHistory() throws ShellHistoryException {
		setIndex(((getIndex() == 0) ? getHistory().length : getIndex()) - 1);
		Object popValue = getHistoryAt(getIndex());
		setHistoryAt(getIndex(), null);
		decCount();
		return popValue;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.ShellHistory#setHistory(java.lang.Object[])
	 */
	public synchronized void setHistory(Object[] value) {
		this.history = value;
	}

	/**
	 * This function just returns a new Object[] history when you resize the
	 * history
	 * 
	 * @param value
	 *            int
	 * @return Object[]
	 */
	private Object[] getHistoryOnSetSize(int value) {
		Object[] newHistory = new Object[value];
		if (getHistory() == null || getHistory().length == 0) { // if previous
																// history was
																// null just
																// return an
																// empty history
			return newHistory;
		}

		int popedItems = 0;
		int startIndex;
		if (getCount() >= newHistory.length - 1) { // find the starting index
			startIndex = newHistory.length - 1; //   (where to put most recent
												// history)
		} else {
			startIndex = getCount() - 1;
		}
		// Start at most recent history and work backwards till run out of room
		for (int i = startIndex; i >= 0 && getCount() > 0; i--) {
			try {
				newHistory[i] = this.popHistory();
				popedItems++;
			} catch (ShellHistoryException ex) {
				logger
						.error("Error: Could not recover part or all of the history");
			}
		}

		setIndex((popedItems >= newHistory.length) ? 0 : popedItems);
		setCount(popedItems);

		return newHistory;
	}

	/**
	 * This is a set method for count, it is used to specify how many items are
	 * currently in the history
	 * 
	 * @param value
	 *            int
	 */
	private synchronized void setCount(int value) {
		count = value;
	}

	/**
	 * this increases the count unless the history is full
	 */
	private synchronized void incCount() {
		this.count = (getCount() + 1 > getHistory().length) ? getCount()
				: getCount() + 1;
	}

	/**
	 * this decreases the count unless the history is empty
	 */
	private synchronized void decCount() {
		this.count = (getCount() > 0) ? getCount() - 1 : 0;
	}

	/**
	 * This sets the index for the location of the history
	 * 
	 * @param value
	 *            int
	 */
	private synchronized void setIndex(int value) {
		this.index = value;
	}
	
	private synchronized void setPeekIndex(int value) {
		this.peekIndex = value;
	}

	/***************************************************************************
	 * main is used to test this class
	 * 
	 * @param args
	 *            String[]
	 * @throws ShellHistoryException
	 **************************************************************************/
	public static void main(String[] args) throws ShellHistoryException {
		logger.setLevel(Level.DEBUG);
		ShellHistoryImpl history = new ShellHistoryImpl();
		for (int i = 0; i < 2; i++) {
			logger.debug(history);
			history.appendHistory(i + "");
		}

		history.setHistorySize(5);

		for (int i = 0; i < 8; i++) {
			logger.debug(history.toString());
			history.appendHistory(i + "");
		}

		for (int i = -1; i >= -history.getCount(); i--) {
			logger.debug("i=" + i + " | " + history.get(i));
		}

		logger.debug(history.getCount() + "");

		for (int i = 1; i < history.getCount(); i++) {
			logger.debug("i=" + i + " | " + history.get(i));
		}

		logger.debug(history);

		logger
				.debug("-------------Checking to see if errors are thrown------------");

		try {
			logger.debug(history.get(0));
			logger.error("Error: Failed to produce expected exception");
		} catch (ShellHistoryException e) {
			logger
					.debug("Caught Expected Exception when calling history.get(0)");
		}
		int num;
		try {
			num = -history.getCount() - 1;
			logger.debug(history.get(num));
			logger.error("Error: Failed to produce expected exception");
		} catch (ShellHistoryException e) {
			num = -history.getCount() - 1;
			logger.debug("Caught Expected Exception when calling history.get("
					+ num + ")");
		}
		try {
			num = history.getCount() + 1;
			logger.debug(history.get(num));
			logger.error("Error: Failed to produce expected exception");
		} catch (ShellHistoryException e) {
			logger.debug("Caught Expected Exception when calling history.get("
					+ num + ")");
		}
		
		logger.debug("------------------checking peek---------------------------");
		for(int i=0;i<history.getCount()/2;i++) {
		  logger.debug("index="+history.getIndex());
		  history.decPeekIndex();
	      logger.debug("Peek["+history.getPeekIndex()+"]"+history.peek());		  	
		}
		
		logger.debug("appending 'peek' to history");
		history.appendHistory("peek");
		logger.debug(history);
		
		for(int i=0;i<history.getCount();i++) {
	      logger.debug("index="+history.getIndex());
	      history.decPeekIndex();	
		  logger.debug("Peek["+history.getPeekIndex()+"]"+history.peek());	      
		}
		
	}
}