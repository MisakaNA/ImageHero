package com.project.imgcrawler.services;

import com.project.imgcrawler.controller.ServiceController;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.hateoas.RepresentationModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@AllArgsConstructor
@Getter
public class SauceNaoResults extends RepresentationModel<SauceNaoResults> {
    private List<SauceNaoResult> searchResults;
    private JSONObject sauceNaoJson;

    public SauceNaoResults() {
        searchResults = new ArrayList<>();
        sauceNaoJson = null;
    }

    public SauceNaoResults(JSONObject sauceNaoJson) throws IOException {
        this.sauceNaoJson = sauceNaoJson;
        searchResults = new ArrayList<>();
        parseJson();
    }

    private void parseJson() {
        JSONArray results = sauceNaoJson.getJSONArray("results");
        int i = 0;
        for (Object each : results) {
            JSONObject result = (JSONObject) each;
            JSONObject resultHeader = result.getJSONObject("header");
            double similarity = Double.parseDouble(resultHeader.getString("similarity"));
            String thumbnail = resultHeader.getString("thumbnail");
            String title = parseTitle(result.getJSONObject("data"));
            List<String> urls = parseUrls(result.getJSONObject("data"));
            String author = parseAuthor(result.getJSONObject("data"));
            SauceNaoResult res = new SauceNaoResult(similarity, thumbnail, title, urls, author);
            res.add(linkTo(methodOn(ServiceController.class).getSearchResult(i++)).withSelfRel());
            searchResults.add(res);
        }
    }

    private String parseTitle(JSONObject object) {
        try {
            if (object.has("title")) {
                return object.getString("title");
            } else if (object.has("eng_name")) {
                return object.getString("eng_name");
            } else if (object.has("material")) {
                return object.getString("material");
            } else if (object.has("source")) {
                return object.getString("source");
            } else if (object.has("created_at")) {
                return object.getString("created_at");
            }
        } catch (JSONException je) {
            return "N/A";
        }

        return "N/A";

    }

    private List<String> parseUrls(JSONObject object) {
        if (object.has("ext_urls")) {
            List<String> urlList = new ArrayList<>();
            JSONArray urlArray = object.getJSONArray("ext_urls");
            if (urlArray != null) {
                for (int i = 0; i < urlArray.length(); i++) {
                    urlList.add(urlArray.getString(i));
                }
            }
            return urlList;
        } else if (object.has("getchu_id")) {
            return List.of("http://www.getchu.com/soft.phtml?id=" + object.getString("getchu_id"));
        }

        return new ArrayList<>();
    }

    private String parseAuthor(JSONObject object) throws JSONException {
        try {
            if (object.has("author")) {
                return object.getString("author");
            } else if (object.has("author_name")) {
                return object.getString("author_name");
            } else if (object.has("member_name")) {
                return object.getString("member_name");
            } else if (object.has("pawoo_user_username")) {
                return object.getString("pawoo_user_username");
            } else if (object.has("twitter_user_handle")) {
                return object.getString("twitter_user_handle");
            } else if (object.has("company")) {
                return object.getString("company");
            } else if (object.has("creator")) {
                try {
                    JSONArray creatorArray = object.getJSONArray("creator");
                    return creatorArray.getString(0);
                } catch (JSONException je) {
                    return object.getString("creator");
                }
            }
        } catch (JSONException je) {
            return "N/A";
        }


        return null;
    }
}
