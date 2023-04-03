package com.project.imgcrawler.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Getter
@AllArgsConstructor
public class SauceNaoResult extends RepresentationModel<SauceNaoResult> {
    private double similarity;
    private String thumbnail;
    private String title;
    private List<String> urls;
    private String author;


}
