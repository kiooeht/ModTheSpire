package com.evacipated.cardcrawl.modthespire;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.Component;
import java.awt.event.*;

// From https://stackoverflow.com/a/24777687
public class JCheckBoxList extends JList<JCheckBox> {
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    public JCheckBoxList() {
        setCellRenderer(new CellRenderer());
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                    checkbox.setSelected(!checkbox.isSelected());
                    repaint();
                }
            }
        });
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public JCheckBoxList(ListModel<JCheckBox> model){
        this();
        setModel(model);
    }

    public int[] getCheckedIndices() {
        int size = 0;
        for (int i=0; i < getModel().getSize(); ++i) {
            if (getModel().getElementAt(i).isSelected()) {
                ++size;
            }
        }
        int[] ret = new int[size];
        int j = 0;
        for (int i=0; i < getModel().getSize(); ++i) {
            if (getModel().getElementAt(i).isSelected()) {
                ret[j] = i;
                ++j;
            }
        }
        return ret;
    }

    protected class CellRenderer implements ListCellRenderer<JCheckBox> {
        public Component getListCellRendererComponent(
                JList<? extends JCheckBox> list, JCheckBox value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JCheckBox checkbox = value;

            //Drawing checkbox, change the appearance here
            checkbox.setBackground(isSelected ? getSelectionBackground()
                    : getBackground());
            checkbox.setForeground(isSelected ? getSelectionForeground()
                    : getForeground());
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ? UIManager
                    .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            return checkbox;
        }
    }
}
