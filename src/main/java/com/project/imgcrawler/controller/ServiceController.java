package com.project.imgcrawler.controller;

import com.project.imgcrawler.services.*;
import okhttp3.MultipartBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @PutMapping(value = "/download/{pid}", produces = "application/hal+json")
    public RepresentationModel<PixivImage> download(@RequestBody String loginCookie, @PathVariable String pid) throws IOException {
        if (!pidNumCheck(pid)) {
            return new PixivImage();
        }

        downloadImage = pixivDownloadService.img_download(loginCookie, pid);
        downloadImage.add(linkTo(methodOn(ServiceController.class).download(loginCookie, pid)).withSelfRel());
        downloadImage.add(linkTo(methodOn(DatabaseController.class).addRecord(downloadImage)).withRel("save"));
        downloadImage.add(linkTo(ServiceController.class).withRel("root"));
        return downloadImage;
    }

    @PostMapping(value = "/search", produces = "application/hal+json")
    public CollectionModel<SauceNaoResult> search(@RequestParam(name = "imageFile", required = false) MultipartFile multipartFile, @RequestBody (required = false) String imgUrl) throws IOException {
        File convFile = null;
        String imgFormat = "";
        if(multipartFile != null) {
            String multipartName = Objects.requireNonNull(multipartFile.getOriginalFilename());
            imgFormat = multipartName.substring(multipartName.lastIndexOf('.') + 1);
            convFile = new File("temp." + imgFormat);
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(multipartFile.getBytes());
            fos.close();
        }
        searchResults = sourceSearchService.search(convFile, imgFormat, imgUrl);
        convFile.delete();
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

    @GetMapping(value = "/download/{pid}")
    public RepresentationModel<PixivImage> download(@PathVariable String pid) {
        return downloadImage;
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

/*
* curl -X PUT -d "first_visit_datetime_pc=2023-01-21+18:46:45; yuid_b=hYgjdYA; p_ab_id=1; p_ab_id_2=6; p_ab_d_id=541778518; _fbp=fb.1.1674294405960.232842880; _gcl_au=
1.1.343932403.1674388992; device_token=0675edebcd45af1c588b7f84f653ea58; privacy_policy_notification=0; a_type=0; b_type=0; login_ever=yes; __utmc=235335808; __utmz=235335808.1675418902.4.2.utmcsr=saucenao.com|utmccn=(referral)|utmcmd=referral|utmc
ct=/; _gid=GA1.2.95023357.1676023506; p_b_type=1; __utma=235335808.93014653.1674294406.1676023502.1676106975.7; __cf_bm=.dZVt4cQIUpd.b60NHHw1DftAcxx7boXGOcsx4vnmwM-1676106992-0-AcFLGRifIbHvHwNRcPXNyi+hxRp5eKSWVOk/vEyi8j/NLuUCEq8Xy2SKdeDTq/3TsET+/tL
ayHcsxU/In6Z/HuKWKCpYdN057jvCGO16nKBoZ3lZRZHIGINuxKbhKDzYve035KzRkFOkVK/JiZeWuDUm2eMyPlZJxu1SUil+mEHZq+CRvQsHoR0tZfX62uTxlpBhGT/6T3NVClL6tNyAfWk=; QSI_S_ZN_5hF4My7Ad6VNNAi=v:0:0; tag_view_ranking=0xsDLqCEW6~_EOd7bsGyl~cb5sHWZoUr~paxmg9qloH~e7lcR-S3
O6~CkDjyRo6Vc~aKhT3n4RHZ~7fCik7KLYi~2kSJBy_FeR~4PyAWCZ_ex~Lt-oEicbBr~T40wdiG5yy~-98s6o2-Rp~EZQqoW9r8g~OqKJfTXHwu~RlJg_oCwwz~LX3_ayvQX4~rkLi5JvRDj~SqVgDNdq49~RTJMXD26Ak~75zhzbk0bS~8Vlr9rDUAd~OI0Fth4n6L~SHmZNqUj92~yFAv48RJQR~teDfbHL-ce~ZJfhw5Ok_J~J5-
yi_9W1W~yms16YMDp0~4QveACRzn3~qkC-JF_MXY~nIjJS15KLN~rOnsP2Q5UN~bX7kls1wXg~KN7uxuR89w~ziiAzr_h04~azESOjmQSV~eVxus64GZU~Bd2L9ZBE8q~v3nOtgG77A~rm7Pj4Jhd_~6293srEnwa~HY55MqmzzQ~jk9IzfjZ6n~821uj_yCkp~pTva8KZAY3~43SvLGYfhU~gmf4CCjFkM~w0YKPAw11M~y5CtiJz4U
3~3fccVtAHTt~D4hLr_YmAD~6-MIBL3gvE~68g3qU2lsG~9ODMAZ0ebV~HZk-7ZdqP6~xha5FQn_XC~Zq3D2K1Onx~rP1Hv59ioH~E166v08Suv~ouiK2OKQ-A~4Q1GaIiU6i~IS9H6coDG0~7Fffe9XHNn~6rYZ-6JKHq~zioivfXX84~sq68XTJrzi~WcTW9TCOx9~UX647z2Emo~hHXjRsPIa6~tIebBucCI-~52-15K-YXl~Vupq
sDSD3k~lz5lBlqdfP~OUF2gvwPef~npWJIbJroU~R9IqWjlnhn~BSlt10mdnm~fPspdOMnDo~Sa_OU-jqfZ; __utmt=1; PHPSESSID=91124358_DwogaZA01ARN3FTTutG3LJyXnfg6m6SG; privacy_policy_agreement=5; _ga_MZ1NL4PHH0=GS1.1.1676107719.2.1.1676107784.0.0.0; _gat_gtag_UA_76252
338_1=1; c_type=34; __utmv=235335808.|2=login ever=yes=1^3=plan=normal=1^6=user_id=91124358=1^9=p_ab_id=1=1^10=p_ab_id_2=6=1^11=lang=en=1; __utmb=235335808.5.9.1676107718348; _im_vid=01GRZWYCPGB35GHM6WBW4XDEER; cto_bundle=J5L6PV81d1dQQzI5WGo4MkdzdG
VtMnFBaTN4cCUyQjdlbWVvSm5LWDJPNjRyQkRZQVB0cHRpNGtpMk5VaXFRSUElMkJmUmtkYkpyV2YzZTVwV21Uc05HRnh0V1k5NW9QdXJuTHJkNmd4UyUyQlRBcmZMejFuSTdOU3lCUUdCcUwwZFlnUThUc0RMYSUyRko5YXJ1dmtXU2VVSW9TQWFiTTltUSUzRCUzRA; _ga=GA1.2.1670362547.1674294406; _gat_UA-18302
49-3=1; _ga_75BBYNYN9J=GS1.1.1676106975.11.1.1676107799.0.0.0" http://localhost:114/services/download/105392048

*
* */
