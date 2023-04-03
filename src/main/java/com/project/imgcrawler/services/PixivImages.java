package com.project.imgcrawler.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@AllArgsConstructor
@Getter
public class PixivImages extends RepresentationModel<PixivImages> {
    List<PixivImage> imageList;
}
