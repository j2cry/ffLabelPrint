package ru.bioresourceslab;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame {
    private JPanel mainPanel;
    private JTextField codeField;
    private JComboBox<String> prefixComboBox;
    private JButton printButton;
    private JList<String> labelList;
    private JButton directPrintButton;
    private JTabbedPane tabbedPane;
    private JPanel singleTab;
    private JButton loadButton;
    private JPanel fromFile;
    private JComboBox<String> fromColumnBox;
    private JList<String> excelList;

//    private final JMenuItem reprintMenuItem;

    private static final DefaultListModel<String> listModel = new DefaultListModel<>();
    private File excelFile = null;


    public MainFrame(String title) {
        super(title);
        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
//        Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("printer.png"));
//        setIconImage(image);

        for (int i = 0; i < 6; i++) {
            Component fromSpinner = findComponentByName(singleTab, "fromSpin" + i);
            if (fromSpinner instanceof JSpinner) {
                ((JSpinner) fromSpinner).setValue(1);
            }
        }

        JMenuItem reprintMenuItem = new JMenuItem("re-print");
        reprintMenuItem.addActionListener(e -> sendLabelToPrint(labelList.getSelectedValue()));
        JMenuItem clearListMenuItem = new JMenuItem("clear");
        clearListMenuItem.addActionListener(e -> listModel.clear());

        JPopupMenu listPopup = new JPopupMenu();
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
        listPopup.add(reprintMenuItem);
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
                // печать с первой вкладки
                if (tabbedPane.getSelectedIndex() == 0) {
                    // получаем спиннеры
                    for (int i = 0; i < 6; i++) {
                        Component spinner = findComponentByName(singleTab, "spin" + i);
                        Component fromSpinner = findComponentByName(singleTab, "fromSpin" + i);
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
                // печать со второй вкладки
                } else if (tabbedPane.getSelectedIndex() == 1) {
                    for (int ind = 0; ind < excelList.getModel().getSize(); ind++) {
                        LabelRender label = new LabelRender(excelList.getModel().getElementAt(ind), font);
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
        });

        directPrintButton.addActionListener(e -> {
            sendLabelToPrint(codeField.getText());
            listModel.addElement(codeField.getText());
        });

        tabbedPane.addChangeListener(changeEvent -> directPrintButton.setEnabled(tabbedPane.getSelectedIndex() != 1));

        loadButton.addActionListener(e -> {
            // load column names from excel file
            JFileChooser openDialog = new JFileChooser();
            openDialog.setFileFilter(new FileNameExtensionFilter("Excel files", "xls", "xlsx"));
            openDialog.setAcceptAllFileFilterUsed(false);
            openDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
            File homeDir = new File((System.getProperty("user.home")));
            openDialog.setCurrentDirectory(homeDir);

            if (openDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                excelFile = openDialog.getSelectedFile();
            } else return;

            Workbook workbook;
            try {
                workbook = WorkbookFactory.create(excelFile, null, true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Import error! Unable to open file.");
                return;
            }

            int firstRowIndex = workbook.getSheetAt(0).getFirstRowNum();
            // get headers row
            Row fileRow = workbook.getSheetAt(0).getRow(firstRowIndex);
            // get cols number
            short firstColIndex = fileRow.getFirstCellNum();
            short lastColIndex = fileRow.getLastCellNum();
            int lastRowIndex = workbook.getSheetAt(0).getLastRowNum();
            if (firstRowIndex == lastRowIndex) {
                JOptionPane.showMessageDialog(this, "Import error! File doesn't contain any data.");
                return;
            }

            DefaultComboBoxModel<String> columnsModel = new DefaultComboBoxModel<>();
            for (int index = firstColIndex; index < lastColIndex; index++) {
                // get column names
                String value = fileRow.getCell(index).toString();
                columnsModel.addElement(value);
            }
            fromColumnBox.setModel(columnsModel);

            try {
                workbook.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Import error! Unable to close file.");
                return;
            }
            if (excelFile != null) {
                fromColumnBox.setEnabled(true);
                fromColumnBox.setSelectedIndex(0);
            }
        });

        fromColumnBox.addActionListener(actionEvent -> {
            // load labels from selected column
            Workbook workbook;
            try {
                workbook = WorkbookFactory.create(excelFile, null, true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Import error! Unable to open file.");
                return;
            }

            int firstRowIndex = workbook.getSheetAt(0).getFirstRowNum();
            int lastRowIndex = workbook.getSheetAt(0).getLastRowNum();
            if (firstRowIndex == lastRowIndex) {
                JOptionPane.showMessageDialog(mainPanel, "Import error! File doesn't contain any data.");
                return;
            }

            // read list
            DefaultListModel<String> excelListModel = new DefaultListModel<>();
            for (int index = firstRowIndex + 1; index <= lastRowIndex; index++) {
                Row fileRow = workbook.getSheetAt(0).getRow(index);
                String value = fileRow.getCell(fromColumnBox.getSelectedIndex()).toString();
                if ((value != null) && (!value.equals("")))
                    excelListModel.addElement(value);
            }
            excelList.setModel(excelListModel);

            // close excel file
            try {
                workbook.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Import error! Unable to close file.");
//                    return;
            }
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
        mainPanel.setLayout(new GridLayoutManager(2, 3, new Insets(5, 5, 5, 5), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(120, -1), null, 0, false));
        labelList = new JList();
        scrollPane1.setViewportView(labelList);
        printButton = new JButton();
        printButton.setText("print all");
        mainPanel.add(printButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 24), null, 0, false));
        directPrintButton = new JButton();
        directPrintButton.setText("direct print (x1)");
        mainPanel.add(directPrintButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(123, 24), null, 0, false));
        tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        singleTab = new JPanel();
        singleTab.setLayout(new GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("single", singleTab);
        final JLabel label1 = new JLabel();
        label1.setAutoscrolls(false);
        label1.setHorizontalAlignment(10);
        label1.setHorizontalTextPosition(11);
        label1.setText("Code");
        singleTab.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(22, 16), null, 0, false));
        codeField = new JTextField();
        singleTab.add(codeField, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(96, 24), null, 0, false));
        prefixComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("T");
        defaultComboBoxModel1.addElement("B");
        defaultComboBoxModel1.addElement("S");
        defaultComboBoxModel1.addElement("C");
        defaultComboBoxModel1.addElement("P");
        defaultComboBoxModel1.addElement("A");
        prefixComboBox.setModel(defaultComboBoxModel1);
        singleTab.add(prefixComboBox, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JSpinner spinner1 = new JSpinner();
        spinner1.setName("fromSpin0");
        singleTab.add(spinner1, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JSpinner spinner2 = new JSpinner();
        spinner2.setName("fromSpin1");
        singleTab.add(spinner2, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JSpinner spinner3 = new JSpinner();
        spinner3.setName("fromSpin2");
        singleTab.add(spinner3, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setAlignmentY(0.5f);
        label2.setText("from");
        singleTab.add(label2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(16, 16), null, 0, false));
        final JSpinner spinner4 = new JSpinner();
        spinner4.setName("spin0");
        singleTab.add(spinner4, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Norma");
        singleTab.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Tumor");
        singleTab.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSpinner spinner5 = new JSpinner();
        spinner5.setName("spin1");
        singleTab.add(spinner5, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Mts");
        singleTab.add(label5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSpinner spinner6 = new JSpinner();
        spinner6.setName("spin2");
        singleTab.add(spinner6, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Lymph node");
        singleTab.add(label6, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSpinner spinner7 = new JSpinner();
        spinner7.setName("spin3");
        singleTab.add(spinner7, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JSpinner spinner8 = new JSpinner();
        spinner8.setName("fromSpin3");
        singleTab.add(spinner8, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Lymph node mts");
        singleTab.add(label7, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSpinner spinner9 = new JSpinner();
        spinner9.setName("spin4");
        singleTab.add(spinner9, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JSpinner spinner10 = new JSpinner();
        spinner10.setName("fromSpin4");
        singleTab.add(spinner10, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Recurrent");
        singleTab.add(label8, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSpinner spinner11 = new JSpinner();
        spinner11.setName("spin5");
        singleTab.add(spinner11, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(48, 24), null, 0, false));
        final JSpinner spinner12 = new JSpinner();
        spinner12.setName("fromSpin5");
        singleTab.add(spinner12, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 24), null, 0, false));
        fromFile = new JPanel();
        fromFile.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("from file", fromFile);
        final JScrollPane scrollPane2 = new JScrollPane();
        fromFile.add(scrollPane2, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        excelList = new JList();
        scrollPane2.setViewportView(excelList);
        final Spacer spacer1 = new Spacer();
        fromFile.add(spacer1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        loadButton = new JButton();
        loadButton.setText("Load");
        fromFile.add(loadButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fromColumnBox = new JComboBox();
        fromColumnBox.setEnabled(false);
        fromFile.add(fromColumnBox, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
