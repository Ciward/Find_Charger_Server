package com.example.Dao;

import java.time.LocalDateTime;
import java.util.List;

import com.example.bean.Charger;
import com.example.bean.INdata;

public interface sqlDao {
    List<INdata> findallInData();
    List<String> findallSid(); 
    List<Charger> findallChargers();
    boolean updateTime(LocalDateTime time,int id);
    LocalDateTime findTime(int id);
    Charger findByname(String name);
    boolean existsById(String id);
    boolean existsByname(String name);
    boolean addCharger(int id,Charger user);
    boolean deleteByName(String name);
    boolean updateById(String id,Charger charger);
    boolean updateByName(String name,Charger charger);
    boolean updatePositionById(String id,double longitude,double latitude);
    boolean updateIpCount(String ip,LocalDateTime time);
}

