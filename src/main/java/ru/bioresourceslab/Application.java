package ru.bioresourceslab;

import java.awt.*;

public class Application {

    public static void main(String[] args) {
        MainFrame frame = new MainFrame("FF label printer");
        frame.setMinimumSize(new Dimension(frame.getWidth(), frame.getHeight()));
        frame.setVisible(true);
    }

}
