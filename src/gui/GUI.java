package gui;

import interfaces.GUIPanel;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 
 * Class GUI to hold the JFrame, everything else extends JPanel so that
 * a new JPanel can be repainted on the JFrame
 * 
 * @author Croller
 *
 */
@SuppressWarnings("serial")
public class GUI extends JFrame {

    private GUIPanel panel;
 
    public GUI() throws IOException {
        setTitle("Chat Program");
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(600, 500));
        setResizable(false);
        panel = new IntroPanel();
        setLocation(d.width / 2 - 600 / 2, d.height / 2
                - 500 / 2);
        getContentPane().add((JPanel) panel);
        pack();
        setVisible(true);
    }

    public GUIPanel getPanel() {
        return panel;
    }

    public void setPanel(GUIPanel p) {
        getContentPane().remove((JPanel) panel);
        panel.kill();
        panel = p;
        getContentPane().add((JPanel) panel);
        pack();
        System.gc();
    }
    
    public void update() {
        paintComponents(getGraphics());
        panel.update();
    }

}
