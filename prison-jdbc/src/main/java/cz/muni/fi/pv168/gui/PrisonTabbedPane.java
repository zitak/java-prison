
package cz.muni.fi.pv168.gui;

import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author Zita
 */
public class PrisonTabbedPane extends JPanel {
    
    public PrisonTabbedPane() {
        JTabbedPane jTabbedPanePrison = new JTabbedPane();
        JPanel jPanelPrisoners = createPanel("Tab1 contains prisoners");
        jTabbedPanePrison.addTab("Prisoners", null, jPanelPrisoners, "Tab 1");
        jTabbedPanePrison.setSelectedIndex(0);
        JPanel jPanelSentences = createPanel("Tab2 contains sentences");
        jTabbedPanePrison.addTab("Sentences", null, jPanelSentences, "Tab 2");
        setLayout(new GridLayout(1, 1));
        add(jTabbedPanePrison);
    }
       

    protected JPanel createPanel(String text) {
        JPanel jplPanel = new JPanel();
        JLabel jlbDisplay = new JLabel(text);
        jlbDisplay.setHorizontalAlignment(JLabel.CENTER);
        jplPanel.setLayout(new GridLayout(1, 1));
        jplPanel.add(jlbDisplay);
        return jplPanel;
    }
}
