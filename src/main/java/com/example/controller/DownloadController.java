package com.example.controller;


import java.io.File;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class DownloadController {
    private int N=0;
    Logger logger = LoggerFactory.getLogger(getClass());
    String path = "apk/update/app-release.apk";
    String pathForce = "apk/force";
    @RequestMapping("/checkUpdate")
    @ResponseBody
    public String checkUpdate(){
        File file = new File(path);
        if (file.exists()) {
            if(new File(pathForce).exists()) return "2";
            else return "1";
        } else {
            return "0";
        }
    }
}
