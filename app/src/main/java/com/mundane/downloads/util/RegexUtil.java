package com.mundane.downloads.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegexUtil
 *
 * @author fangyuan
 * @date 2023-05-18
 */
public class RegexUtil {
    private static final Pattern TITLE_PATTERN = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
    
    public static String replaceTitle(String title) {
        Matcher matcher = TITLE_PATTERN.matcher(title);
        // 将匹配到的非法字符以空替换
        title = matcher.replaceAll("");
        return title;
    }
}
