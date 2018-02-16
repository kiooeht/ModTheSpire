package com.evacipated.cardcrawl.modthespire;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

// From https://stackoverflow.com/a/24777687
@SuppressWarnings("serial")
public class JCheckBoxList extends JList<ModPanel> {
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public JCheckBoxList() {
		// enable drag and drop
		setDragEnabled(true);
		setDropMode(DropMode.INSERT);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setTransferHandler(new ListItemTransferHandler());

		setCellRenderer(new CellRenderer());
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				if (index != -1) {
					JCheckBox checkbox = ((ModPanel) getModel().getElementAt(index)).checkBox;
					checkbox.setSelected(!checkbox.isSelected());
					repaint();
				}
			}
		});
	}

	public JCheckBoxList(DefaultListModel<ModPanel> model) {
		this();
		setModel(model);
	}
	
	public File[] getCheckedMods() {
		int size = 0;
		for (int i = 0; i < getModel().getSize(); ++i) {
			if (getModel().getElementAt(i).checkBox.isSelected()) {
				++size;
			}
		}
		File[] ret = new File[size];
		int j = 0;
		for (int i = 0; i < getModel().getSize(); ++i) {
			if (getModel().getElementAt(i).checkBox.isSelected()) {
				ret[j] = getModel().getElementAt(i).modFile;
				++j;
			}
		}
		return ret;
	}

	protected class CellRenderer implements ListCellRenderer<ModPanel> {
		public Component getListCellRendererComponent(JList<? extends ModPanel> list, ModPanel value, int index,
				boolean isSelected, boolean cellHasFocus) {
			JCheckBox checkbox = value.checkBox;

			// Drawing checkbox, change the appearance here
			value.setBackground(isSelected ? getSelectionBackground() : getBackground());
			value.setForeground(isSelected ? getSelectionForeground() : getForeground());
			checkbox.setEnabled(isEnabled());
			checkbox.setFont(getFont());
			checkbox.setFocusPainted(false);
			checkbox.setBorderPainted(true);
			checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			return value;
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
				listModel.add(idx, values[i]);
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