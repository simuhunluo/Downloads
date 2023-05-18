package com.mundane.downloads.util;

import cn.hutool.json.JSONObject;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * ParseUtil
 *
 * @author fangyuan
 * @date 2022-08-28
 */
public class ParseUtil {
    
    public static Map<String, String> headers = new HashMap<String, String>() {{
        put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.41");
        put("origin", "https://www.douyin.com");
        put("cookie", "douyin.com; ttwid=1|xGr-W8S6nxYr8EC7MefThD-nhaIC_-vftgO_sAVVkSI|1675694880|6b09f06b17e4d7021e91d397da634fc5ff00dee6e38f4f58c611f24d2867a366; home_can_add_dy_2_desktop=\"0\"; passport_csrf_token=1883f00e07c67356a0883dcf791cbe4e; passport_csrf_token_default=1883f00e07c67356a0883dcf791cbe4e; s_v_web_id=verify_ldsxgv8m_N2R5jJHS_KydI_4X8L_9407_jMMYLkeEwGpj; csrf_session_id=a1ef82e4c5c1bc3865bfff7ccd0a1d1a; douyin.com; bd_ticket_guard_client_data=eyJiZC10aWNrZXQtZ3VhcmQtdmVyc2lvbiI6MiwiYmQtdGlja2V0LWd1YXJkLWNsaWVudC1jc3IiOiItLS0tLUJFR0lOIENFUlRJRklDQVRFIFJFUVVFU1QtLS0tLVxyXG5NSUlCRGpDQnRRSUJBREFuTVFzd0NRWURWUVFHRXdKRFRqRVlNQllHQTFVRUF3d1BZbVJmZEdsamEyVjBYMmQxXHJcbllYSmtNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUVPN2N6bWFlaytTYVFhREpQZU9rOGhyaGxcclxuRDAxM0FPQzRvM2FmZ3VjbW1kS3hjQ0Q3WUJHaUdxSDFtVTg3REQ4L1BSTHRMempXVnFuVklGeVkyaE13MmFBc1xyXG5NQ29HQ1NxR1NJYjNEUUVKRGpFZE1Cc3dHUVlEVlIwUkJCSXdFSUlPZDNkM0xtUnZkWGxwYmk1amIyMHdDZ1lJXHJcbktvWkl6ajBFQXdJRFNBQXdSUUlnVFphcHdQc0pQbVBsQlNValZlbHA0bVd0eVFMc3ZSazJodUIvUTd3UTZXY0NcclxuSVFETVdMOVRNc2V6V1lzSmxsd2x5a0xrOTBMMXdocHFrMWN3MTJrOTVrcHowUT09XHJcbi0tLS0tRU5EIENFUlRJRklDQVRFIFJFUVVFU1QtLS0tLVxyXG4ifQ==; AB_LOGIN_GUIDE_TIMESTAMP=\"1676374434423\"; VIDEO_FILTER_MEMO_SELECT={\"expireTime\":1677056810341,\"type\":1}; strategyABtestKey=\"1676452012.125\"; download_guide=\"3/20230215\"; msToken=tiVgoARXKveVfvKO3__fb5vuRL7coG6BJNz1tESfEsTFPsUpCuj4X__HTpMi-MuPjleExIjDEijPyQibSaCCVcoBiEKtZkmdUu9-l5skTrjxTTVXp3LyMpBP7A3Cvv4=; tt_scid=v2ClHiG.1.Hph1BRKBreQEKpgf9fB8w4eWj6Oe3xZig9gZZa8ioyxrtlOU2WOEov4117; msToken=7ieki6MFlKwZ7-phqPS5DvSpq2UuiHpnEXrFH22bdX0Oim2lZXDHuyy0IJAkVtns0vtzOWTTYyO_VLriLU6Ve_wNNvuGwtEtlXPAH2RxJO_319QKL8ynmTWGZr9bMFU=; __ac_nonce=063ecff1200dfc2e54816; __ac_signature=_02B4Z6wo00f01MpXGjQAAIDASlXgdPAe05zKdx6AAFF-Ch3Tv1EttX9IwAmOzUnVqlvJ28x.S82IRP4dZSi4o9ZS3fgw4aZkNchZhRt27OW8iw2J1sxlewx7d-8st6fkJx1EYtj3lhYQNxVH4c; __ac_referer=https://www.iesdouyin.com/");
    }};
    
    public static String getUserAgent() {
        return headers.get("user-agent");
    }
    
    public static String getPageInfo(Map<String, String> payload, String awesomeUrl) throws IOException {
        headers.put("referer", awesomeUrl);
        String pageUrl = "https://www.douyin.com/aweme/v1/web/aweme/post/";
        
        String body = Jsoup.connect(pageUrl)
                .data(payload)
                .headers(headers)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute()
                .body();
        
        return body;
    }
    
    
    public static String parseUrl(String text) {
        String regex = "https://v.douyin.com[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String url = matcher.group();
            return url;
        }
        return null;
    }
    
    
    public static String getAwesomeUrl(String url) {
        try {
            Document document = Jsoup.connect(url).headers(headers).get();
            String location = document.location();
            document = Jsoup.connect(location).headers(headers).get();
            location = document.location();
            return location;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String getJsonStr(String url) {
        try {
            String body = Jsoup.connect(url).ignoreContentType(true).execute().body();
            return body;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    public static int getContentLengthByAddress(String videoAddress) {
        int contentLength = 0;
        try {
            Connection.Response document = Jsoup.connect(videoAddress).ignoreContentType(true).timeout(30000).execute();
            contentLength = Integer.parseInt(document.header("Content-Length"));
            return contentLength;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentLength;
    }
    
    
    public static JSONObject getData(String awesomeUrl) {
        try {
            if (awesomeUrl.contains("?")) {
                awesomeUrl = awesomeUrl.split("\\?")[0];
            }
            headers.put("referer", "https://www.iesdouyin.com/");
            Document document = Jsoup
                    .connect(awesomeUrl)
                    .headers(headers)
                    .ignoreContentType(true)
                    .get();
            Element element = document.selectFirst("script#RENDER_DATA[type=application/json]");
            String html = element.html();
            String decodedStr = URLDecoder.decode(html, "UTF-8");
            JSONObject json = new JSONObject(decodedStr);
            return json;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static JSONObject getAwesomeInfo(JSONObject data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof JSONObject) {
                JSONObject obj = (JSONObject) value;
                if (obj.containsKey("aweme")) {
                    return obj;
                }
            }
        }
        return null;
    }
    
    public static JSONObject getPostInfo(JSONObject data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof JSONObject) {
                JSONObject obj = (JSONObject) value;
                if (obj.containsKey("post")) {
                    return obj;
                }
            }
        }
        return null;
    }
}
