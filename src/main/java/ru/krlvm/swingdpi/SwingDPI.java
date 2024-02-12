package ru.krlvm.swingdpi;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * SwingDPI API
 * <p>
 * API of SwingDPI
 * <p>
 * SwingDPI allows you to scale your application for convenient using on HiDPI screens
 * Call SwingDPI.applyScalingAutomatically() on your application start for easy scaling
 * GitHub Page: https://github.com/krlvm/SwingDPI
 *
 * @author krlvm
 */
public class SwingDPI {

    public static final String VERSION = "1.2";

    //is scale factor set
    private static boolean scaleFactorSet = false;
    //the applied scale factor, e.g. 1.25 when the system DPI scaling is 125%
    private static float scaleFactor = 1.0f;
    //default scale factor, 100% scale
    private static final float DEFAULT_SCALE_FACTOR = 1.0f;
    //is DPI scale applied
    private static boolean scaleApplied = false;
    //exclude/whitelist defaults (something one)
    private static Set<String> BLACKLISTED_DEFAULTS;
    private static Set<String> WHITELISTED_DEFAULTS;
    // has the Java 9+ native scaling been disabled
    private static final boolean isJava9 = isJava9();
    private static boolean java9ScalingDisabled = !isJava9;

    /**
     * Automatically determines scale factor and applies scaling for all existing and new windows
     * Java 9+ Native Scaling disables
     */
    public static void applyScalingAutomatically() {
        applyScalingAutomatically(true);
    }

    /**
     * Automatically determines scale factor and applies scaling for all existing and new windows
     * <p>
     * Java 9+ Native Scaling is very buggy and have a poor font rendering
     * If you don't want to disable the Java 9 Scaling, SwingDPI will not work
     * to avoid double scaling
     */
    public static void applyScalingAutomatically(boolean disableJava9NativeScaling) {
        if (isJava9) {
            if (!disableJava9NativeScaling) {
                return;
            } else {
                disableJava9NativeScaling();
            }
        }
        determineScaleFactor();
        if (scaleFactor != DEFAULT_SCALE_FACTOR) {
            setScaleApplied(true);
        }
    }

    /**
     * Determines, sets the system DPI scaling setting and retrieves scale factor
     * Returns 1.0 if Java 9 scaling is preferred
     *
     * @return DPI scale factor
     */
    public static float determineScaleFactor() {
        return isJava9 && !java9ScalingDisabled ? 1.0F : _determineScaleFactor();
    }

    /**
     * Determines, sets the system DPI scaling setting and retrieves scale factor
     *
     * @return DPI scale factor
     */
    public static float _determineScaleFactor() {
        float resolution = Toolkit.getDefaultToolkit().getScreenResolution(); //gets the screen resolution in percent, i.e. system DPI scaling
        if (resolution != 100.0f) { //when the system DPI scaling is not 100%
            setScaleFactor(resolution / 96.0f); //divide the system DPI scaling by default (100%) DPI and get the scale factor
        }
        return scaleFactor;
    }

    /**
     * Applies/disables scale for new and existing frames
     *
     * @param apply - enable or disable scaling
     */
    public static void setScaleApplied(boolean apply) {
        setScaleApplied(apply, true);
    }

    /**
     * Applies/disables scale for new and the existing frames
     *
     * @param apply               - enable or disable scaling
     * @param scaleExistingFrames - enable or disable scaling for existing frames
     */
    public static void setScaleApplied(boolean apply, boolean scaleExistingFrames) {
        if (apply == scaleApplied) {
            return; // scale already applied/disabled
        }
        scaleApplied = apply;
        if (!apply) {
            setScaleFactor(1.0f); // after that, the scaling factor should be determined again
        }

        UIDefaults defaults = UIManager.getLookAndFeelDefaults(); // gets the Swing UI defaults - we will writing in them
        for (Object key : Collections.list(defaults.keys())) { // processing all default UI keys
            if(isWindowsLF() && Arrays.asList
                    (
                        "RadioButtonMenuItem.font",
                        "CheckBoxMenuItem.font",
                        "MenuBar.font",
                        "PopupMenu.font",
                        "MenuItem.font",
                        "Menu.font",
                        "ToolTip.font"
                    ).contains(key.toString())) {
                continue;
            }
            if (BLACKLISTED_DEFAULTS != null) {
                if (BLACKLISTED_DEFAULTS.contains(key.toString())) {
                    continue;
                }
            } else if (WHITELISTED_DEFAULTS != null) {
                if (!WHITELISTED_DEFAULTS.contains(key.toString())) {
                    continue;
                }
            }
            Object original = defaults.get(key);
            Object newValue = scale(key, original);
            if (newValue != null && newValue != original) {
                defaults.put(key, newValue); //updating defaults
            }
        }
        fixJOptionPaneIcons();
        if (scaleExistingFrames) {
            for (Frame frame : Frame.getFrames()) { //gets all created frames
                if (!(frame instanceof JFrame)) {
                    return;
                }
                Dimension dimension = frame.getSize();
                frame.setSize(scale(dimension));
                for (Component component : ((JFrame) frame).getContentPane().getComponents()) {
                    dimension = component.getSize();
                    Dimension newDimension = scale(dimension);
                    if (component instanceof JTextField) {
                        component.setPreferredSize(newDimension);
                    } else {
                        component.setSize(newDimension);
                    }
                }
            }
        }
    }

    /**
     * Retrieves a boolean that determines whether scaling is applied or not.
     *
     * @return is scaling applied
     */
    public static boolean isScaleApplied() {
        return scaleApplied;
    }

    /**
     * Sets the scale factor
     *
     * @param scaleFactor - new scale factor
     */
    public static void setScaleFactor(float scaleFactor) {
        disableJava9NativeScaling(); // avoid double scaling
        if (!scaleFactorSet) {
            scaleFactorSet = true;
        }
        SwingDPI.scaleFactor = scaleFactor;
    }

    /**
     * Retrieves the current scale factor
     *
     * @return scale factor
     */
    public static float getScaleFactor() {
        if (!scaleFactorSet) {
            determineScaleFactor();
        }
        return scaleFactor;
    }

    /**
     * Retrieves a scaled version of the param from Swing UI defaults
     *
     * @param key         - param key
     * @param original    - original value
     * @param scaleFactor - scale factor
     * @return a scaled param version
     */
    private static Object scale(Object key, Object original, float scaleFactor) {
        if (original instanceof Font) {
            if (original instanceof FontUIResource && key.toString().endsWith(".font")) {
                int newSize = (int) (Math.round((float) ((Font) original).getSize()) * scaleFactor);
//                return new FontUIResource(((Font) original).getName(), ((Font) original).getStyle(), newSize);
                // Use deriveFont instead so non-english characters aren't broken
                return new FontUIResource(((Font) original).deriveFont((float) newSize));
            }
            return original;
        }
        if (original instanceof Integer) {
            if (!endsWithOneOf((key instanceof String) ? ((String) key).toLowerCase() : "")) {
                return original;
            }
            return (int) ((Integer) original * scaleFactor);
        }
        return null;
    }

    /**
     * Retrieves a scaled version of the param from Swing UI defaults
     *
     * @param key      - param key
     * @param original - original value
     * @return a scaled param version
     */
    private static Object scale(Object key, Object original) {
        return scale(key, original, scaleFactor);
    }

    /**
     * Scales dimension
     *
     * @param dimension - dimension to scale
     * @return a scaled version of the dimension
     */
    public static Dimension scale(Dimension dimension) {
        if (!scaleFactorSet) {
            return dimension;
        }
        dimension.setSize((int) (dimension.getWidth() * scaleFactor), (int) (dimension.getHeight() * scaleFactor));
        return dimension;
    }

    /**
     * Retrieves a scaled version of a dimension
     *
     * @param dimension - dimension to scale
     * @return a scaled version of the dimension
     */
    public static Dimension getScaledDimension(Dimension dimension) {
        if (!scaleFactorSet) {
            return dimension;
        }
        return new Dimension((int) (dimension.getWidth() * scaleFactor), (int) (dimension.getHeight() * scaleFactor));
    }

    public static Dimension scale(int width, int height) {
        return scale(new Dimension(width, height));
    }

    public static int scale(int i) {
        if (!scaleFactorSet) {
            return i;
        }
        return (int) (i * scaleFactor);
    }

    private static boolean endsWithOneOf(String text) {
        for (String suffix : new String[]{"width", "height", "indent", "size", "gap"}) {
            if (suffix.endsWith(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves scaled version of dimension
     * The scaling algorithm is optimized for frame scaling
     *
     * @param dimension - dimension to scale
     * @return a scaled version of the dimension
     */
    public static Dimension scaleFrame(Dimension dimension) {
        if (!scaleFactorSet) {
            return dimension;
        }
        return scale((int) (dimension.width - (dimension.width * .2)), (int) (dimension.height - (dimension.height * .15)));
    }

    /**
     * If font of specific component did not scaled automatically use this method
     *
     * @param component - component for scale
     */
    public static void scaleFont(Component component) {
        setFontSize(component, component.getFont().getSize());
    }

    public static void setFontSize(Component component, float size) {
        Font font = component.getFont();
        component.setFont(font.deriveFont(size * scaleFactor));
    }

    // https://stackoverflow.com/questions/33926645/joptionpane-icon-gets-cropped-in-windows-10
    private static void fixJOptionPaneIcons() {
        if (!isWindowsLF() || isJava9 || (scaleFactor != 1.25 && scaleFactor != 1.5)) return;
        try {
            String[][] icons = {
                    {"OptionPane.warningIcon", "65581"},
                    {"OptionPane.questionIcon", "65583"},
                    {"OptionPane.errorIcon", "65585"},
                    {"OptionPane.informationIcon", "65587"}
            };

            //obtain a method for creating proper icons
            Method getIconBits = Class.forName("sun.awt.shell.Win32ShellFolder2").getDeclaredMethod("getIconBits", long.class, int.class);
            getIconBits.setAccessible(true);
            int icon32Size = (scaleFactor == 1) ? (32) : ((scaleFactor == 1.25) ? (40) : ((scaleFactor == 1.5) ? (45) : ((int) (32 * scaleFactor))));
            Object[] arguments = {null, icon32Size};
            for (String[] s : icons) {
                if (UIManager.get(s[0]) instanceof ImageIcon) {
                    arguments[0] = Long.valueOf(s[1]);
                    //this method is static, so the first argument can be null
                    int[] iconBits = (int[]) getIconBits.invoke(null, arguments);
                    if (iconBits != null) {
                        //create an image from the obtained array
                        BufferedImage img = new BufferedImage(icon32Size, icon32Size, BufferedImage.TYPE_INT_ARGB);
                        img.setRGB(0, 0, icon32Size, icon32Size, iconBits, 0, icon32Size);
                        ImageIcon newIcon = new ImageIcon(img);
                        //override previous icon with the new one
                        UIManager.put(s[0], newIcon);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Exclude some UI Defaults from scaling (blacklist)
     *
     * @param toExclude - UI Defaults to exclude
     * @throws IllegalStateException - if UI Defaults is in whitelist mode
     */
    public static void excludeDefaults(Collection<String> toExclude) {
        if (WHITELISTED_DEFAULTS != null) {
            throw new IllegalStateException("UI Defaults is in whitelist mode");
        }
        if (BLACKLISTED_DEFAULTS == null) {
            BLACKLISTED_DEFAULTS = new HashSet<String>();
        }
        BLACKLISTED_DEFAULTS.addAll(toExclude);
    }

    /**
     * Exclude some UI Defaults from scaling (blacklist)
     *
     * @param toExclude - UI Defaults to exclude
     * @throws IllegalStateException - if UI Defaults is in whitelist mode
     */
    public static void excludeDefaults(String... toExclude) {
        excludeDefaults(Arrays.asList(toExclude));
    }

    /**
     * Allow to scale only specified UI Defaults (whitelist)
     *
     * @param toWhitelist - UI Defaults to whitelist
     * @throws IllegalStateException - if UI Defaults is in blacklist mode
     */
    public static void whitelistDefaults(Collection<String> toWhitelist) {
        if (BLACKLISTED_DEFAULTS != null) {
            throw new IllegalStateException("UI Defaults is in blacklist mode");
        }
        if (WHITELISTED_DEFAULTS == null) {
            WHITELISTED_DEFAULTS = new HashSet<String>();
        }
        WHITELISTED_DEFAULTS.addAll(toWhitelist);
    }

    /**
     * Allow to scale only specified UI Defaults (whitelist)
     *
     * @param toWhitelist - UI Defaults to whitelist
     * @throws IllegalStateException - if UI Defaults is in blacklist mode
     */
    public static void whitelistDefaults(String... toWhitelist) {
        whitelistDefaults(Arrays.asList(toWhitelist));
    }

    public static void disableJava9NativeScaling() {
        if (isJava9) {
            System.setProperty("sun.java2d.uiScale", "1.0");
            System.setProperty("glass.win.uiScale", "100%");
            System.setProperty("prism.allowhidpi", "false");
            java9ScalingDisabled = true;
        }
    }

    public static boolean isLegacyScalingEnabled() {
        return !java9ScalingDisabled;
    }

    // or greater
    public static boolean isJava9() {
        return !System.getProperty("java.specification.version").startsWith("1.");
    }
    private static boolean isWindowsLF() {
        return UIManager.getLookAndFeel().getClass().getName().equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    }
}
