package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.classfilters.CardFilter;
import com.evacipated.cardcrawl.modthespire.classfilters.RelicFilter;
import org.clapper.util.classutil.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomContent
{
    public static List<String> find(ClassFilter typeFilter, URL[] urls) throws URISyntaxException
    {
        ClassFinder finder = new ClassFinder();
        File[] files = new File[urls.length];
        for (int i=0; i<urls.length; ++i) {
            files[i] = new File(urls[i].toURI());
        }
        finder.add(files);

        ClassFilter filter =
            new AndClassFilter(
                new NotClassFilter(new InterfaceOnlyClassFilter()),
                new NotClassFilter(new AbstractClassFilter()),
                typeFilter
            );
        Collection<ClassInfo> foundClasses = new ArrayList<>();
        finder.findClasses(foundClasses, filter);

        List<String> ret = new ArrayList<>();
        for (ClassInfo classInfo : foundClasses) {
            ret.add(classInfo.getClassName());
        }
        return ret;
    }

    public static List<String> findCards(URL[] urls) throws URISyntaxException
    {
        return find(new CardFilter(), urls);
    }

    public static List<String> findRelics(URL[] urls) throws URISyntaxException
    {
        return find(new RelicFilter(), urls);
    }
}
