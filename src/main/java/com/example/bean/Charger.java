package com.example.bean;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data //包含了get，set和toString
@AllArgsConstructor //有参构造器 set
@NoArgsConstructor  //无参构造器 get
public class Charger implements Serializable{
    public String id;
    public String name="";
    public double distance=-1;
    public int total = 0;
    public int free = 0 ;
    public double latitude;
    public double longitude;


}
