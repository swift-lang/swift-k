//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 30, 2005
 */
package org.globus.cog.coaster.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.RemoteConfiguration;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.TestCommand;
import org.globus.cog.coaster.handlers.TestHandler;
import org.globus.cog.coaster.handlers.TestHandler.TestCallback;

public class ChannelTest implements TestCallback {
	private final static Logger logger = Logger.getLogger(ChannelTest.class);

	private int seq, index;
	private boolean done;
	private static MonitorWindow mw;

	public void run() {
		seq = 0;
		done = false;
		TestHandler.setCallback(this);
		done(null);
	}

	public static void main(String[] args) {
		mw = new MonitorWindow(new String[] { "Persistent: ", "Polling: ", "Callback: " }, 10);
		mw.pack();
		mw.setLocation(300, 300);
		mw.setSize(300, 100);
		mw.show();
		for (int i = 0; i < 10; i++) {
			ChannelTest ct = new ChannelTest(i);
			ct.run();
			while (!ct.done) {
				try {
					Thread.sleep(1000);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		//System.exit(0);
	}

	public ChannelTest(int i) {
		this.index = i;
	}

	public synchronized void done(CoasterChannel ochannel) {
		try {
			logger.info("=========================== " + index + " ===========================");
			if (seq > 0) {
				mw.setState(seq - 1, index, 2);
			}
			if (seq < 3) {
				mw.setState(seq, index, 1);
			}
			if (seq == 0) {
				logger.info("Testing persistent channel...");
				RemoteConfiguration.Entry entry = new RemoteConfiguration.Entry(
						".*localhost:50001", "keepalive(600), reconnect");
				RemoteConfiguration.getDefault().prepend(entry);
				CoasterChannel channel = ChannelManager.getManager().reserveChannel(
						"https://localhost:50001", null);
				ChannelManager.getManager().reserveLongTerm(channel);
				logger.debug("Channel config: " + channel.getChannelContext().getConfiguration());
				TestCommand test = new TestCommand();
				test.execute(channel);
				logger.debug("Test command executed");
				ChannelManager.getManager().releaseChannel(channel);
				ChannelManager.getManager().releaseLongTerm(channel);
				logger.debug("Channel released");
			}
			else if (seq == 1) {
				ChannelManager.getManager().releaseLongTerm(ochannel);
				logger.info("OK");
				logger.info("Testing polling ...");
				RemoteConfiguration.Entry entry = new RemoteConfiguration.Entry(
						".*localhost:50002", "poll(10)");
				RemoteConfiguration.getDefault().prepend(entry);
				CoasterChannel channel = ChannelManager.getManager().reserveChannel(
						"https://localhost:50002", null);
				ChannelManager.getManager().reserveLongTerm(channel);
				logger.debug("Channel config: " + channel.getChannelContext().getConfiguration());
				TestCommand test = new TestCommand();
				test.execute(channel);
				logger.debug("Test command executed");
				ChannelManager.getManager().releaseChannel(channel);
				logger.debug("Channel released");
			}
			else if (seq == 2) {
				//ChannelManager.getManager().releaseLongTerm(ochannel);
				//logger.info("OK");
				logger.info("Testing callback ...");
				RemoteConfiguration.Entry entry = new RemoteConfiguration.Entry(
						".*localhost:50003", "callback");
				RemoteConfiguration.getDefault().prepend(entry);
				CoasterChannel channel = ChannelManager.getManager().reserveChannel(
						"https://localhost:50003", null);
				ChannelManager.getManager().reserveLongTerm(channel);
				logger.debug("Channel config: " + channel.getChannelContext().getConfiguration());
				TestCommand test = new TestCommand();
				test.execute(channel);
				logger.debug("Test command executed");
				ChannelManager.getManager().releaseChannel(channel);
			}
			else {
				logger.info("OK");
				done = true;
			}
		}
		catch (Exception e) {
			mw.setState(seq, index, 3);
			e.printStackTrace();
			done = true;
		}
		finally {
			seq++;
		}
	}

	public static class MonitorWindow extends JFrame {
		private final Square[][] s;

		public MonitorWindow(String[] labels, int count) {
			getContentPane().setLayout(new GridLayout(0, 1));
			s = new Square[labels.length][count];
			for (int i = 0; i < labels.length; i++) {
				JPanel rp = new JPanel();
				rp.setLayout(new FlowLayout());
				getContentPane().add(rp);
				JLabel l = new JLabel(labels[i]);
				l.setPreferredSize(new Dimension(100, 20));
				rp.add(l);
				JPanel sp = new JPanel();
				sp.setLayout(new GridLayout(1, 0));
				rp.add(sp);
				for (int j = 0; j < count; j++) {
					s[i][j] = new Square();
					sp.add(s[i][j]);
					s[i][j].setPreferredSize(new Dimension(8, 8));
					s[i][j].setSize(8, 8);
				}
			}
		}

		public void setState(int i, int j, int state) {
			s[i][j].setState(state);
		}
	}

	public static class Square extends JComponent {
		private static Color[] colors = new Color[] { Color.GRAY, Color.YELLOW, Color.GREEN,
				Color.RED };
		private int state;

		public void paint(Graphics g) {
			g.setColor(colors[state]);
			g.fillRect(0, 0, 7, 7);
		}

		public void setState(int state) {
			if (this.state > state) {
				return;
			}
			this.state = state;
			repaint();
		}
	}
}
