package com.project.imgcrawler.services;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PixivDownloadServiceImpl implements PixivDownloadService {

    private Map<String, String> setCookies(String cookie) {
        Matcher m = Pattern.compile("user_id=(\\d+)").matcher(cookie);
        if (m.find()) {
            System.out.printf("Debug: Successfully get login session! User id: %s\n", m.group(0).split("=")[1]);
        } else {
            System.out.println("Debug: Error login!");
        }
        Map<String, String> cookies = new HashMap<>();
        for (String each : cookie.split("; ")) {
            String[] cookieStr = each.split("=");
            cookies.put(cookieStr[0], cookieStr[1]);
        }
        return cookies;
    }

    public PixivImage img_download(String loginCookie, String pid) throws IOException {
        String imgUrl = getPictureUrl(loginCookie, pid);
        String imgFormat = imgUrl.substring(imgUrl.lastIndexOf(".") + 1);
        URL url = new URL(imgUrl);
        HttpURLConnection res = (HttpURLConnection) url.openConnection();

        res.setRequestMethod("GET");
        res.setRequestProperty("cookie", loginCookie);
        res.setRequestProperty("referer", "https://www.pixiv.net/artworks/" + pid);
        res.setConnectTimeout(30000);
        res.setReadTimeout(30000);
        res.connect();

        if (res.getResponseCode() == 200) {
            String base64ImageString = Base64.getEncoder().encodeToString(writeImgByteArray(res));
            return new PixivImage(pid, imgFormat, base64ImageString, LocalDateTime.now());
        } else {
            System.out.println("Debug: Error, unable to download the picture!");
        }
        System.out.println();
        return new PixivImage();
    }

    private String getPictureUrl(String loginCookie, String pid) throws IOException {
        String url = String.format("https://www.pixiv.net/ajax/illust/%s?lang=zh", pid);

        String document = Jsoup.connect(url)
                .cookies(setCookies(loginCookie))
                .ignoreContentType(true)
                .execute().body();

        JSONObject json = new JSONObject(document);
        String imgUrl = json.getJSONObject("body").getJSONObject("urls").getString("original");
        System.out.println("Debug: Successfully retrieve image url: " + imgUrl);

        return imgUrl;
    }

    private byte[] writeImgByteArray(HttpURLConnection res) throws IOException {
        InputStream inputStream = res.getInputStream();

        // opens an output stream to save into file
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[4096];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();


        System.out.println("Debug: Download succeed!");
        return outputStream.toByteArray();
    }
}