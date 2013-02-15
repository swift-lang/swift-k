// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.panels;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;

import org.globus.cog.gui.setup.components.CertificateAuthorityComponent;
import org.globus.cog.gui.setup.components.DateComponent;
import org.globus.cog.gui.setup.components.IPAddressComponent;
import org.globus.cog.gui.setup.components.LicenseComponent;
import org.globus.cog.gui.setup.components.LocalProxyComponent;
import org.globus.cog.gui.setup.components.PreviousSetupComponent;
import org.globus.cog.gui.setup.components.PrivateKeyComponent;
import org.globus.cog.gui.setup.components.PropertiesFileComponent;
import org.globus.cog.gui.setup.components.RegistrationComponent;
import org.globus.cog.gui.setup.components.SetupComponent;
import org.globus.cog.gui.setup.components.UserCertificateComponent;
import org.globus.cog.gui.setup.controls.ComponentListItem;
import org.globus.cog.gui.setup.events.ComponentStatusChangedEvent;
import org.globus.cog.gui.setup.events.ComponentStatusChangedListener;
import org.globus.cog.gui.setup.events.NavActionListener;
import org.globus.cog.gui.setup.events.NavEvent;
import org.globus.cog.gui.setup.util.ComponentLabelBridge;
import org.globus.cog.gui.setup.util.MyOverlayLayout;
import org.globus.common.CoGProperties;

/**
 * The panel handling the components
 */
public class ComponentPanel extends JPanel implements ComponentStatusChangedListener,
		NavActionListener {
	private LinkedList setupComponents;
	private ComponentLabelBridge visibleComponent = null;
	private ListPanel listPanel;
	private JLabel titleLabel;
	private NavPanel nav;
	private CoGProperties properties;
	private ProgressMonitor progress;
	private int step;

	public ComponentPanel(JLabel titleLabel, ListPanel listPanel, NavPanel nav) {
		super();

		properties = CoGProperties.getDefault();

		this.titleLabel = titleLabel;
		this.listPanel = listPanel;
		this.nav = nav;

		nav.addNavEventListener(this);
		listPanel.addNavEventListener(this);

		setLayout(new MyOverlayLayout());
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		setupComponents = new LinkedList();

		progress = new ProgressMonitor(this, "Loading components...", null, 0, 10);
		step = 0;

		ComponentLabelBridge license, registration, previousSetup, userCertificate, privateKey; 
		ComponentLabelBridge certificateAuthority, localProxy, ipAddress, date, propertiesFile;

		license = bridge(new LicenseComponent());
		registration = bridge(new RegistrationComponent());
		previousSetup = bridge(new PreviousSetupComponent(properties));
		userCertificate = bridge(new UserCertificateComponent(properties));
		privateKey = bridge(new PrivateKeyComponent(properties));
		certificateAuthority = bridge(new CertificateAuthorityComponent(properties));
		localProxy = bridge(new LocalProxyComponent(properties));
		ipAddress = bridge(new IPAddressComponent(properties));
		date = bridge(new DateComponent(properties));
		propertiesFile = bridge(new PropertiesFileComponent(properties));

		dependency(registration, license);
		dependency(previousSetup, license);
		dependency(userCertificate, license);
		dependency(privateKey, license);
		dependency(certificateAuthority, license);
		dependency(localProxy, license);
		dependency(ipAddress, license);
		dependency(date, license);
		dependency(propertiesFile, license);

		addSetupComponent(license);
		addSetupComponent(registration);
		addSetupComponent(previousSetup);
		addSetupComponent(userCertificate);
		addSetupComponent(privateKey);
		addSetupComponent(certificateAuthority);
		addSetupComponent(localProxy);
		addSetupComponent(ipAddress);
		addSetupComponent(date);
		addSetupComponent(propertiesFile);
		progress.close();

		showComponent(0);
		componentStatusChanged(null);
	}

	private ComponentLabelBridge bridge(SetupComponent c) {
		progress.setProgress(step++);
		return new ComponentLabelBridge(c);
	}

	private void dependency(ComponentLabelBridge target, ComponentLabelBridge source) {
		target.getSetupComponent().addDependency(source.getSetupComponent());
	}

	public void addSetupComponent(ComponentLabelBridge CLB) {
		setupComponents.add(CLB);
		JComponent JC = (JComponent) CLB.getSetupComponent().getVisualComponent();
		add(JC);
		listPanel.addItem(CLB.getComponentListItem());
		CLB.getSetupComponent().addComponentStatusChangedListener(this);
	}

	public void addComponentStatusChangedListener(ComponentStatusChangedListener CSCL) {
		listenerList.add(ComponentStatusChangedListener.class, CSCL);
	}

	public void fireComponentStatusChangedEvent(ComponentStatusChangedEvent e) {
		Object[] listeners = listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ComponentStatusChangedListener.class) {
				((ComponentStatusChangedListener) listeners[i + 1]).componentStatusChanged(e);
			}
		}

	}

	public void showComponent(String title) {
		Iterator components = setupComponents.listIterator();

		while (components.hasNext()) {
			ComponentLabelBridge crtBridge = (ComponentLabelBridge) components.next();
			SetupComponent crtComp = crtBridge.getSetupComponent();

			if (crtComp.getTitle().compareTo(title) == 0) {
				if (visibleComponent != null) {
					if (!visibleComponent.getSetupComponent().leave()) {
						return;
					}
				}
				crtComp.enter();
				visibleComponent = crtBridge;
				break;
			}
		}

	}

	/**
	 * Shows the component at the specified index, making sure the active
	 * component is de-activated nicely
	 * 
	 * @param index
	 *            Description of the Parameter
	 */
	public void showComponent(int index) {
		if ((index > setupComponents.size()) || (index < 0)) {
			return;
		}

		ComponentLabelBridge crtBridge = (ComponentLabelBridge) setupComponents.get(index);
		SetupComponent crtComp = crtBridge.getSetupComponent();

		if (visibleComponent != null) {
			if (!visibleComponent.getSetupComponent().leave()) {
				return;
			}
			visibleComponent.getComponentListItem().setActive(false);

		}
		crtComp.enter();
		visibleComponent = crtBridge;
		visibleComponent.getComponentListItem().setActive(true);
		updateControls();
	}

	public void nextComponent() {
		if (visibleComponent == null) {
			showComponent(0);
			return;
		}

		int next = getNext(getVisibleComponentIndex());

		if (next != -1) {
			showComponent(next);
		}
		return;
	}

	public void prevComponent() {
		if (visibleComponent == null) {
			showComponent(0);
			return;
		}

		int prev = getPrev(getVisibleComponentIndex());

		if (prev != -1) {
			showComponent(prev);
		}
		return;
	}

	public boolean nextAvailable() {
		if (visibleComponent == null) {
			return true;
		}

		if (getNext(getVisibleComponentIndex()) != -1) {
			return true;
		}

		return false;
	}

	public boolean prevAvailable() {
		if (visibleComponent == null) {
			return true;
		}

		if (getPrev(getVisibleComponentIndex()) != -1) {
			return true;
		}

		return false;
	}

	private int getVisibleComponentIndex() {
		for (int i = 0; i < setupComponents.size(); i++) {
			if (setupComponents.get(i) == visibleComponent) {
				return i;
			}
		}
		return -1;
	}

	public SetupComponent getVisibleComponent() {
		return visibleComponent.getSetupComponent();
	}

	private int getPrev(int index) {
		for (int j = index - 1; j >= 0; j--) {
			ComponentLabelBridge CLB = (ComponentLabelBridge) setupComponents.get(j);
			ComponentListItem CLI = CLB.getComponentListItem();

			if (CLI.getState() != ComponentListItem.StateDisabled) {
				return j;
			}
		}
		return -1;
	}

	private int getNext(int index) {
		for (int j = index + 1; j < setupComponents.size(); j++) {
			ComponentLabelBridge CLB = (ComponentLabelBridge) setupComponents.get(j);
			ComponentListItem CLI = CLB.getComponentListItem();

			if (CLI.getState() != ComponentListItem.StateDisabled) {
				return j;
			}
		}
		return -1;
	}

	public void componentStatusChanged(ComponentStatusChangedEvent e) {
		updateControls();
	}

	private void updateControls() {
		for (int i = 0; i < setupComponents.size(); i++) {
			if (setupComponents.get(i) == visibleComponent) {
				visibleComponent.getComponentListItem().setState(ComponentListItem.StateNone);
				continue;
			}

			SetupComponent SC = ((ComponentLabelBridge) setupComponents.get(i)).getSetupComponent();
			ComponentListItem LI = ((ComponentLabelBridge) setupComponents.get(i)).getComponentListItem();
			LinkedList Deps = SC.getDependencies();
			boolean DependOk = true;

			for (int j = 0; j < Deps.size(); j++) {
				SetupComponent Dep = (SetupComponent) Deps.get(j);

				if (!Dep.verify()) {
					DependOk = false;
					break;
				}
			}
			if (DependOk) {
				if (SC.completed()) {

					if (SC.verify()) {
						LI.setState(ComponentListItem.StateOk);
					}
					else {
						LI.setState(ComponentListItem.StateFailed);
					}

				}
				else {
					LI.setState(ComponentListItem.StateNone);
				}
			}
			else {
				LI.setState(ComponentListItem.StateDisabled);
			}
		}
		nav.setNextEnabled(nextAvailable());
		nav.setPrevEnabled(prevAvailable());
		if (visibleComponent.getSetupComponent().canFinish()) {
			nav.setFinishEnabled(true);
		}
		else {
			nav.setFinishEnabled(false);
		}
		titleLabel.setText(visibleComponent.getSetupComponent().getTitle());
	}

	public void navAction(NavEvent e) {
		int Action = e.getNavAction();

		if (Action == NavEvent.Next) {
			nextComponent();
		}
		else if (Action == NavEvent.Prev) {
			prevComponent();
		}
		else if (Action == NavEvent.Jump) {
			showComponent(e.getJumpIndex());
		}
	}
}
