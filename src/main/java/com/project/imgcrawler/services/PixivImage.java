package com.project.imgcrawler.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PixivImage extends RepresentationModel<PixivImage> {
    private String pid;
    private String imgFormat;
    private String imageBase64;
    private LocalDateTime downloadTime;
}
