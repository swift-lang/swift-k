
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.panels;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JLabel;

import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.SimpleGridLayout;

public class InstructionsPanel extends GridContainer {
	private List instructionsList;
	
	public InstructionsPanel(String instructions) {
		super();
		setLayout(new FlowLayout());
		instructionsList = new ArrayList();

		StringTokenizer ST = new StringTokenizer(instructions, "\n");
		while (ST.hasMoreTokens()) {
			instructionsList.add(ST.nextToken());
		}

		setGridSize(instructionsList.size(), 1);

		for (int i = 0; i < instructionsList.size(); i++) {
			JLabel Label = new JLabel((String) instructionsList.get(i));
			add(Label);
		}

		setPreferredSize(new Dimension(SimpleGridLayout.Expand, instructionsList.size() * 24));
	}
}