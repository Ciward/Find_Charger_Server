package com.example.activity;


import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


import com.example.persistence.model.Charger;

@Component

public class MainActivity {
    Logger logger = LoggerFactory.getLogger(getClass());
    private int N=0;
    private double Longitude=120.688838,Latitude=36.366236;
    private boolean needSid=true;
    private String sid;
    private String getSidUrl="https://xlr.xlvren.com/jweb_autocharge/wxcommon/userBindOpenid.json?mobile=15206530585&passwd=8c34ba216b43947be78c4c243d3a79ad&openid=oef-duET16JoyBGCNzKrznrtymhQ&unionid=ool2bt3Ql1_TNfyJ3aM5gIpGZKEk";

    public List<Charger> chargers=new ArrayList<>();
    private List<double[]> locations=new ArrayList<>();
    
    public MainActivity(){
        new File("src").mkdir();
        initlist();
        new File("apk/update").mkdir();
    }


    @Scheduled(cron = "0 */7 * * * *")
    @Async
    public void run(){
        sendRequestWithHttpURLConnectionNorth();
        if(needSid)getSid();
        sendRequestWithHttpURLConnectionSouth();
        try {
            writeFile(chargers);
        }catch (Exception e){
            e.printStackTrace();
            new File("src").mkdir();
        }
        logger.info(N+" ");
        N++;
    }
    public void writeFile(List<Charger> lst) throws IOException {
        FileOutputStream file = new FileOutputStream(new File("src/data.dat"));
        ObjectOutputStream writer = new ObjectOutputStream(file);
        LocalDateTime localDateTime = LocalDateTime.now();
        writer.writeObject(localDateTime);
        for (Charger Cls : lst) {
            writer.writeObject(Cls);
        }
        file.close();
        writer.close();
    }

    private void initlist(){
        double baselo=120.685846;
        double basela=36.374208;
        for(int i=0;i<8;i++){
            if(i==4){
                baselo=120.684847;
                basela=36.374924;

            }
            double[] pos=new double[2];

            pos[0]= baselo+(double)i*0.00025;
            pos[1]=basela;
            locations.add(pos);
        }
    }

    private void sendRequestWithHttpURLConnectionNorth() {
        chargers=new ArrayList<>();
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        List<String> urls=new ArrayList<>();
        urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000012097");
        urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000012095");
        urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000012099");
        urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000012096");
        urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000009531");
        urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000009532");
        urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000011046");
        urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000011047");
        try {
            for(int i=0;i<urls.size();i++) {
                URL url = new URL(urls.get(i));
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
                pareNorthJSON(response.toString(),i);
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
    //解析 1
    private void pareNorthJSON(String jsonData,int n){
        try {
            //JSONObject jsonObject=new JSONObject(jsonData);
            JSONArray array1=new JSONArray(jsonData);
            JSONObject jsonObject=array1.getJSONObject(0);
            //String name=new String(jsonObject.getString("mc").getBytes(),"UTF-8");
            String name=jsonObject.getString("mc");
            int total=jsonObject.getInt("gls");
            Charger charger=new Charger();
            charger.name=name;
            charger.latitude=locations.get(n)[1];
            charger.longitude=locations.get(n)[0];
            charger.total=total;
            int free=total;
            for(int i=1;i<=total;i++){
                int s=jsonObject.getInt("glzt"+i);
                if(s==1){
                    free-=1;
                };
            }
            charger.free=free;
            chargers.add(charger);
            
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
                    pareSouthJSON(response.toString());

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
    private void pareSouthJSON(String jsonData){
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
