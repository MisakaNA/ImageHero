package com.project.imgcrawler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.imgcrawler.services.PixivImage;
import com.project.imgcrawler.services.PixivImages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping("/database")
@ResponseBody
@CrossOrigin
public class DatabaseController {

    @Autowired
    JdbcTemplate artworksJdbcTemplate;

    @PostMapping("/add")
    public CollectionModel<PixivImage> addRecord(@RequestBody Map<String, String> json) throws JsonProcessingException {
        String uname = json.get("uname");
        PixivImage image = new ObjectMapper().registerModule(new JavaTimeModule()).registerModule(new Jackson2HalModule()).readValue(json.get("image"), PixivImage.class);//json.get("image");
        if (image == null || image.getPid().equals("")) {
            return CollectionModel.of(new ArrayList<>());
        }
        String fetchSqlString = "SELECT pid, title, author, image_format, image_base64_string, download_time, image_url FROM " + uname + "_favorite_artworks WHERE pid = ?";

        try {
            artworksJdbcTemplate.queryForObject(fetchSqlString, rowMapper(), Integer.parseInt(image.getPid()));
        } catch (EmptyResultDataAccessException e) {
            String addSqlString = "INSERT INTO " + uname + "_favorite_artworks (pid, title, author, image_format, image_base64_string, download_time, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
            artworksJdbcTemplate.update(addSqlString, Integer.parseInt(image.getPid()), image.getTitle(), image.getAuthor(), image.getImgFormat(), image.getImageBase64(), image.getDownloadTime(), image.getImgUrl());
            return fetchAll(Map.of("uname", uname));
        }

        return null;
    }

    @GetMapping("/image/{pid}")
    public RepresentationModel<PixivImage> fetchImage(@PathVariable String pid, @RequestBody String uname) {
        int pidNum;
        try {
            pidNum = Integer.parseInt(pid);
        } catch (NumberFormatException nfe) {
            return null;
        }
        String fetchSqlString = "SELECT pid, title, author, image_format, image_base64_string, download_time, image_url FROM " + uname + "_favorite_artworks WHERE pid = ?";
        PixivImage fetchResult = artworksJdbcTemplate.queryForObject(fetchSqlString, rowMapper(), pidNum);
        if (fetchResult != null) {
            fetchResult.add(linkTo(methodOn(DatabaseController.class).fetchImage(pid, uname)).withSelfRel());
        }
        return fetchResult;
    }

    @PostMapping("/images")
    public CollectionModel<PixivImage> fetchAll(@RequestBody Map<String, String> json) {
        String uname = json.get("uname");
        String fetchSqlString = "SELECT pid, title, author, image_format, image_base64_string, download_time, image_url FROM " + uname + "_favorite_artworks";
        PixivImages pixivImages = new PixivImages(artworksJdbcTemplate.query(fetchSqlString, rowMapper()));
        for (PixivImage image : pixivImages.getImageList()) {
            image.add(linkTo(methodOn(DatabaseController.class).fetchImage(image.getPid(), uname)).withSelfRel());
            image.add(linkTo(methodOn(DatabaseController.class).fetchAll(Map.of("uname", uname))).withRel("super"));
        }
        return CollectionModel.of(pixivImages.getImageList(), List.of(linkTo(methodOn(DatabaseController.class).fetchAll(Map.of("uname", uname))).withSelfRel()));
    }

    @DeleteMapping("/image/{pid}")
    public CollectionModel<PixivImage> deleteImage(@PathVariable String pid, @RequestBody String uname) {
        int pidNum;
        try {
            pidNum = Integer.parseInt(pid);
        } catch (NumberFormatException nfe) {
            return CollectionModel.of(new ArrayList<>());
        }
        String deleteSqlString = "DELETE FROM " + uname + "_favorite_artworks WHERE pid = ?";
        artworksJdbcTemplate.update(deleteSqlString, pidNum);
        return fetchAll(Map.of("uname", uname));
    }

    private RowMapper<PixivImage> rowMapper() {
        return ((rs, rowNum) ->
                new PixivImage(
                        rs.getString("pid"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("image_format"),
                        rs.getString("image_base64_string"),
                        rs.getTimestamp("download_time").toLocalDateTime(),
                        rs.getString("image_url")
                ));
    }
}
