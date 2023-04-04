package com.project.imgcrawler.controller;

import com.project.imgcrawler.services.PixivImage;
import com.project.imgcrawler.services.PixivImages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping("/database")
@ResponseBody
@CrossOrigin
public class DatabaseController {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostMapping("/add")
    public CollectionModel<PixivImage> addRecord(@RequestBody PixivImage image) {
        if (image == null || image.getPid().equals("")) {
            return CollectionModel.of(new ArrayList<>());
        }
        String fetchSqlString = "SELECT pid, image_format, image_base64_string, download_time FROM my_favorite_artworks WHERE pid = ?";

        try {
            jdbcTemplate.queryForObject(fetchSqlString, rowMapper(), Integer.parseInt(image.getPid()));
        } catch (EmptyResultDataAccessException e) {
            String addSqlString = "INSERT INTO my_favorite_artworks (pid, image_format, image_base64_string, download_time) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(addSqlString, Integer.parseInt(image.getPid()), image.getImgFormat(), image.getImageBase64(), image.getDownloadTime());
            return fetchAll();
        }

        return null;
    }

    @GetMapping("/image/{pid}")
    public RepresentationModel<PixivImage> fetchImage(@PathVariable String pid) {
        int pidNum;
        try {
            pidNum = Integer.parseInt(pid);
        } catch (NumberFormatException nfe) {
            return null;
        }
        String fetchSqlString = "SELECT pid, image_format, image_base64_string, download_time FROM my_favorite_artworks WHERE pid = ?";
        PixivImage fetchResult = jdbcTemplate.queryForObject(fetchSqlString, rowMapper(), pidNum);
        if (fetchResult != null) {
            fetchResult.add(linkTo(methodOn(DatabaseController.class).fetchImage(pid)).withSelfRel());
        }
        return fetchResult;
    }

    @GetMapping("/images")
    public CollectionModel<PixivImage> fetchAll() {
        String fetchSqlString = "SELECT pid, image_format, image_base64_string, download_time FROM my_favorite_artworks";
        PixivImages pixivImages = new PixivImages(jdbcTemplate.query(fetchSqlString, rowMapper()));
        for (PixivImage image : pixivImages.getImageList()) {
            image.add(linkTo(methodOn(DatabaseController.class).fetchImage(image.getPid())).withSelfRel());
            image.add(linkTo(methodOn(DatabaseController.class).fetchAll()).withRel("super"));
        }
        return CollectionModel.of(pixivImages.getImageList(), List.of(linkTo(methodOn(DatabaseController.class).fetchAll()).withSelfRel()));
    }

    @DeleteMapping("/image/{pid}")
    public void deleteImage(@PathVariable String pid) {
        int pidNum;
        try {
            pidNum = Integer.parseInt(pid);
        } catch (NumberFormatException nfe) {
            return;
        }
        String deleteSqlString = "DELETE FROM my_favorite_artworks WHERE pid = ?";
        jdbcTemplate.update(deleteSqlString, pidNum);

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
                        rs.getString("imgUrl")
                ));
    }
}
