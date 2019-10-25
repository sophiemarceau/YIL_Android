package com.softinc.bean;

import java.io.Serializable;

/**
 * 活动实体类
 * Created by zhangbing on 15-4-1.
 */
public class Meeting implements Serializable {
    public String id;
    public String meetingTime;
    public String createTime;
    public String meetingAddress;
    public String watchCount;//被浏览次数
    public String memberCount;//参与人数
    public String commentCount;//评论数量
    public String coinCount;//硬币数量
    public String memberLevel;
    public String payType;
    public String gender;
    public String title;

    public String onTop;
    public String latitude;
    public String longitude;
    public String description;
    public String payYouB;//将要支付的友币

    public String uid;
    public String hxUser;
    public String userPic;
    public String ownerGender;
    public String nickName;
    public String creditPoint;

    public Integer needAccept;
    public Integer memberLimit;

    @Override
    public String toString() {
        return "Meeting{" +
                "id='" + id + '\'' +
                ", meetingTime='" + meetingTime + '\'' +
                ", createTime='" + createTime + '\'' +
                ", meetingAddress='" + meetingAddress + '\'' +
                ", watchCount='" + watchCount + '\'' +
                ", memberCount='" + memberCount + '\'' +
                ", commentCount='" + commentCount + '\'' +
                ", coinCount='" + coinCount + '\'' +
                ", memberLevel='" + memberLevel + '\'' +
                ", payType='" + payType + '\'' +
                ", gender='" + gender + '\'' +
                ", title='" + title + '\'' +
                ", uid='" + uid + '\'' +
                ", onTop='" + onTop + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", description='" + description + '\'' +
                ", payYouB='" + payYouB + '\'' +

                ", hxUser='" + hxUser + '\'' +
                ", userPic='" + userPic + '\'' +
                ", ownerGender='" + ownerGender + '\'' +
                ", nickName='" + nickName + '\'' +
                ", creditPoint='" + creditPoint + '\'' +

                ", needAccept='" + needAccept + '\'' +
                ", memberLimit='" + memberLimit + '\'' +
                '}';
    }
}
