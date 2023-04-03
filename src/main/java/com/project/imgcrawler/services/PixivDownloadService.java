package com.project.imgcrawler.services;

import java.io.IOException;

public interface PixivDownloadService {

    PixivImage img_download(String loginCookie, String pid) throws IOException;

}
