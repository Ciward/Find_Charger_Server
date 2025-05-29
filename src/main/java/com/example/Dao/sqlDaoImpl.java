package com.example.Dao;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Repository;

import com.example.bean.Charger;
import com.example.bean.INdata;


@Repository 
public class sqlDaoImpl implements sqlDao{
    @Autowired
    private JdbcTemplate jdbcTemplate; //jdbc连接工具类
    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//注意月和小时的格式为两个大写字母
    //查询所有数据
    @Override
    public List<INdata> findallInData() {
        String sql = "select * from inData";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<INdata>(INdata.class));
    }
    public List<String> findallSid(){
        String sql = "select id from outData";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    //查询指定name
    @Override
    public Charger findByname(String name) {
        String sql = "select * from outData where name=?";
        Object[] params = new Object[]{name};
        return jdbcTemplate.queryForObject(
                sql,
                params,
                new BeanPropertyRowMapper<>(Charger.class));
    }
    
    //增
    @Override
    public boolean addCharger(int id,Charger charger) {
        String sql = "insert into outData(id,name,Longitude,Latitude,total,free) values(?,?,?,?,?,?)";
        Object[] params = {charger.getId(),charger.getName(),charger.getLongitude(),charger.getLatitude(),charger.getTotal(),charger.getFree()};
        return jdbcTemplate.update(sql, params)>0;
    }

 
    //删
    @Override
    public boolean deleteByName(String name) {
        String sql = "delete from outData where name=?";
        Object[] params = {name};
        return jdbcTemplate.update(sql,params)>0;

    }

    @Override
    public boolean updateById(String id,Charger charger) {
        String sql = "insert into outData(id,name,total,free) value(?,?,?,?) on DUPLICATE KEY update name=?,total=?,free=?";
        Object[] params = {id,charger.getName(),charger.getTotal(),charger.getFree(),charger.getName(),charger.getTotal(),charger.getFree()};
        return jdbcTemplate.update(sql,params)>0;
    }

    @Override
    public boolean existsByname(String name) {
        String sql = "select exists(select * from outData where name=?)";
        Object[] params = {name};
        return jdbcTemplate.queryForObject(sql, params,new SingleColumnRowMapper<>(boolean.class));
    }

    @Override
    public boolean updateByName(String name, Charger charger) {
        String sql = "update outData set id=?,total=?,free=? where name=?";
        
        Object[] params = {charger.getId(),charger.getTotal(),charger.getFree(),name};
        return jdbcTemplate.update(sql,params)>0;
    }

    public boolean existsById(String id) {
        String sql = "select exists(select * from outData where id=?)";
        Object[] params = {id};
        return jdbcTemplate.queryForObject(sql, params,new SingleColumnRowMapper<>(boolean.class));
    }

    @Override
    public List<Charger> findallChargers() {
        String sql = "select * from outData";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<Charger>(Charger.class));
        
    }

    @Override
    public boolean updateTime(int id) {
        
        String sql = "update timeData set time=Now() where id=?";
        Object[] params = {id};
        return jdbcTemplate.update(sql,params)>0;
    }

    @Override
    public LocalDateTime findTime(int id) {
        String sql= "select time from timeData where id=?";
        Object[] params = {id};
        return jdbcTemplate.queryForObject(sql, params, LocalDateTime.class);
        //return LocalDateTime.parse(timeText, df);
    }

    @Override
    public boolean updatePositionById(String id,double longitude,double latitude) {
        String sql = "insert into outData(id,Longitude,Latitude) value(?,?,?) on DUPLICATE KEY update Longitude=?,Latitude=?";
        Object[] params = {id,longitude,latitude,longitude,latitude};
        return jdbcTemplate.update(sql,params)>0;
    }

    @Override
    public boolean updateIpCount(String ip,LocalDateTime time) {
        String timeText = time.format(df);
        String sql = "insert into ipData(ip,count,lasttime) value(?,1,?) on DUPLICATE KEY update count=count+1,lasttime=?";
        Object[] params = {ip,timeText,timeText};
        return jdbcTemplate.update(sql,params)>0;
    }
    
}
