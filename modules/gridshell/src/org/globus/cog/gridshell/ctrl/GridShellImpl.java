/*
 * 
 */
package org.globus.cog.gridshell.ctrl;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.commands.AbstractCommand;
import org.globus.cog.gridshell.commands.gsh.Gsh;
import org.globus.cog.gridshell.gui.GridShellGUIImpl;
import org.globus.cog.gridshell.gui.MenuItemGroup;
import org.globus.cog.gridshell.interfaces.GridShellSwingGUI;
import org.globus.cog.gridshell.interfaces.Program;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.ScopeImpl;
import org.globus.cog.gui.about.CoGAbout;
import org.globus.cog.util.ImageLoader;
import org.globus.cog.util.plugin.Plugin;
import org.globus.cog.util.plugin.Support;
import org.globus.cog.util.plugin.ToolBar;

/**
 * 
 */
public class GridShellImpl extends JPanel implements Plugin {
	private static final Logger logger = Logger.getLogger(GridShellImpl.class);
	
	public static final String TITLE = "GridShell";
	public static final Action DEFAULT_CLOSE = new AbstractAction() {
	    public void actionPerformed(ActionEvent aEvent) {
	        System.exit(0);
	    }
	};
	
	private Collection menuItemGroups = new LinkedList();
	
	private Scope scope = new ScopeImpl();
	private Action close = DEFAULT_CLOSE;
	private Program shell;
	private GridShellSwingGUI gui;
	
	public GridShellImpl() {
		gui = new GridShellGUIImpl();
		this.setLayout(new GridLayout());
		try {
		    shell = new Gsh();
		    // we don't want to pass just getCloseAction() to it because this way if we change
		    // GridShellImpl's we guarantee that shell is the same
		    Action shellCloseAction = new AbstractAction() {
		        public void actionPerformed(ActionEvent aEvent) {
		             getCloseAction().actionPerformed(aEvent);
		        }
		    };
			shell.init(AbstractCommand.arrayToMap(new Object[] {"gridShellSwingGUI",gui,"closeAction",shellCloseAction}));
			shell.execute();
		} catch (Exception e) {
			logger.warn("Problems initializing GridShell",e);
		}
	    this.add(gui.getJComponent());
	}
	public void destroy() {
	    if(shell != null) {
	        try {
                shell.destroy();
            } catch (Exception e) {
                logger.error("error closing shell",e);
            }
	    }
	}
	public String getTitle() {
	    return TITLE;
	}
	public Action getCloseAction() {
	    return close;
	}
	public void setCloseAction(Action value) {
	    if(value==null) {
	        logger.warn("returning to prevent setting close to a null value");
	        return;
	    }
	    close = value;
	}
	
	public Action getAboutAction() {
        final Frame frame = getFrame(this);
        return new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                CoGAbout about = new CoGAbout(frame, true);
                about.setTextResource("text/gridshell/about.html");
                about.setImageResource("images/logos/about-small.png");
                about.show();
            }
        };
    }
	
	public JMenuBar getMenuBar() {
	    JMenuBar result = new JMenuBar();
	    JMenu file = new JMenu("File");
	    JMenuItem item = new JMenuItem("Exit");
	    item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                destroy();
                getCloseAction().actionPerformed(aEvent);
            } 
	    });
	    file.add(item);
	    result.add(file);
	    
	    JMenu options = new JMenu("Options");
	    JMenu font = new JMenu("Font");
	    JMenu size = new JMenu("Size");
	    JMenu face = new JMenu("Face");
	    JMenu style = new JMenu("Style");
	    

        final GridShellSwingGUI gui = this.gui;        
	    // the sizes
	    JMenu s = size;
	    MenuItemGroup group = new MenuItemGroup();
	    JCheckBoxMenuItem cbItem;
	    for(int i=8;i<=36;i++) {
	        final int fontSize = i;
	        cbItem = new JCheckBoxMenuItem(String.valueOf(fontSize));
	        cbItem.addActionListener(new ActionListener() {
	           public void actionPerformed(ActionEvent aEvent) {
	               Font oldFont = gui.getCommandField().getFont();
	               Font newFont = new Font(oldFont.getName(),oldFont.getStyle(),fontSize);
	               gui.getCommandField().setFont(newFont);
	               gui.getHistoryField().setFont(newFont);
	           }
	        });
	        group.add(cbItem);
	        s.add(cbItem);
	        if((i-8)%10==0 && i!=8) {
	            JMenu more = new JMenu("more");
	            s.add(more);
	            s = more;
	        }
	    }	    
	    font.add(size);
	    
	    // font faces
	    String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	    JMenu f = face;
	    group = new MenuItemGroup();
	    for(int i=0;fonts!=null && i<fonts.length;i++) {
	        final String name = fonts[i];
	        cbItem = new JCheckBoxMenuItem(String.valueOf(name));
	        cbItem.addActionListener(new ActionListener() {
	           public void actionPerformed(ActionEvent aEvent) {
	               Font oldFont = gui.getCommandField().getFont();
	               Font newFont = new Font(name,oldFont.getStyle(),oldFont.getSize());
	               gui.getCommandField().setFont(newFont);
	               gui.getHistoryField().setFont(newFont);	               
	           }
	        });
	        f.add(cbItem);
	        if(i%10==0 && i!=0) {
	            JMenu more = new JMenu("more");
	            f.add(more);
	            f = more;
	        }
	        group.add(cbItem);
	    }	    
	    font.add(face);
	    menuItemGroups.add(group);
	    
	    int[] styles = new int[] {Font.BOLD, Font.ITALIC, Font.PLAIN};
	    String[] labels = new String[]{"Bold","Italic","Plain"};
	    for(int i=0;i<styles.length;i++) {
	        final String label = labels[i];
	        final int styl = styles[i];
	        
	        cbItem = new JCheckBoxMenuItem(label);
	        cbItem.addActionListener(new ActionListener() {
	           public void actionPerformed(ActionEvent aEvent) {
	               Font oldFont = gui.getCommandField().getFont();
	               Font newFont = new Font(oldFont.getName(),styl,oldFont.getSize());
	               gui.getCommandField().setFont(newFont);
	               gui.getHistoryField().setFont(newFont);	               
	           }
	        });
	        style.add(cbItem);
	        group.add(cbItem);
	    }
	    menuItemGroups.add(group);
	    font.add(style);
	    
	    options.add(font);
	    
	    
	    result.add(options);
	    
	    
	    JMenu help = new JMenu("Help");    
	    item = new JMenuItem("About");
	    final Frame frame = getFrame(this);
	    item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                getAboutAction().actionPerformed(aEvent);
            }
	    });
	    help.add(item);
	    result.add(help);
	    
	    return result;
	}
	
	public ImageIcon getImageIconC16x16() {
	    return ImageLoader.loadIcon("images/16x16/co/terminal.png");
	}
	
	public ImageIcon getImageIconC32x32() {
	    return ImageLoader.loadIcon("images/32x32/co/terminal.png");
	}
	public Component getComponent() {
	    return this;
	}
	public JToolBar getToolBar() {
	    ToolBar result = new ToolBar(new String[] {"images/16x16/co/help.png"},new String[] {"About GridShell"});
	    result.getButtonAt(0).addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent aEvent) {
	            // we want to wait till last minute for getting about action
	            // to ensure the panel is in the frame
	            getAboutAction().actionPerformed(aEvent);
	        }
	    });	 
	    return result;
	}
	
	public static Frame getFrame(Component c) {
	    if(c == null) {
	        return null;
	    }else if(c instanceof Frame) {
	        return (Frame)c;
	    }else {
	        return getFrame(c.getParent());
	    }
	}		
	
	public static void createAndShowGUI() throws Exception {
		GridShellImpl gridShellImpl = new GridShellImpl();
		JFrame frame = new JFrame("GridShell");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(Support.injectPlugin(gridShellImpl,frame));
		frame.pack();
		frame.show();		
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater( 
			new Runnable() {
				public void run() {
					try{
						createAndShowGUI();
					}catch(Exception exception) {
						logger.warn("Uncaught exception",exception);
					}
				}
			}
		);
	}
}
