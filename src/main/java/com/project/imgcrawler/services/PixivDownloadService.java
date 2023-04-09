package com.project.imgcrawler.services;

import java.io.IOException;

public interface PixivDownloadService {

    PixivImage img_download(String pid) throws IOException;

}
