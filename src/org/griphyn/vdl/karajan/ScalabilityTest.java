/*
 * Created on Jun 20, 2006
 */
package org.griphyn.vdl.karajan;

import java.awt.GridLayout;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.globus.cog.karajan.Loader;
import org.globus.cog.karajan.arguments.AbstractWriteOnlyVariableArguments;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.EventListener;

public class ScalabilityTest {
	private static volatile int jobsSubmitted, jobsFailed, jobsCompleted, workflowsStarted,
			workflowsFailed, workflowsCompleted;

	private static int sleepTime;

	private static long startTime, endTime;

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: my-name <instanceCount> <jobCount> <jobSleepTime>");
			System.exit(1);
		}
		startTime = System.currentTimeMillis();
		sleepTime = Integer.parseInt(args[2]);
		int instanceCount = Integer.parseInt(args[0]);
		for (int i = 0; i < instanceCount; i++) {
			try {
				ElementTree tree = Loader.load("scalability.k");
				ExecutionContext ec = new ExecutionContext(tree);
				ec.setStdout(new Stdout());
				List l = new LinkedList();
				l.add(args[1]);
				l.add(args[2]);
				ec.setArguments(l);
				ec.addEventListener(new Listener());
				ec.start();
				workflowsStarted++;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		Monitor m = new Monitor();
		while (workflowsStarted > workflowsCompleted + workflowsFailed) {
			try {
				m.update();
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				break;
			}
		}
		m.update();
		endTime = System.currentTimeMillis();
		System.out.println("Workflows started: " + workflowsStarted);
		System.out.println("Workflows completed: " + workflowsCompleted);
		System.out.println("Workflows failed: " + workflowsFailed);
		System.out.println("Jobs started: " + jobsSubmitted);
		System.out.println("Jobs completed: " + jobsCompleted);
		System.out.println("Jobs failed: " + jobsFailed);
		System.out.println("Total time: " + (endTime - startTime) / 1000 + "s");
		System.out.println("Total time excluding sleep time: "
				+ ((endTime - startTime) / 1000 - sleepTime) + "s");
	}

	public static class Stdout extends AbstractWriteOnlyVariableArguments {
		public void merge(VariableArguments args) {
			appendAll(args.getAll());
		}

		public void append(Object value) {
			if (".".equals(value)) {
				jobsSubmitted++;
			}
			else if ("O".equals(value)) {
				jobsCompleted++;
			}
			else if ("-".equals(value)) {
				jobsFailed++;
			}
			System.out.print(value);
		}

		public void appendAll(List args) {
			Iterator i = args.iterator();
			while (i.hasNext()) {
				append(i.next());
			}
		}

		public boolean isCommutative() {
			return true;
		}
	}

	public static class Listener implements EventListener {
        public void completed(VariableStack stack) throws ExecutionException {
        	workflowsCompleted++;
        }

        public void failed(VariableStack stack, ExecutionException e)
                throws ExecutionException {
        	workflowsFailed++;
        }
	}

	public static class Monitor {
		private JFrame frame;
		private JLabel time, js, jf, jc, ws, wf, wc;

		public Monitor() {
			frame = new JFrame();
			JPanel p = new JPanel();
			p.setLayout(new GridLayout(0, 2));
			p.add(new JLabel("Time: "));
			p.add(time = new JLabel());

			p.add(new JLabel("Workflows started: "));
			p.add(ws = new JLabel());
			p.add(new JLabel("Workflows failed: "));
			p.add(wf = new JLabel());
			p.add(new JLabel("Workflows completed: "));
			p.add(wc = new JLabel());

			p.add(new JLabel("Jobs started: "));
			p.add(js = new JLabel());
			p.add(new JLabel("Jobs failed: "));
			p.add(jf = new JLabel());
			p.add(new JLabel("Jobs completed: "));
			p.add(jc = new JLabel());

			frame.getContentPane().add(p);
			frame.setSize(300, 200);
			frame.setVisible(true);
		}

		public void update() {
			time.setText(String.valueOf((System.currentTimeMillis() - startTime) / 1000) + "s");

			ws.setText(String.valueOf(workflowsStarted));
			wf.setText(String.valueOf(workflowsFailed));
			wc.setText(String.valueOf(workflowsCompleted));

			js.setText(String.valueOf(jobsSubmitted));
			jf.setText(String.valueOf(jobsFailed));
			jc.setText(String.valueOf(jobsCompleted));
		}
	}

}
