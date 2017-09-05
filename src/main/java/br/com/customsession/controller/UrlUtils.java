package br.com.customsession.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class UrlUtils {

    private UrlUtils() {}

    public static List<Pattern> loadIgnorePaths(String fullTextIgnore) {
        List<Pattern> ignorePaths = new ArrayList<Pattern>();
        if (fullTextIgnore != null) {
            String[] urls = fullTextIgnore.split(",");
            for (String url : urls) {
                url = StringUtils.remove(url, '\n');
                url = StringUtils.remove(url, '\r');
                url = url.replace(".", "\\.");
                url = url.replace("*", ".*.");
                url = url.trim();
                ignorePaths.add(Pattern.compile("^" + url + "$"));
            }
        }
        return ignorePaths;
    }
}

