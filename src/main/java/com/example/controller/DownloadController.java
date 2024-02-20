package com.example.controller;


import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.dongliu.apk.parser.ApkFile;



@Controller
public class DownloadController {
    private int N=0;
    Logger logger = LoggerFactory.getLogger(getClass());
    String path = "apk/update/app-release.apk";
    String pathForce = "apk/force";
    @RequestMapping("/checkUpdate")
    @ResponseBody
    public String checkUpdate(@RequestParam(value="version",required = false,defaultValue = "114514") Integer version){
        File file = new File(path);
        int versionNew = 1;
        if (file.exists()) {
            ApkFile apk =null;
            try {
                apk = new ApkFile(file);
                String manifest = apk.getManifestXml();
                String matchString = "android:versionCode=\"(.*?)\"";
                Pattern matchPattern = Pattern.compile(matchString);
                Matcher match = matchPattern.matcher(manifest);
                if(match.find()) versionNew = Integer.valueOf(match.group(1));
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (apk != null) {
                    try {
                        apk.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(versionNew > version){
                if(new File(pathForce).exists()) return "2";
                else return "1";
            }else return "0";
        } else return "0";
        
    }
}
