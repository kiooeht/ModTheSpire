/*
 * Copyright (C) 2016, 2018 Randall Wood
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alexandriasoftware.swing;

import com.alexandriasoftware.swing.action.ButtonClickedActionListener;
import com.alexandriasoftware.swing.action.SplitButtonClickedActionListener;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * An implementation of a "split" button. The left side acts like a normal
 * button, right side has a jPopupMenu attached. If there is no attached menu,
 * the right side of the split button appears disabled, and clicking anywhere in
 * the button triggers the normal button action.
 *
 * Implement {@link ButtonClickedActionListener} to handle the event raised when
 * the main button is clicked and {@link SplitButtonClickedActionListener} to
 * handle the event raised when the popup menu is triggered.
 *
 * @author Naveed Quadri 2012
 * @author Randall Wood 2016
 */
public class JSplitButton extends JButton {

    /**
     * Key used for serialization.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Vertical spacing around visual separator.
     */
    private int separatorSpacing = 4;
    /**
     * Width of split button containing menu arrow.
     */
    private int splitWidth = 22;
    /**
     * Size of menu arrow.
     */
    private int arrowSize = 8;
    /**
     * True if mouse is hovering over split; false otherwise.
     */
    private boolean onSplit = false;
    /**
     * Component in split button.
     */
    private Rectangle splitRectangle = new Rectangle();
    /**
     * Menu with split button.
     */
    private JPopupMenu popupMenu;
    /**
     * True if menu should always be displayed when button clicked; false if
     * menu should only be displayed when split clicked.
     */
    private boolean alwaysPopup;
    /**
     * Color of menu arrow.
     */
    private Color arrowColor = Color.BLACK;
    /**
     * Color or menu arrow when disabled.
     */
    private Color disabledArrowColor = Color.GRAY;
    private transient Image image;
    private transient Image disabledImage;
    private final transient Listener listener;

    /**
     * Creates a button with initial text and an icon.
     *
     * @param text the text of the button
     * @param icon the Icon image to display on the button
     */
    public JSplitButton(final String text, final Icon icon) {
        super(text, icon);
        this.listener = new Listener();
        super.addMouseMotionListener(this.listener);
        super.addMouseListener(this.listener);
        super.addActionListener(this.listener);
    }

    /**
     * Creates a button with text.
     *
     * @param text the text of the button
     */
    public JSplitButton(final String text) {
        this(text, null);
    }

    /**
     * Creates a button with an icon.
     *
     * @param icon the Icon image to display on the button
     */
    public JSplitButton(final Icon icon) {
        this(null, icon);
    }

    /**
     * Creates a button with no set text or icon.
     */
    public JSplitButton() {
        this(null, null);
    }

    /**
     * Returns the JPopupMenu if set, null otherwise.
     *
     * @return JPopupMenu
     */
    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    /**
     * Sets the JPopupMenu to be displayed, when the split part of the button is
     * clicked.
     *
     * @param popupMenu the menu to display
     */
    public void setPopupMenu(final JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
        image = null; //to repaint the arrow image
    }

    /**
     * Returns the separatorSpacing. Separator spacing is the space above and
     * below the separator (the line drawn when you hover your mouse over the
     * split part of the button).
     *
     * @return the spacing
     */
    public int getSeparatorSpacing() {
        return separatorSpacing;
    }

    /**
     * Sets the separatorSpacing. Separator spacing is the space above and below
     * the separator (the line drawn when you hover your mouse over the split
     * part of the button).
     *
     * @param separatorSpacing the spacing
     */
    public void setSeparatorSpacing(final int separatorSpacing) {
        this.separatorSpacing = separatorSpacing;
    }

    /**
     * Show the popup menu, if attached, even if the button part is clicked.
     *
     * @return true if alwaysPopup, false otherwise.
     */
    public boolean isAlwaysPopup() {
        return alwaysPopup;
    }

    /**
     * Show the popup menu, if attached, even if the button part is clicked.
     *
     * @param alwaysPopup true to show the attached JPopupMenu even if the
     *                    button part is clicked, false otherwise
     */
    public void setAlwaysPopup(final boolean alwaysPopup) {
        this.alwaysPopup = alwaysPopup;
    }

    /**
     * Gets the color of the arrow.
     *
     * @return the color of the arrow
     */
    public Color getArrowColor() {
        return arrowColor;
    }

    /**
     * Set the arrow color.
     *
     * @param arrowColor the color of the arrow
     */
    public void setArrowColor(final Color arrowColor) {
        this.arrowColor = arrowColor;
        image = null; // to repaint the image with the new color
    }

    /**
     * Gets the disabled arrow color.
     *
     * @return color of the arrow if no popup menu is attached.
     */
    public Color getDisabledArrowColor() {
        return disabledArrowColor;
    }

    /**
     * Sets the disabled arrow color.
     *
     * @param disabledArrowColor color of the arrow if no popup menu is
     *                           attached.
     */
    public void setDisabledArrowColor(final Color disabledArrowColor) {
        this.disabledArrowColor = disabledArrowColor;
        image = null; //to repaint the image with the new color
    }

    /**
     * Splitwidth is the width of the split part of the button.
     *
     * @return the width of the split
     */
    public int getSplitWidth() {
        return splitWidth;
    }

    /**
     * Splitwidth is the width of the split part of the button.
     *
     * @param splitWidth the width of the split
     */
    public void setSplitWidth(final int splitWidth) {
        this.splitWidth = splitWidth;
    }

    /**
     * Gets the size of the arrow.
     *
     * @return size of the arrow
     */
    public int getArrowSize() {
        return arrowSize;
    }

    /**
     * Sets the size of the arrow.
     *
     * @param arrowSize the size of the arrow
     */
    public void setArrowSize(final int arrowSize) {
        this.arrowSize = arrowSize;
        image = null; //to repaint the image with the new size
    }

    /**
     * Gets the image to be drawn in the split part. If no is set, a new image
     * is created with the triangle.
     *
     * @return image
     */
    public Image getImage() {
        if (image != null) {
            return image;
        } else if (popupMenu == null) {
            return this.getDisabledImage();
        } else {
            image = this.getImage(this.arrowSize, this.arrowColor);
            return image;
        }
    }

    /**
     * Sets the image to draw instead of the triangle.
     *
     * @param image the image
     */
    public void setImage(final Image image) {
        this.image = image;
    }

    /**
     * Gets the disabled image to be drawn in the split part. If no is set, a
     * new image is created with the triangle.
     *
     * @return image
     */
    public Image getDisabledImage() {
        if (disabledImage != null) {
            return disabledImage;
        } else {
            disabledImage = this.getImage(this.arrowSize, this.disabledArrowColor);
            return disabledImage;
        }
    }

    /**
     * Draws the default arrow image in the specified color.
     *
     * @param color
     * @return image
     */
    private Image getImage(final int size, final Color color) {
        Graphics2D g;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.setColor(color);
        // this creates a triangle facing right >
        g.fillPolygon(new int[]{0, 0, size / 2}, new int[]{0, size, size / 2}, 3);
        g.dispose();
        // rotate it to face downwards
        img = rotate(img, 90);
        BufferedImage dimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        g = dimg.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(img, null, 0, 0);
        g.dispose();
        for (int i = 0; i < dimg.getHeight(); i++) {
            for (int j = 0; j < dimg.getWidth(); j++) {
                if (dimg.getRGB(j, i) == Color.WHITE.getRGB()) {
                    dimg.setRGB(j, i, 0x8F1C1C);
                }
            }
        }

        return Toolkit.getDefaultToolkit().createImage(dimg.getSource());
    }

    /**
     * Sets the disabled image to draw instead of the triangle.
     *
     * @param image the new image to use
     */
    public void setDisabledImage(final Image image) {
        this.disabledImage = image;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        if (popupMenu != null) {
            size.width = size.width + getSplitWidth();
        }
        return size;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        Color oldColor = g.getColor();
        splitRectangle = new Rectangle(getWidth() - splitWidth, 0, splitWidth, getHeight());
        g.translate(splitRectangle.x, splitRectangle.y);
        int mh = getHeight() / 2;
        int mw = splitWidth / 2;
        if (popupMenu != null) {
            g.drawImage((isEnabled() ? getImage() : getDisabledImage()), mw - arrowSize / 2, mh + 2 - arrowSize / 2, null);
        }
        if (onSplit && !alwaysPopup && popupMenu != null) {
            g.setColor(UIManager.getDefaults().getColor("ComboBox.buttonArrowColor"));
            g.drawLine(1, separatorSpacing + 2, 1, getHeight() - separatorSpacing - 2);
        }
        g.setColor(oldColor);
        g.translate(-splitRectangle.x, -splitRectangle.y);
    }

    /**
     * Rotates the given image with the specified angle.
     *
     * @param img   image to rotate
     * @param angle angle of rotation
     * @return rotated image
     */
    private BufferedImage rotate(final BufferedImage img, final int angle) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(w, h, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.rotate(Math.toRadians(angle), w / 2.0, h / 2.0);
        g.drawImage(img, null, 0, 0);
        return dimg;
    }

    /**
     * Add a {@link ButtonClickedActionListener} to the button. This listener
     * will be notified whenever the button part is clicked.
     *
     * @param l the listener to add.
     */
    public void addButtonClickedActionListener(final ButtonClickedActionListener l) {
        listenerList.add(ButtonClickedActionListener.class, l);
    }

    /**
     * Remove a {@link ButtonClickedActionListener} from the button.
     *
     * @param l the listener to remove.
     */
    public void removeButtonClickedActionListener(final ButtonClickedActionListener l) {
        listenerList.remove(ButtonClickedActionListener.class, l);
    }

    /**
     * Add a {@link SplitButtonClickedActionListener} to the button. This
     * listener will be notified whenever the split part is clicked.
     *
     * @param l the listener to add.
     */
    public void addSplitButtonClickedActionListener(final SplitButtonClickedActionListener l) {
        listenerList.add(SplitButtonClickedActionListener.class, l);
    }

    /**
     * Remove a {@link SplitButtonClickedActionListener} from the button.
     *
     * @param l the listener to remove.
     */
    public void removeSplitButtonClickedActionListener(final SplitButtonClickedActionListener l) {
        listenerList.remove(SplitButtonClickedActionListener.class, l);
    }

    /**
     * @return the listener
     */
    Listener getListener() {
        return listener;
    }

    /**
     * Listener for internal changes within the JSplitButton itself.
     *
     * Package private so its available to tests.
     */
    class Listener implements MouseMotionListener, MouseListener, ActionListener {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (popupMenu == null) {
                fireButtonClicked(e);
            } else if (alwaysPopup) {
                popupMenu.show(JSplitButton.this, getWidth() - (int) popupMenu.getPreferredSize().getWidth(), getHeight());
                fireButtonClicked(e);
            } else if (onSplit) {
                popupMenu.show(JSplitButton.this, getWidth() - (int) popupMenu.getPreferredSize().getWidth(), getHeight());
                fireSplitButtonClicked(e);
            } else {
                fireButtonClicked(e);
            }
        }

        /**
         * Notifies all listeners that have registered interest for notification
         * on this event type. The event instance is lazily created using the
         * {@code event} parameter.
         *
         * @param event the {@code ActionEvent} object
         * @see EventListenerList
         */
        private void fireButtonClicked(final ActionEvent event) {
            // Guaranteed to return a non-null array
            this.fireActionEvent(event, listenerList.getListeners(ButtonClickedActionListener.class));
        }

        /**
         * Notifies all listeners that have registered interest for notification
         * on this event type. The event instance is lazily created using the
         * {@code event} parameter.
         *
         * @param event the {@code ActionEvent} object
         * @see EventListenerList
         */
        private void fireSplitButtonClicked(final ActionEvent event) {
            // Guaranteed to return a non-null array
            this.fireActionEvent(event, listenerList.getListeners(SplitButtonClickedActionListener.class));
        }

        /**
         * Notifies all listeners that have registered interest for notification
         * on this event type. The event instance is lazily created using the
         * {@code event} parameter.
         *
         * @param event                the {@code ActionEvent} object
         * @param singleEventListeners the array of event-specific listeners,
         *                             either
         *                             {@link ButtonClickedActionListener}s or
         *                             {@link SplitButtonClickedActionListener}s
         * @see EventListenerList
         */
        private void fireActionEvent(final ActionEvent event, ActionListener[] singleEventListeners) {
            if (singleEventListeners.length != 0) {
                String actionCommand = event.getActionCommand();
                if (actionCommand == null) {
                    actionCommand = getActionCommand();
                }
                ActionEvent e = new ActionEvent(JSplitButton.this,
                    ActionEvent.ACTION_PERFORMED,
                    actionCommand,
                    event.getWhen(),
                    event.getModifiers());
                // Process the listeners last to first
                for (int i = singleEventListeners.length - 1; i >= 0; i--) {
                    singleEventListeners[i].actionPerformed(e);
                }
            }
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            onSplit = false;
            repaint(splitRectangle);
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            onSplit = splitRectangle.contains(e.getPoint());
            repaint(splitRectangle);
        }

        // <editor-fold defaultstate="collapsed" desc="Unused Listeners">
        @Override
        public void mouseDragged(final MouseEvent e) {
            // required by MouseMotionListener API, but ignored as drag/drop
            // not intrisicly supported within this widget
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            // required by MouseListener API, but handled by actionPerformed()
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            // required by MouseListener API, but handled by actionPerformed()
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            // required by MouseListener API, but handled by actionPerformed()
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
            // required by MouseListener API, but handled by mouseMoved()
        }
        // </editor-fold>
    }
}
