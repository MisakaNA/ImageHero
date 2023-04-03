package com.project.imgcrawler.services;

import okhttp3.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class SourceSearchServiceImpl implements SourceSearchService {
    private static final String BASEURL = "http://saucenao.com/search.php?output_type=2&numres=6&minsim=80!&db=999&api_key=" +
            "5cafef8dce38d9235f2a65892cba850edc959509";

    public SauceNaoResults search(File file, String imgFormat, String imgUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request;
        String searchUrl;
        if (file != null && imgUrl == null) {
            MediaType mediaType = MediaType.parse("image/" + imgFormat);
            MultipartBody.Builder builder1 = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder1.addFormDataPart("file", "image." + imgFormat, RequestBody.create(mediaType, file));
            RequestBody body = builder1.build();
            request = new Request.Builder()
                    .url(BASEURL)
                    .post(body)
                    .build();
        } else if (file == null && imgUrl != null) {
            searchUrl = BASEURL + "&url=" + imgUrl;
            request = new Request.Builder()
                    .url(searchUrl)
                    .get()
                    .build();
        } else {
            System.out.println("Debug: Invalid Image!");
            return new SauceNaoResults();
        }
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Unexpected HTTP code " + response.code());
        }

        return new SauceNaoResults(new JSONObject(response.body().string()));
    }
}
