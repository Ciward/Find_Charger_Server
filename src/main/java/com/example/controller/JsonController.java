package com.example.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.persistence.model.Charger;
import com.example.activity.MainActivity;

@RestController
@RequestMapping(value = "/json", produces = "application/json; charset=UTF-8")
public class JsonController {

    @RequestMapping("/chargers")
    public Map<String,Object> readChargers(){
        //activity.run();
        Map<String, Object> map = new HashMap<>(3);
        List<Charger> lst = new ArrayList<>();
        try{
            FileInputStream file = new FileInputStream(new File("src/data.dat"));
            
            ObjectInputStream reader = new ObjectInputStream(file);
            LocalDateTime localDateTime = (LocalDateTime) reader.readObject();
        while (true) {
            try {
                Charger Cls = (Charger) reader.readObject();
                lst.add(Cls);
            } catch (EOFException e) {
                System.out.println("读取完成");
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        file.close();
        reader.close();
        //LocalDate localDate1 = LocalDate.now();
        //LocalTime localTime1 = LocalTime.now();
        
        map.put("time", localDateTime);
        map.put("data", lst);
        return map;

        }catch(IOException e){
            new MainActivity().run();
        }catch(Exception e1){
            e1.printStackTrace();
        }
        return null;
    }
}
