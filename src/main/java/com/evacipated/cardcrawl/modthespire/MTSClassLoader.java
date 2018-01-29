package com.evacipated.cardcrawl.modthespire;

import java.net.URL;
import java.net.URLClassLoader;

// Custom ClassLoader
// When loading STS DesktopLauncher (main entry point), skips searching the parent classloader
// Parent classloader is us and will find our fake DesktopLauncher rather than the real game
// Otherwise acts like URLClassLoader
public class MTSClassLoader extends URLClassLoader {
    public MTSClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.equals("com.megacrit.cardcrawl.desktop.DesktopLauncher")) {
            Class c = findLoadedClass(name);
            if (c == null) {
                c = findClass(name);
                if (c == null) {
                    c = super.loadClass(name);
                }
            }
            return c;
        } else {
            return super.loadClass(name);
        }
    }
}
