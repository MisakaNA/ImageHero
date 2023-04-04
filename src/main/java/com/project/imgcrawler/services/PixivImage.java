package com.project.imgcrawler.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PixivImage extends RepresentationModel<PixivImage> {
    private String pid;
    private String title;
    private String author;
    private String imgFormat;
    private String imageBase64;
    private LocalDateTime downloadTime;
    private String imgUrl;
}
