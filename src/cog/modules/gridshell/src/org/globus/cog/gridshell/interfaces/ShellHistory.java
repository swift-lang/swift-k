/*
 * Keeps track of the Shell's History up to a specified number of items
 */
package org.globus.cog.gridshell.interfaces;

import java.io.Serializable;

import org.globus.cog.gridshell.model.ShellHistoryException;

/**
 * 
 */
public interface ShellHistory extends Serializable {
	/**
	 * If history is larger than 0, then it appends newHistory to the history
	 * which may override values
	 * 
	 * @param newHistory -
	 *            the history object to append
	 */
	void appendHistory(Object newHistory);

	/**
	 * Will decrement the peek index which ranges from -historySize to
	 * historySize
	 * 
	 * @return - if it fails to decrement (because peek index goes out of range)
	 *         returns false, else true
	 */
	boolean decPeekIndex();

	/**
	 * returns if(i>0) the ith command since the shell is active. if(i <0) it is
	 * the ith command since the last submited command else returns null
	 * 
	 * @param i -
	 *            specifies which command to return
	 * @return - the ith command since the shell is active
	 */
	Object get(int i) throws ShellHistoryException;

	/**
	 * Gets the number of items in the history
	 * 
	 * @return
	 */
	int getCount();

	/**
	 * returns the history as Object[]
	 * 
	 * @return
	 */
	Object[] getHistory();

	/**
	 * Gets the max items for the history
	 * 
	 * @return
	 */
	int getHistorySize();

	/**
	 * Gets the last history value
	 * 
	 * @return @throws
	 *         ShellHistoryException - if now items in history
	 */
	Object getLast() throws ShellHistoryException;

	/**
	 * Will increment the peek index which ranges from -historySize to
	 * historySize
	 * 
	 * @return - if it fails to increment (because peek index goes out of range)
	 *         returns false, else true
	 */
	boolean incPeekIndex();

	/**
	 * Looks at Object get(peekIndex) if peekIndex is 0 returns null
	 * 
	 * @return
	 */
	Object peek();

	/**
	 * Set the history to a Object[]
	 * 
	 * @param value
	 */
	void setHistory(Object[] value);

	/**
	 * Set the history value at index i
	 * 
	 * @param i -
	 *            index to set the history at
	 * @param value -
	 *            value to set
	 * @throws ShellHistoryException -
	 *             if index is out of bounds
	 */
	void setHistoryAt(int i, Object value) throws ShellHistoryException;

	/**
	 * Resizes the history and preserves as many history objects as permitted by
	 * the new history size
	 * 
	 * @param value
	 * @throws ShellHistoryException
	 */
	void setHistorySize(int value) throws ShellHistoryException;

}