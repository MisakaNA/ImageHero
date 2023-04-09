package com.project.imgcrawler.controller;

import com.project.imgcrawler.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Controller
@RequestMapping("/services")
@ResponseBody
@CrossOrigin
public class ServiceController {
    @Autowired
    PixivDownloadService pixivDownloadService;
    @Autowired
    SourceSearchService sourceSearchService;
    SauceNaoResults searchResults;
    PixivImage downloadImage;

    @GetMapping(value = "/download/{pid}", produces = "application/hal+json")
    public RepresentationModel<PixivImage> download(@PathVariable String pid) throws IOException {
        if (!pidNumCheck(pid)) {
            return new PixivImage();
        }

        downloadImage = pixivDownloadService.img_download(pid);
        downloadImage.add(linkTo(methodOn(ServiceController.class).download(pid)).withSelfRel());
        downloadImage.add(linkTo(methodOn(DatabaseController.class).addRecord(new HashMap<>())).withRel("save"));
        downloadImage.add(linkTo(ServiceController.class).withRel("root"));
        return downloadImage;
    }

    @PostMapping(value = "/search", produces = "application/hal+json")
    public CollectionModel<SauceNaoResult> search(@RequestParam(name = "imageFile", required = false) MultipartFile multipartFile, @RequestBody(required = false) String imgUrl) throws IOException {
        File convFile = null;
        String imgFormat = "";
        if (multipartFile != null) {
            String multipartName = Objects.requireNonNull(multipartFile.getOriginalFilename());
            imgFormat = multipartName.substring(multipartName.lastIndexOf('.') + 1);
            convFile = new File("temp." + imgFormat);
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(multipartFile.getBytes());
            fos.close();
        }
        if (imgUrl != null) {
            imgUrl = imgUrl.substring(imgUrl.indexOf('=') + 1);
        }
        searchResults = sourceSearchService.search(convFile, imgFormat, imgUrl);
        if(convFile != null) {
            convFile.delete();
        }
        Link link = linkTo(methodOn(ServiceController.class).search(multipartFile, imgUrl)).withSelfRel();
        Link rootlink = linkTo(ServiceController.class).withRel("root");
        return CollectionModel.of(searchResults.getSearchResults(), List.of(link, rootlink));
    }

    @GetMapping(value = "/search/{index}")
    public SauceNaoResult getSearchResult(@PathVariable int index) {
        SauceNaoResult singleResult = searchResults.getSearchResults().get(index);
        singleResult.add(linkTo(methodOn(ServiceController.class).getSearchResult(index)).withSelfRel());
        return singleResult;
    }


    private boolean pidNumCheck(String pid) {
        try {
            Integer.parseInt(pid);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}