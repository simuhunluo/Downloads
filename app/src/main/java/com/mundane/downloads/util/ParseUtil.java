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
    
    private static Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.41");
        headers.put("origin", "https://www.douyin.com");
        headers.put("Cookie", "ttwid=1%7C1nkM2JZy9KS2u9Z_Q6WCaqUZnq1VTBcwwJpzNqvIxT0%7C1665055333%7C5044f9ac08822040d011689f15314359e103c12e3773fbe79ec9b9a0acce289d; passport_csrf_token=8459cb7d2458d506479afb53506ba377; passport_csrf_token_default=8459cb7d2458d506479afb53506ba377; s_v_web_id=verify_lhm0amfj_XQdEKWuQ_NHls_4R08_Bw4u_mdMw7CUB3CcV; VIDEO_FILTER_MEMO_SELECT=%7B%22expireTime%22%3A1684597030957%2C%22type%22%3A1%7D; pwa2=%222%7C0%22; download_guide=%223%2F20230514%22; strategyABtestKey=%221684497390.312%22; passport_assist_user=CjzLjQKKaQ64xBoD9NIz5I9UlzW1EtVK0_qghvDN_EDQ5p2ZAdx_8FyDVmW4frbCwn3saH_BJCs0mLrFc3UaSAo89mJ9ysJ1iOJj5mLT7r0Tst1GFA6wSQbqGLFNzv4eqnDvX9Gue_lfBfUDnyJWPJPLDX4PpsKsTE5E-VSLEL_FsQ0Yia_WVCIBA01dewE%3D; n_mh=hvnJEQ4Q5eiH74-84kTFUyv4VK8xtSrpRZG1AhCeFNI; sso_uid_tt=661bee349a525a01e43ff77b5255b8c5; sso_uid_tt_ss=661bee349a525a01e43ff77b5255b8c5; toutiao_sso_user=247cc288d167cb287a4a58d9333f8f2e; toutiao_sso_user_ss=247cc288d167cb287a4a58d9333f8f2e; sid_ucp_sso_v1=1.0.0-KDUwMGU1ZThmY2ZmYzU1ZGRiNjYyYzk1MzFiMGFjMWMwYmU5M2JmMWYKHQiWyueX3QEQvcmdowYY7zEgDDDr4vLHBTgGQPQHGgJobCIgMjQ3Y2MyODhkMTY3Y2IyODdhNGE1OGQ5MzMzZjhmMmU; ssid_ucp_sso_v1=1.0.0-KDUwMGU1ZThmY2ZmYzU1ZGRiNjYyYzk1MzFiMGFjMWMwYmU5M2JmMWYKHQiWyueX3QEQvcmdowYY7zEgDDDr4vLHBTgGQPQHGgJobCIgMjQ3Y2MyODhkMTY3Y2IyODdhNGE1OGQ5MzMzZjhmMmU; odin_tt=782141e25b625f9e4e87d2828f4fea03a445c93507d807172207411eee9c27f295ad1e84f9d7defe5a39bba21c25c32a; passport_auth_status=6b8c7497a459a6c30e44fc52a97d635e%2C; passport_auth_status_ss=6b8c7497a459a6c30e44fc52a97d635e%2C; uid_tt=70e55d4076814fcb09d49ae3698946ba; uid_tt_ss=70e55d4076814fcb09d49ae3698946ba; sid_tt=b5662e1e3c4e6683a7b53e0d71a47234; sessionid=b5662e1e3c4e6683a7b53e0d71a47234; sessionid_ss=b5662e1e3c4e6683a7b53e0d71a47234; LOGIN_STATUS=1; sid_guard=b5662e1e3c4e6683a7b53e0d71a47234%7C1684497602%7C5183998%7CTue%2C+18-Jul-2023+12%3A00%3A00+GMT; sid_ucp_v1=1.0.0-KDg0YTk2OTA5ZWRlYWI5MmYwZDUzODI1YTEzYzdjMzUzZDU1NjhjMjMKGQiWyueX3QEQwsmdowYY7zEgDDgGQPQHSAQaAmxxIiBiNTY2MmUxZTNjNGU2NjgzYTdiNTNlMGQ3MWE0NzIzNA; ssid_ucp_v1=1.0.0-KDg0YTk2OTA5ZWRlYWI5MmYwZDUzODI1YTEzYzdjMzUzZDU1NjhjMjMKGQiWyueX3QEQwsmdowYY7zEgDDgGQPQHSAQaAmxxIiBiNTY2MmUxZTNjNGU2NjgzYTdiNTNlMGQ3MWE0NzIzNA; store-region=cn-zj; store-region-src=uid; d_ticket=2c48481a00318605436b1ecad4586b5a15f3b; msToken=R7WslQl_ubd4MJALMh45eoJ0Izz8Vdvp03mjY-elU-2Ne8-k6ftcgdnyVGYCLjsmI5V8MGFaW0JUC2BdHrziS6CEuAJ5V0kEyZUDJoLc0IhAnF32TtIE_dg=; FOLLOW_LIVE_POINT_INFO=%22MS4wLjABAAAAZZoSR1AZDLkSp5YyEpVDTcVQHYppVQzFnxa0hcjGHKw%2F1684512000000%2F0%2F0%2F1684498445128%22; FOLLOW_NUMBER_YELLOW_POINT_INFO=%22MS4wLjABAAAAZZoSR1AZDLkSp5YyEpVDTcVQHYppVQzFnxa0hcjGHKw%2F1684512000000%2F0%2F1684497845128%2F0%22; home_can_add_dy_2_desktop=%221%22; publish_badge_show_info=%220%2C0%2C0%2C1684497948398%22; tt_scid=F1R4VMTJd9Llwlq01ZyY6MIETOnQFsDGMdvAJrmhZ.x1YYNMzKHxnloeOqA0Vv9jee82; msToken=zzmnD606Rlh99sX8i-5yt3F-yXUmz30U7LqEvRGXXR6Idxyo7VstTaXKqDL4If8qLaD_i-xO7Vv5bVRD8Fs8nbAB4siMQOtH2C0dIWdlaFSnZUcEgQNdXnk=; __ac_nonce=0646787f80045d7325e3c; __ac_signature=_02B4Z6wo00f01j.q3AwAAIDA10KNnoP9JX4.ztiAAOuri9AfGa2alfvo90RrawN4TSyzFGL78LFil.fggaCENWHuIeKumTlNHPZ5T6uB0bpc6u1XkxL5XcCh6l7d1MpxcCkkyGAEfNXvg6Wa21; __ac_referer=https://www.iesdouyin.com/");
        return headers;
    }
    
    //public static Map<String, String> headers = new HashMap<String, String>() {{
    //    put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.41");
    //    put("origin", "https://www.douyin.com");
    //    put("cookie", "douyin.com; ttwid=1|xGr-W8S6nxYr8EC7MefThD-nhaIC_-vftgO_sAVVkSI|1675694880|6b09f06b17e4d7021e91d397da634fc5ff00dee6e38f4f58c611f24d2867a366; home_can_add_dy_2_desktop=\"0\"; passport_csrf_token=1883f00e07c67356a0883dcf791cbe4e; passport_csrf_token_default=1883f00e07c67356a0883dcf791cbe4e; s_v_web_id=verify_ldsxgv8m_N2R5jJHS_KydI_4X8L_9407_jMMYLkeEwGpj; csrf_session_id=a1ef82e4c5c1bc3865bfff7ccd0a1d1a; douyin.com; bd_ticket_guard_client_data=eyJiZC10aWNrZXQtZ3VhcmQtdmVyc2lvbiI6MiwiYmQtdGlja2V0LWd1YXJkLWNsaWVudC1jc3IiOiItLS0tLUJFR0lOIENFUlRJRklDQVRFIFJFUVVFU1QtLS0tLVxyXG5NSUlCRGpDQnRRSUJBREFuTVFzd0NRWURWUVFHRXdKRFRqRVlNQllHQTFVRUF3d1BZbVJmZEdsamEyVjBYMmQxXHJcbllYSmtNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUVPN2N6bWFlaytTYVFhREpQZU9rOGhyaGxcclxuRDAxM0FPQzRvM2FmZ3VjbW1kS3hjQ0Q3WUJHaUdxSDFtVTg3REQ4L1BSTHRMempXVnFuVklGeVkyaE13MmFBc1xyXG5NQ29HQ1NxR1NJYjNEUUVKRGpFZE1Cc3dHUVlEVlIwUkJCSXdFSUlPZDNkM0xtUnZkWGxwYmk1amIyMHdDZ1lJXHJcbktvWkl6ajBFQXdJRFNBQXdSUUlnVFphcHdQc0pQbVBsQlNValZlbHA0bVd0eVFMc3ZSazJodUIvUTd3UTZXY0NcclxuSVFETVdMOVRNc2V6V1lzSmxsd2x5a0xrOTBMMXdocHFrMWN3MTJrOTVrcHowUT09XHJcbi0tLS0tRU5EIENFUlRJRklDQVRFIFJFUVVFU1QtLS0tLVxyXG4ifQ==; AB_LOGIN_GUIDE_TIMESTAMP=\"1676374434423\"; VIDEO_FILTER_MEMO_SELECT={\"expireTime\":1677056810341,\"type\":1}; strategyABtestKey=\"1676452012.125\"; download_guide=\"3/20230215\"; msToken=tiVgoARXKveVfvKO3__fb5vuRL7coG6BJNz1tESfEsTFPsUpCuj4X__HTpMi-MuPjleExIjDEijPyQibSaCCVcoBiEKtZkmdUu9-l5skTrjxTTVXp3LyMpBP7A3Cvv4=; tt_scid=v2ClHiG.1.Hph1BRKBreQEKpgf9fB8w4eWj6Oe3xZig9gZZa8ioyxrtlOU2WOEov4117; msToken=7ieki6MFlKwZ7-phqPS5DvSpq2UuiHpnEXrFH22bdX0Oim2lZXDHuyy0IJAkVtns0vtzOWTTYyO_VLriLU6Ve_wNNvuGwtEtlXPAH2RxJO_319QKL8ynmTWGZr9bMFU=; __ac_nonce=063ecff1200dfc2e54816; __ac_signature=_02B4Z6wo00f01MpXGjQAAIDASlXgdPAe05zKdx6AAFF-Ch3Tv1EttX9IwAmOzUnVqlvJ28x.S82IRP4dZSi4o9ZS3fgw4aZkNchZhRt27OW8iw2J1sxlewx7d-8st6fkJx1EYtj3lhYQNxVH4c; __ac_referer=https://www.iesdouyin.com/");
    //}};
    
    public static String getUserAgent() {
        return getHeaders().get("user-agent");
    }
    
    public static String getPageInfo(Map<String, String> payload, String awesomeUrl) throws IOException {
        Map<String, String> headers = getHeaders();
        headers.put("referer", awesomeUrl);
        String pageUrl = "https://www.douyin.com/aweme/v1/web/aweme/post/";
        
        String body = Jsoup.connect(pageUrl).data(payload).headers(headers).method(Connection.Method.GET).ignoreContentType(true).execute().body();
        
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
        Map<String, String> headers = getHeaders();
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
    
    
    public static JSONObject getData(String awesomeUrl) {
        Map<String, String> headers = getHeaders();
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
