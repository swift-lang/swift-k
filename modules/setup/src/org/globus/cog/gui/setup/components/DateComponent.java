
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.components;

import java.awt.Dimension;

import org.globus.cog.gui.setup.controls.DateInputControl;
import org.globus.cog.gui.util.GridPosition;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.common.CoGProperties;

public class DateComponent extends AbstractSetupComponent implements SetupComponent {

	private DateInputControl date;
	private CoGProperties properties;

	public DateComponent(CoGProperties Properties) {
		super("Date", "text/setup/date.txt");
		this.properties = Properties;

		date = new DateInputControl();
		date.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 54));

		add(date, new GridPosition(2, 0));
	}


	public boolean verify() {
		if (!super.verify()) {
			return false;
		}

		if (date.offsetBigger(1000)) {
			setErrorMessage("Your system clock does not appear to be synchronized.\n" + "A difference of more than 1000ms was detected.");
			return false;
		}

		return true;
	}

  public void enter(){
    date.start();
    super.enter();
  }

  public boolean leave(){
    if(super.leave()){
      date.stop();
      return true;
    }
    return false;
  }
}
