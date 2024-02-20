package com.example.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.example.activity.MainActivity;
import com.example.bean.Charger;
import com.example.Dao.sqlDaoImpl;
import com.example.Dao.IpUtils;

@RestController
@RequestMapping(value = "/json", produces = "application/json; charset=UTF-8")
public class JsonController {
    @Autowired
    private sqlDaoImpl sqlDaoImpl;
    @Autowired
    MainActivity mainActivity;
    IpUtils ipUtils=new IpUtils();
    @RequestMapping("/chargers")
    public Map<String,Object> readChargers(HttpServletRequest request){
        String ip = IpUtils.getIpAddr(request);
        sqlDaoImpl.updateIpCount(ip,LocalDateTime.now());
        //activity.run();
        Map<String, Object> map = new HashMap<>(3);
        List<Charger> lst = new ArrayList<>();
        try{
            lst= sqlDaoImpl.findallChargers();
            LocalDateTime localDateTime = sqlDaoImpl.findTime(1);
            map.put("time", localDateTime);
            map.put("data", lst);
            return map;
        }catch(Exception e1){
            e1.printStackTrace();
        }
        return null;
    }
    
    @RequestMapping("/run")
    public void runActivity(){
        mainActivity.run();
    }

    @RequestMapping("/updatePosition")
    public String updatePosition(String id,double longitude,double latitude){
        if(sqlDaoImpl.updatePositionById(id, longitude, latitude))
            return "1";
        else
            return "0";
    }
}
