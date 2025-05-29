package com.example.activity;


import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.bean.Charger;
import com.example.bean.INdata;
import com.example.Dao.sqlDaoImpl;

@Component

public class MainActivity {
    @Autowired
    private sqlDaoImpl sqlDaoImpl;
    Logger logger = LoggerFactory.getLogger(getClass());
    private int N=0;
    private double Longitude=120.688838,Latitude=36.366236;
    private boolean needSid=true;
    private String sid;
    private String getSidUrl="https://xlr.xlvren.com/jweb_autocharge/wxcommon/userBindOpenid.json?mobile=15206530585&passwd=8c34ba216b43947be78c4c243d3a79ad&openid=oef-duET16JoyBGCNzKrznrtymhQ&unionid=ool2bt3Ql1_TNfyJ3aM5gIpGZKEk&code=0";
    public List<Charger> chargers=new ArrayList<>();
    private List<INdata> data=new ArrayList<>();
    private List<String> sids=new ArrayList<>();
    public MainActivity(){
        new File("src").mkdir();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean do1 =true;
                while(do1){
                    try{
                        //data = sqlDaoImpl.findallInData();
                        sids = sqlDaoImpl.findallSid();
                        do1 =false;
                    }catch(NullPointerException e){
                        do1 =true;
                    }
                }
            }
        }).start();
        new File("apk/update").mkdir();
    }

    @Scheduled(cron = "0 */7 * * * *")
    @Async
    public void run(){
        //data = sqlDaoImpl.findallInData();
        sids = sqlDaoImpl.findallSid();
        if(needSid)getSid();
        for(int i =0;i<sids.size();i++){
            sendRequestForCharger(sids.get(i));
            
            try {
                Thread.currentThread();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //writeDataBase_old();
        sqlDaoImpl.updateTime(1);
        logger.info(N+" ");
        N++;
    }

    
    public void writeDataBase(Charger c){
        sqlDaoImpl.updateById(c.getId(), c); 
    }

    public void sendRequestForCharger(String id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url;
                    if(id.length()==11)
                        url = new URL("http://cdz.gpsserver.cn/ChargeCarSys?gtel="+id);
                    else if(id.length()==6)
                        url = new URL("https://xlr.xlvren.com/japp_userquery/findAllByBoxCode.json?sid="+sid+"&display=2&boxCode="+id+"&dealSource=1");
                    else throw new IllegalArgumentException("Invalid charger sid");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE/10.0.2287.0");
                    connection.setRequestProperty("contentType", "UTF-8");
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    //System.out.println("success1");
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if(id.length()==11)
                        pareNorthJSON(response.toString(),id);
                    else if(id.length()==6)
                        //pareSouthJSON(response.toString(),id);
                        logger.info(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
    
    //解析 1
    private void pareNorthJSON(String jsonData,String id){
        try {
            //JSONObject jsonObject=new JSONObject(jsonData);
            JSONArray array1=new JSONArray(jsonData);
            JSONObject jsonObject=array1.getJSONObject(0);
            //String name=new String(jsonObject.getString("mc").getBytes(),"UTF-8");
            Charger charger=new Charger();
            charger.setId(id); 
            charger.name=jsonObject.getString("mc");
            charger.total=jsonObject.getInt("gls");
            int free=charger.total;
            for(int i=1;i<=charger.total;i++){
                int s=jsonObject.getInt("glzt"+i);
                if(s==1){
                    free-=1;
                };
            }
            charger.free=free;
            chargers.add(charger);
            writeDataBase(charger);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private void pareSouthJSON(String jsonString,String id){
        try {
            JSONObject jsonObject=new JSONObject(jsonString);
            if(jsonObject.getInt("errorCode")==-1){
                needSid=true;
                return;
            }
            Charger charger=new Charger();
            JSONObject json_data=jsonObject.getJSONObject("data");
            //List<Charger> list=new ArrayList<>();
            JSONObject json_pile=json_data.getJSONObject("pile");
            charger.setId(json_pile.getString("boxCode"));
            charger.setName(json_pile.getString("address"));
            charger.setTotal(json_pile.getInt("deviceNum"));
            JSONArray piles =json_data.getJSONArray("deviceModule");
            int used =0;
            for(int i=0;i<piles.length();i++){
                used += piles.getJSONObject(i).getInt("usedFlag")==0 ? 0:1;
            }
            charger.setFree(charger.getTotal()-used);
            chargers.add(charger);
            writeDataBase(charger);
        }catch (Exception e){
            e.printStackTrace();
        }
        
    }
    
    private void getSid(){
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try{
            URL url = new URL(getSidUrl);
            //URL url = new URL("https://xlr.xlvren.com/jweb_autocharge/position/listPosition.json?longitude=120.687256&latitude=36.37475&sid=8Gsomxku5QrL&showProprietary=1");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE/10.0.2287.0");
            connection.setRequestProperty("contentType", "UTF-8");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            //System.out.println("success1");
            InputStream in = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            JSONObject jsonObject=new JSONObject(response.toString()).getJSONObject("data");
            sid=jsonObject.getString("sid");
            logger.info("sid got: "+sid);
            needSid=false;
        }catch(Exception e){
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void writeDataBase_old(){
        sqlDaoImpl.updateTime(1);
        int i=1;
        for (Charger Cls : chargers) {
            if(sqlDaoImpl.existsById(Cls.getId())){
                sqlDaoImpl.updateById(Cls.getId(), Cls); 
            }else{
                sqlDaoImpl.addCharger(i, Cls);
            }
            i++;
        }
    }

    private void sendRequestWithHttpURLConnectionNorth() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            for(int i=0;i<data.size();i++) {
                URL url = new URL("http://cdz.gpsserver.cn/ChargeCarSys?gtel="+data.get(i).getId());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE/10.0.2287.0");
                connection.setRequestProperty("contentType", "UTF-8");
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                //System.out.println("success1");
                InputStream in = connection.getInputStream();
                
                reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                //pareNorthJSON(response.toString(),i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void sendRequestWithHttpURLConnectionSouth() {
        
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {

            double longitude=Longitude;
            double latitude=Latitude;
            String urlst="https://xlr.xlvren.com/jweb_autocharge/position/listPosition.json?longitude="+longitude+"&latitude="+latitude+"&sid="+sid+"&showProprietary=1";
            URL url = new URL(urlst);
            //URL url = new URL("https://xlr.xlvren.com/jweb_autocharge/position/listPosition.json?longitude=120.687256&latitude=36.37475&sid=8Gsomxku5QrL&showProprietary=1");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE/10.0.2287.0");
            connection.setRequestProperty("contentType", "UTF-8");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            //System.out.println("success1");
            InputStream in = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            pareSouthJSON_old(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    //解析2
    private void pareSouthJSON_old(String jsonData){
        try {
            JSONObject jsonObject=new JSONObject(jsonData);
            if(jsonObject.getInt("errorCode")==-1){
                needSid=true;
                return;
            }
            JSONArray array=jsonObject.getJSONArray("data");
            //List<Charger> list=new ArrayList<>();
            int len=array.length();
            
            for(int i=0;i<len;i++){
                JSONObject jsoni=new JSONObject(array.get(i).toString());
                Charger charger=new Charger();
                charger.setId(jsoni.getString("boxCode"));
                charger.name=jsoni.getString("stationName");
                charger.distance=(double)jsoni.get("distance");
                charger.total=jsoni.getInt("totalPile");
                charger.free= jsoni.getInt("freePile");
                charger.longitude=jsoni.getDouble("longitude");
                charger.latitude=jsoni.getDouble("latitude");
                chargers.add(charger);

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        
    }

}
