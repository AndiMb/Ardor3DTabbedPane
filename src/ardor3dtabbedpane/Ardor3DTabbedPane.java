package ardor3dtabbedpane;

import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Andreas Hauffe
 */
public class Ardor3DTabbedPane extends JFrame {

    private static int number = 1;

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private ArdorPanel oldSelected = null;

    public Ardor3DTabbedPane() {
        this.getContentPane().setLayout(new BorderLayout());

        //Add the tabbed pane to this panel.
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        //The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                ArdorPanel selPanel = ((ArdorPanel) sourceTabbedPane.getSelectedComponent());
                if (selPanel != null) {
                    selPanel.panelShowing();
                }
                if (oldSelected != null) {
                    oldSelected.panelHidden();
                }
                oldSelected = selPanel;
            }
        };
        tabbedPane.addChangeListener(changeListener);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Tab");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArdorPanel panel = new ArdorPanel();
                panel.panelOpened();
                tabbedPane.addTab("Tab " + number++, null, panel, "Does nothing at all");
                tabbedPane.setSelectedComponent(panel);
            }
        });
        buttonPanel.add(addButton);
        JButton remove = new JButton("Remove Tab");
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabbedPane.getTabCount() > 0) {
                    ArdorPanel panel = (ArdorPanel) tabbedPane.getSelectedComponent();
                    System.out.println("remove");
                    tabbedPane.remove(panel);
                    System.out.println("removed");
                    panel.panelClosed();
                }
            }
        });
        buttonPanel.add(remove);

        getContentPane().add(buttonPanel, BorderLayout.NORTH);
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new Ardor3DTabbedPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 300));

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI();
            }
        });
    }
}
