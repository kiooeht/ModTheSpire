package com.evacipated.cardcrawl.modthespire.ui;

import com.evacipated.cardcrawl.modthespire.ModInfo;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// From https://stackoverflow.com/a/24777687
@SuppressWarnings("serial")
public class JModPanelCheckBoxList extends JList<ModPanel> {
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    private ModSelectWindow parent;

    public JModPanelCheckBoxList(ModSelectWindow parent) {
        this.parent = parent;
        // enable drag and drop
        setDragEnabled(true);
        setDropMode(DropMode.INSERT);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setTransferHandler(new ListItemTransferHandler());

        setCellRenderer(new CellRenderer());

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private StatusIconButton hovered = null;

            private void setHovered(StatusIconButton next)
            {
                if (hovered != next) {
                    if (hovered != null) {
                        hovered.setHovered(false);
                    }
                    if (next != null) {
                        next.setHovered(true);
                    }
                    hovered = next;

                    if (hovered != null && hovered.isHovered()) {
                        JModPanelCheckBoxList.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        JModPanelCheckBoxList.this.setCursor(null);
                    }
                    JModPanelCheckBoxList.this.repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    ModPanel modPanel = getModel().getElementAt(index);
                    parent.setModInfo(modPanel.info);

                    if (e.getX() <= modPanel.checkBox.getWidth()) {
                        if (modPanel.checkBox.isEnabled()) {
                            modPanel.checkBox.setSelected(!modPanel.checkBox.isSelected());
                            repaint();
                            parent.saveCurrentModList();
                        }
                    } else if (hovered != null) {
                        hovered.doClick();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    ModPanel modPanel = getModel().getElementAt(index);
                    StatusIconButton nextHovered = null;
                    for (StatusIconButton icon : modPanel.icons) {
                        int x = icon.getX() + icon.getParent().getX();
                        if (e.getX() >= x && e.getX() <= x + icon.getWidth()) {
                            nextHovered = icon;
                            break;
                        }
                    }
                    setHovered(nextHovered);
                }
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                setHovered(null);
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        // force mods to calc their backgrounds
        publishBoxChecked();
    }
    
    public void publishBoxChecked() {
        for (int i = 0; i < getModel().getSize(); i++) {
            getModel().getElementAt(i).recalcModWarnings(this);
        }
    }

    public JModPanelCheckBoxList(ModSelectWindow parent, DefaultListModel<ModPanel> model) {
        this(parent);
        setModel(model);
    }

    public File[] getAllMods() {
        File[] ret = new File[getModel().getSize()];
        for (int i = 0; i < getModel().getSize(); ++i) {
            ret[i] = getModel().getElementAt(i).modFile;
        }
        return ret;
    }

    public File[] getCheckedMods() {
        int size = 0;
        for (int i = 0; i < getModel().getSize(); ++i) {
            if (getModel().getElementAt(i).isSelected()) {
                ++size;
            }
        }
        File[] ret = new File[size];
        int j = 0;
        for (int i = 0; i < getModel().getSize(); ++i) {
            if (getModel().getElementAt(i).isSelected()) {
                ret[j] = getModel().getElementAt(i).modFile;
                ++j;
            }
        }
        return ret;
    }

    public List<String> getCheckedModIDs() {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < getModel().getSize(); ++i) {
            if (getModel().getElementAt(i).isSelected()) {
                ret.add(getModel().getElementAt(i).info.ID);
            }
        }
        return ret;
    }

    public List<ModInfo> getCheckedModInfos() {
        List<ModInfo> ret = new ArrayList<>();
        for (int i = 0; i < getModel().getSize(); ++i) {
            if (getModel().getElementAt(i).isSelected()) {
                ret.add(getModel().getElementAt(i).info);
            }
        }
        return ret;
    }

    public void toggleAllMods()
    {
        int on = 0;
        int visibleMods = 0;
        for (int i=0; i<getModel().getSize(); ++i) {
            ModPanel modPanel = getModel().getElementAt(i);
            if (!modPanel.isFilteredOut()) {
                ++visibleMods;
                if (modPanel.isSelected()) {
                    ++on;
                }
            }
        }

        if (on > visibleMods / 2) {
            // Toggle off
            for (int i=0; i<getModel().getSize(); ++i) {
                ModPanel modPanel = getModel().getElementAt(i);
                if (!modPanel.isFilteredOut()) {
                    modPanel.setSelected(false);
                }
            }
        } else {
            // Toggle on
            for (int i=0; i<getModel().getSize(); ++i) {
                ModPanel modPanel = getModel().getElementAt(i);
                if (!modPanel.isFilteredOut()) {
                    modPanel.setSelected(true);
                }
            }
        }

        publishBoxChecked();
    }

    public void enableAllMods(boolean enable)
    {
        for (int i=0; i<getModel().getSize(); ++i) {
            ModPanel modPanel = getModel().getElementAt(i);
            if (!modPanel.isFilteredOut()) {
                modPanel.setSelected(enable);
            }
        }

        publishBoxChecked();
    }

    public synchronized void setUpdateIcon(ModInfo info, ModSelectWindow.UpdateIconType type)
    {
        for (int i=0; i<getModel().getSize(); ++i) {
            if (info.equals(getModel().getElementAt(i).info)) {
                getModel().getElementAt(i).setUpdateIcon(type);
                break;
            }
        }
        repaint();
    }

    @Override
    public void updateUI()
    {
        super.updateUI();
        for (int i=0; i<getModel().getSize(); ++i) {
            ModPanel panel = getModel().getElementAt(i);
            panel.updateUI();
            SwingUtilities.updateComponentTreeUI(panel);
            panel.recalcModWarnings(this);
        }
    }

    protected class CellRenderer implements ListCellRenderer<ModPanel> {
        private final JPanel hiddenItem = new JPanel();

        public CellRenderer() {
            hiddenItem.setPreferredSize(new Dimension(0, 0));
        }

        public Component getListCellRendererComponent(JList<? extends ModPanel> list, ModPanel value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JCheckBox checkbox = value.checkBox;

            // Drawing checkbox, change the appearance here
            value.setBackground(isSelected ? getSelectionBackground() : getBackground());
            value.setForeground(isSelected ? getSelectionForeground() : getForeground());

            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(false);

            if (value.isFilteredOut()) {
                return hiddenItem;
            } else {
                return value;
            }
        }
    }
}

// from https://stackoverflow.com/questions/16586562/reordering-jlist-with-drag-and-drop/16591678

// @camickr already suggested above.
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
@SuppressWarnings("serial")
class ListItemTransferHandler extends TransferHandler {
    protected final DataFlavor localObjectFlavor;
    protected int[] indices;
    protected int addIndex = -1; // Location where items were added
    protected int addCount; // Number of items added.

    public ListItemTransferHandler() {
        super();
        // localObjectFlavor = new ActivationDataFlavor(
        // Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of
        // items");
        localObjectFlavor = new DataFlavor(Object[].class, "Array of items");
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JList<?> source = (JList<?>) c;
        c.getRootPane().getGlassPane().setVisible(true);

        indices = source.getSelectedIndices();
        Object[] transferedObjects = source.getSelectedValuesList().toArray(new Object[0]);
        // return new DataHandler(transferedObjects,
        // localObjectFlavor.getMimeType());
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { localObjectFlavor };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return Objects.equals(localObjectFlavor, flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (isDataFlavorSupported(flavor)) {
                    return transferedObjects;
                } else {
                    throw new UnsupportedFlavorException(flavor);
                }
            }
        };
    }

    @Override
    public boolean canImport(TransferSupport info) {
        return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
    }

    @Override
    public int getSourceActions(JComponent c) {
        Component glassPane = c.getRootPane().getGlassPane();
        glassPane.setCursor(DragSource.DefaultMoveDrop);
        return MOVE; // COPY_OR_MOVE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(TransferSupport info) {
        TransferHandler.DropLocation tdl = info.getDropLocation();
        if (!canImport(info) || !(tdl instanceof JList.DropLocation)) {
            return false;
        }

        JList.DropLocation dl = (JList.DropLocation) tdl;
        @SuppressWarnings("rawtypes")
        JList target = (JList) info.getComponent();
        @SuppressWarnings("rawtypes")
        DefaultListModel listModel = (DefaultListModel) target.getModel();
        int max = listModel.getSize();
        int index = dl.getIndex();
        index = index < 0 ? max : index; // If it is out of range, it is
                                            // appended to the end
        index = Math.min(index, max);

        addIndex = index;

        try {
            Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
            for (int i = 0; i < values.length; i++) {
                int idx = index++;
                ((ModPanel)values[i]).checkBox.addItemListener((event) -> {
                    ((JModPanelCheckBoxList)target).publishBoxChecked();
                });
                listModel.add(idx, values[i]);
                ((ModPanel) values[i]).recalcModWarnings((JModPanelCheckBoxList) target);
                target.addSelectionInterval(idx, idx);
            }
            addCount = values.length;
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        c.getRootPane().getGlassPane().setVisible(false);
        cleanup(c, action == MOVE);
    }

    private void cleanup(JComponent c, boolean remove) {
        if (remove && Objects.nonNull(indices)) {
            if (addCount > 0) {
                // https://github.com/aterai/java-swing-tips/blob/master/DragSelectDropReordering/src/java/example/MainPanel.java
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] >= addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            @SuppressWarnings("rawtypes")
            JList source = (JList) c;
            @SuppressWarnings("rawtypes")
            DefaultListModel model = (DefaultListModel) source.getModel();
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }

        indices = null;
        addCount = 0;
        addIndex = -1;
    }
}
