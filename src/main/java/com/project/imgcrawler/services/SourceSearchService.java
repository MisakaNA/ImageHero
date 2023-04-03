package com.project.imgcrawler.services;

import java.io.File;
import java.io.IOException;

public interface SourceSearchService {

    SauceNaoResults search(File file, String imgFormat, String imgUrl) throws IOException;
}
