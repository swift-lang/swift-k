
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl;

import org.globus.cog.gridface.interfaces.GridFace;
import java.util.Date;

public class GridFaceImpl implements GridFace {
  private Date lastUpdateTime = new Date(System.currentTimeMillis());
  private String label,name;

  public GridFaceImpl() {
    setLabel("");
    setName("");
  }

  /**
   * lastUpdateTime
   *
   * @return Date
   */
  public synchronized Date lastUpdateTime() {
    return this.lastUpdateTime;
  }

  /**
   * register
   *
   * @param connection GridFace
   */
  public synchronized void register(GridFace connection) {
    System.err.println("Not sure what this does");
  }

  /**
   * setLabel
   *
   * @param label String
   */
  public synchronized void setLabel(String label) {
    this.label = label;
  }

  /**
   * setName
   *
   * @param name String
   */
  public synchronized void setName(String name) {
    this.name = name;
  }

  /**
   * update
   */
  public synchronized void update() {
    this.lastUpdateTime.setTime(System.currentTimeMillis());
  }
}
