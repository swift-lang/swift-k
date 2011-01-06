/*
 * 
 */
package org.globus.cog.gridface.impl.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.globus.cog.gridface.impl.texteditor.TextEditor;
import org.globus.cog.gui.util.UITools;


/**
 * Configure this as you would any log4j appender, but it is recommended to use only
 * for error messages because it pops a dialog up each time a logging event is received.
 * 
 * <pre>
 * Example configuration:
 * 
 * #############
 * #Root category
 * #############
 * log4j.rootCategory=DEBUG, ERROR-DIALOG
 * log4j.debug=true
 * 
 * #############
 * # ERROR-DIALOG is an error dialog for the desktop that allows errors to be emailed
 * #############
 * log4j.appender.ERROR-DIALOG=org.globus.cog.gridface.impl.util.ErrorDialogAppender
 * # only use for events >= WARN
 * log4j.appender.ERROR-DIALOG.level=WARN
 * # the pattern for the message display
 * log4j.appender.ERROR-DIALOG.messageConversionPattern=%d %-5p [%t] %C{2} (%F:%L) - %m%n
 * # a url to post the data to
 * # this url should handle the following parameters: message, location, level, and throwable
 * log4j.appender.ERROR-DIALOG.PostURL=http://some-domain.com/cgi-bin/script.cgi
 * # the layout of the details display
 * log4j.appender.ERROR-DIALOG.layout=org.apache.log4j.HTMLLayout
 * </pre>
 * 
 */
public class ErrorDialogAppender extends AppenderSkeleton {
	private final static String EOL = System.getProperty("line.separator");
	
	/** the url that we post our data to */
	protected String postURL = null;
	
	/** the overriden level that this dialog uses */
	protected Level level = null;	
	/** the loggernames we want to skip */
	protected String excludeLoggerNames = null;
	/** the layout of the header */
	protected PatternLayout messageLayout = new PatternLayout();
	/** the pattern of the header */
	protected String messageConversionPattern = "%-5p [%c] %x - %m%n";
	
	/**
	 * A Dialog that displays appended log4j events
	 */
	public class Log4JDialog {
		
		private JOptionPane optionPane = new JOptionPane();
		private JDialog dialog;
		private JPanel main,hideDetailsPanel,showDetailsPanel; 
		
		
		private JButton detailsButton;
		private JScrollPane detailsScrollPane;
		
		// actions for changing states
		private ActionListener showDetailsAListener = new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						showDetailsState();
					}					
				});
			}
		};
		private ActionListener hideDetailsAListener = new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						hideDetailsState();
					}					
				});
			}
		};
			
		private Dimension hideDetailsSize, showDetailsSize;
		
		private LoggingEvent logEvent;
		private JTextArea messageTextArea;				
		
		public Log4JDialog(LoggingEvent logEvent, int WIDTH, int HEIGHT) {
			this.logEvent = logEvent;
			
			hideDetailsSize = new Dimension(WIDTH,HEIGHT);
			showDetailsSize = new Dimension(WIDTH,HEIGHT*2);
			
			// Init panels		
			main = new JPanel(new BorderLayout());			
			hideDetailsPanel = new JPanel(new BorderLayout());
			showDetailsPanel = new JPanel(new BorderLayout());
			
			// put in a question to ask
			JLabel submitBug = new JLabel("Send this bug?");
			submitBug.setHorizontalAlignment(JLabel.CENTER);
			main.add(submitBug,BorderLayout.SOUTH);
					
			
			// Create the message
			messageTextArea = new JTextArea();
			if(getMessageLayout() != null) {
			  messageTextArea.setText( getMessageLayout().format(logEvent) );
			}
			JScrollPane messageScrollPane = new JScrollPane(messageTextArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			messageTextArea.setCaretPosition(0);
			
			// add to the hideDetailsPanel
			hideDetailsPanel.add(new JLabel("Message:"),BorderLayout.NORTH);
			hideDetailsPanel.add(messageScrollPane,BorderLayout.CENTER);
			
			// Create a details button
			detailsButton = new JButton();
			hideDetailsPanel.add(detailsButton,BorderLayout.SOUTH);
			
			// Create the throwable content for details			
			JEditorPane detailsText = new JEditorPane();
			detailsText.setContentType("text/html");
			detailsText.setText(getDetailedMessage());
			
			detailsScrollPane = new JScrollPane(detailsText,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
									
			optionPane.setMessage(main);
			optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
			
			dialog = optionPane.createDialog(null,"Log4JDialog - Alert Appender");
			
			// init state
			hideDetailsState();		
		}
		
		/**
		 * The detailed message to display
		 * @return
		 */
		public String getDetailedMessage() {
	       return getLayout().getHeader()+getLayout().format(logEvent)+getLayout().getFooter();
		}
		
		public void okButtonPushed() {
			if(logEvent == null) {
				return;
			}
			// init our arguments
			HashMap args = new HashMap();
			args.put("message",logEvent.getMessage());
			args.put("location",logEvent.getLocationInformation().fullInfo);
			args.put("level",logEvent.getLevel().toString());
			
			String exceptionString = null;			
			// ensure there is a throwable to process
			if(logEvent.getThrowableInformation() != null) {
				// if there is get the string value
				exceptionString = LoggerImpl.getExceptionString(logEvent.getThrowableInformation().getThrowable());
			}else {
				// otherwise it is null
				exceptionString = null;
			}
			args.put("throwable",exceptionString);
			
			try {
				// try and submit
				String html = new HTTPPost().post(getPostURL(),args);
				createDialog("Your submission results",html);				
			} catch (MalformedURLException exception) {
				LogLog.error("Failed to submit results",exception);
				createDialog("Failed to submit results",
						"<h1>Could not submit results</h1><h2>"+ exception +
						"</h2><p>Please ensure you have specified a valid PostURL in log4j.properties (ie log4j.appender.ERROR-DIALOG.PostURL=http://www.somedomain.com/script.cgi)</p>");
						
			} catch (IOException exception) {
				LogLog.error("Failed to submit results",exception);
				createDialog("Failed to submit results",
						"<h1>Could not submit results</h1><h2>"+ exception +
						"</h2><p>Please ensure you have an internet connection and you have specified a valid PostURL in log4j.properties (ie log4j.appender.ERROR-DIALOG.PostURL=http://www.somedomain.com/script.cgi)</p>");
			}
		}
		/**
		 * Creates a dialog with the a title and the and html content
		 * @param title
		 * @param htmlContent
		 */
		public void createDialog(String title,String htmlContent) {
			TextEditor mainPanel = new TextEditor();
			mainPanel.setPreferredSize(main.getSize());
			mainPanel.setContentType("text/html");
			mainPanel.setText(htmlContent);
			
			optionPane.setMessage(mainPanel);
			
			dialog = optionPane.createDialog(null,title);
			
			dialog.show();
		}
		
		public void show() {			
			dialog.show();			
			if(optionPane.getValue() != null 
					&& ((Integer)optionPane.getValue()).equals(
							new Integer(JOptionPane.OK_OPTION)) ) {
				LogLog.debug("submitting");
				okButtonPushed();				
			}else {
				LogLog.debug("not sending an email");
			}
		}
		
		////// the states ///////
		
		private void hideDetailsState() {
			LogLog.debug("hidDetailsState()");
			
			// remove and add
			main.remove(showDetailsPanel);
			main.add(hideDetailsPanel,BorderLayout.CENTER);
			
			// change the state
			detailsButton.removeActionListener(hideDetailsAListener);
			detailsButton.addActionListener(showDetailsAListener);
			detailsButton.setText("Show Details >>");
			dialog.setSize(hideDetailsSize);
			
			dialog.validate();
			
			UITools.center(null,dialog);			
		}
		
		private void showDetailsState() {
			LogLog.debug("showDetailsState()");
			
			// remove and add
			main.remove(hideDetailsPanel);
			// init showDetailsPanel (must always do this)
			showDetailsPanel.add(hideDetailsPanel,BorderLayout.NORTH);
			showDetailsPanel.add(detailsScrollPane,BorderLayout.CENTER);
			main.add(showDetailsPanel,BorderLayout.CENTER);
			
			// change the button state
			detailsButton.removeActionListener(showDetailsAListener);
			detailsButton.addActionListener(hideDetailsAListener);
			detailsButton.setText("<< Hide Details");
			
			// change the dialog
			dialog.setSize(showDetailsSize);			
			dialog.validate();
			
			UITools.center(null,dialog);
		}		
	}
	
	public void setPostURL(String url) {
		this.postURL = url;
	}
	
	public String getPostURL() {
		return postURL;
	}
	
	public void showLog4JDialog(LoggingEvent logEvent) {
		new Log4JDialog(logEvent,300,200).show();
	}
	public void setMessageConversionPattern(String value) {
		this.messageConversionPattern = value;
	}
	public String getMessageConversionPattern() {
		return messageConversionPattern;
	}	
	public Layout getMessageLayout() {
		messageLayout.setConversionPattern(getMessageConversionPattern());
		return messageLayout;
	}
	public void setMessageLayout(PatternLayout layout) {		
		messageLayout = layout;
	}
	public void setExcludeLoggerNames(String value) {
		this.excludeLoggerNames = value;
	}
	public String getExcludeLoggerNames() {
		return this.excludeLoggerNames;
	}
	public void setLevel(Level newLevel) {
		this.level = newLevel;
	}
	
	public Level getError() {
		return level;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	protected void append(LoggingEvent logEvent) {
		if( (level == null || (logEvent != null && logEvent.getLevel().isGreaterOrEqual(level)) )
			&& (!isExcludedLoggerName(logEvent))  ) {	
		  LogLog.debug("showing event: "+logEvent);
		  showLog4JDialog(logEvent);			
		}else {
		  LogLog.debug("skipping event: "+logEvent);
		}
	}
	/**
	 * Determine if we are excluding an event based upon a logger name
	 * @param logEvent
	 * @return
	 */
	public boolean isExcludedLoggerName(LoggingEvent logEvent) {
		String loggerName = logEvent.getLoggerName();
		String excludedString = this.getExcludeLoggerNames();
		LogLog.debug("loggerName: "+loggerName+" excludedString: "+excludedString);
		if(excludedString == null) {
			return false;
		}else {
			String[] excluded = excludedString.split(" ");
			for(int i=0;excluded!=null && i<excluded.length;i++) {
				String e = excluded[i];				
				if(loggerName != null && loggerName.indexOf(e) != -1) {
					LogLog.debug("exclude: "+e);
					return true;
				}else {
					LogLog.debug("ok by: "+e+" "+loggerName.indexOf(e));
				}
			}
		}		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.Appender#close()
	 */
	public void close() {
		// don't really need to do anything
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.Appender#requiresLayout()
	 */
	public boolean requiresLayout() {
		return true;
	}

}
