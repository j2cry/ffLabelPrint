package ru.bioresourceslab;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class MainFrame extends JFrame {
    private JPanel mainPanel;
    private JTextField codeField;
    private JComboBox prefixComboBox;
    private JButton printButton;
    private JPanel spinnerPanel;
    private JList<String> labelList;
    private JButton directPrintButton;

    private JPopupMenu listPopup;
    private JMenuItem reprintMenuItem;
    private JMenuItem clearListMenuItem;

    private static DefaultListModel<String> listModel = new DefaultListModel<>();

    public MainFrame(String title) {
        super(title);
        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
//        Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("printer.png"));
//        setIconImage(image);

        for (int i = 0; i < 6; i++) {
            Component fromSpinner = findComponentByName(spinnerPanel, "fromSpin" + i);
            if (fromSpinner instanceof JSpinner) {
                ((JSpinner) fromSpinner).setValue(1);
            }
        }

        listPopup = new JPopupMenu();
        listPopup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                reprintMenuItem.setEnabled(labelList.getSelectedIndex() > -1);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
        reprintMenuItem = new JMenuItem("re-print");
        reprintMenuItem.addActionListener(e -> sendLabelToPrint((String) labelList.getSelectedValue()));
        listPopup.add(reprintMenuItem);
        clearListMenuItem = new JMenuItem("clear");
        clearListMenuItem.addActionListener(e -> listModel.clear());
        listPopup.add(clearListMenuItem);

        labelList.setComponentPopupMenu(listPopup);
        labelList.setModel(listModel);

        printButton.addActionListener(e -> {
// тут можно вставить кучку настроек шрифта
            Font font = new Font("Times New Roman", Font.BOLD, 14);

// тут можно вставить кучку настроек листа и печатаемой области
            PrinterJob printJob = PrinterJob.getPrinterJob();
            PageFormat pf = printJob.defaultPage();
            Paper paper = pf.getPaper();

            paper.setImageableArea(0.05 * 72, 0.1 * 72, 2.348 * 72, 0.976 * 72);
            pf.setPaper(paper);

            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

            if (printJob.printDialog(attributes)) {
                // получаем спиннеры
                for (int i = 0; i < 6; i++) {
                    Component spinner = findComponentByName(spinnerPanel, "spin" + i);
                    Component fromSpinner = findComponentByName(spinnerPanel, "fromSpin" + i);
                    // можно потом перенастроить, чтобы когда (fromSpinner == null) печатало с 1-ой
                    if (!(spinner instanceof JSpinner) || !(fromSpinner instanceof JSpinner)) {
                        listModel.addElement("OnFrameControls error!");
                        continue;
                    }
                    if ((int) ((JSpinner) spinner).getValue() > 0) {
                        for (int j = 0; j < (int) ((JSpinner) spinner).getValue(); j++) {
                            LabelRender label = new LabelRender(codeField.getText(), prefixComboBox.getSelectedIndex(), i + 1, (int) ((JSpinner) fromSpinner).getValue() + j, font);
                            printJob.setPrintable(label, pf);
                            try {
                                printJob.print();
                                listModel.addElement(label.getLabel());
                            } catch (PrinterException prnExc) {
                                // обработчик исключения
                            }
                        }
                    }

                }
            }
        });
        directPrintButton.addActionListener(e -> {
            sendLabelToPrint(codeField.getText());
            listModel.addElement(codeField.getText());
        });
    }

    //    @Nullable
    private Component findComponentByName(Container container, String componentName) {
        for (Component component : container.getComponents()) {
            if (componentName.equals(component.getName())) {
                return component;
            }
            if (component instanceof JRootPane) {
                // According to the JavaDoc for JRootPane, JRootPane is
                // "A lightweight container used behind the scenes by JFrame,
                // JDialog, JWindow, JApplet, and JInternalFrame.". The reference
                // to the RootPane is set up by implementing the RootPaneContainer
                // interface by the JFrame, JDialog, JWindow, JApplet and
                // JInternalFrame. See also the JavaDoc for RootPaneContainer.
                // When a JRootPane is found, recurse into it and continue searching.
                JRootPane nestedJRootPane = (JRootPane) component;
                return findComponentByName(nestedJRootPane.getContentPane(), componentName);
            }
            if (component instanceof JPanel) {
                // JPanel found. Recursing into this panel.
                JPanel nestedJPanel = (JPanel) component;
                return findComponentByName(nestedJPanel, componentName);
            }
        }
        return null;
    }

    private void sendLabelToPrint(String labelValue) {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        PageFormat pf = printJob.defaultPage();
        Paper paper = pf.getPaper();
        paper.setImageableArea(0.05 * 72, 0.1 * 72, 2.348 * 72, 0.976 * 72);
        pf.setPaper(paper);

        Font font = new Font("Times New Roman", Font.BOLD, 14);
        LabelRender label = new LabelRender(labelValue, font);
        printJob.setPrintable(label, pf);

        try {
            printJob.print();
        } catch (PrinterException e) {
            listModel.addElement(e.getMessage() + " while printing " + labelValue);
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 4, new Insets(5, 5, 5, 5), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(1, 3, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(120, -1), null, 0, false));
        labelList = new JList();
        scrollPane1.setViewportView(labelList);
        spinnerPanel = new JPanel();
        spinnerPanel.setLayout(new GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(spinnerPanel, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSpinner spinner1 = new JSpinner();
        spinner1.setName("spin1");
        spinnerPanel.add(spinner1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JSpinner spinner2 = new JSpinner();
        spinner2.setName("spin2");
        spinnerPanel.add(spinner2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JSpinner spinner3 = new JSpinner();
        spinner3.setName("spin3");
        spinnerPanel.add(spinner3, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JSpinner spinner4 = new JSpinner();
        spinner4.setName("spin4");
        spinnerPanel.add(spinner4, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JSpinner spinner5 = new JSpinner();
        spinner5.setName("spin5");
        spinnerPanel.add(spinner5, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JSpinner spinner6 = new JSpinner();
        spinner6.setName("spin0");
        spinnerPanel.add(spinner6, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Norma");
        spinnerPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Tumor");
        spinnerPanel.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Mts");
        spinnerPanel.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Lymph node");
        spinnerPanel.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Lymph node mts");
        spinnerPanel.add(label5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Recurrent");
        spinnerPanel.add(label6, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setAlignmentY(0.5f);
        label7.setText("from");
        spinnerPanel.add(label7, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(16, 16), null, 0, false));
        final JSpinner spinner7 = new JSpinner();
        spinner7.setName("fromSpin0");
        spinnerPanel.add(spinner7, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JSpinner spinner8 = new JSpinner();
        spinner8.setName("fromSpin1");
        spinnerPanel.add(spinner8, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JSpinner spinner9 = new JSpinner();
        spinner9.setName("fromSpin2");
        spinnerPanel.add(spinner9, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JSpinner spinner10 = new JSpinner();
        spinner10.setName("fromSpin3");
        spinnerPanel.add(spinner10, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JSpinner spinner11 = new JSpinner();
        spinner11.setName("fromSpin4");
        spinnerPanel.add(spinner11, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JSpinner spinner12 = new JSpinner();
        spinner12.setName("fromSpin5");
        spinnerPanel.add(spinner12, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        prefixComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("T");
        defaultComboBoxModel1.addElement("B");
        defaultComboBoxModel1.addElement("S");
        defaultComboBoxModel1.addElement("C");
        defaultComboBoxModel1.addElement("P");
        defaultComboBoxModel1.addElement("A");
        prefixComboBox.setModel(defaultComboBoxModel1);
        spinnerPanel.add(prefixComboBox, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        codeField = new JTextField();
        spinnerPanel.add(codeField, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(90, 24), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setAutoscrolls(false);
        label8.setHorizontalAlignment(10);
        label8.setHorizontalTextPosition(11);
        label8.setText("Code");
        spinnerPanel.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(22, 16), null, 0, false));
        printButton = new JButton();
        printButton.setText("print all");
        mainPanel.add(printButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 24), null, 0, false));
        directPrintButton = new JButton();
        directPrintButton.setText("direct print (x1)");
        mainPanel.add(directPrintButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(123, 24), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
