package database_java;

import javax.swing.*;

class SimpleButton extends JButton {
    public SimpleButton(String title) {
        super(title);
        setContentAreaFilled(false);
        setBorderPainted(false);
    }
}
