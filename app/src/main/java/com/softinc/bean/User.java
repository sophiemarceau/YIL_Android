package com.softinc.bean;

import com.easemob.chat.EMContact;

public class User extends EMContact {

    public String id;
    public String phoneNumber;
    public String password;
    public String Gender;
    private int unreadMsgCount;
    private String header;
    private String avatar;
    public String Brithday;
    public String CreditPoint;
    public String salaryId;
    public String jobId;
    public String jobName;
    public String areaId;
    public String areaName;
    public String iconPath;
    public String BR_ID;
    public String BuddyStatus;
    public String BuddyRequest;
    public String UserLevel;
    public String hxUser;
    public String signtext;
    public String Distance;
    public String mail;
    public String hobbies;

    public User() {
    }

    public User(String username) {
        this.username = username;
    }

    public int getUnreadMsgCount() {
        return unreadMsgCount;
    }

    public void setUnreadMsgCount(int unreadMsgCount) {
        this.unreadMsgCount = unreadMsgCount;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getBrithday() {
        return Brithday;
    }

    public void setBrithday(String brithday) {
        Brithday = brithday;
    }

    public String getCreditPoint() {
        return CreditPoint;
    }

    public void setCreditPoint(String creditPoint) {
        CreditPoint = creditPoint;
    }

    public String getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(String salaryId) {
        this.salaryId = salaryId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getUserLevel() {
        return UserLevel;
    }

    public void setUserLevel(String userLevel) {
        UserLevel = userLevel;
    }

    public String getHxUser() {
        return hxUser;
    }

    public void setHxUser(String hxUser) {
        this.hxUser = hxUser;
    }

    public String getSigntext() {
        return signtext;
    }

    public void setSigntext(String signtext) {
        this.signtext = signtext;
    }

    public String getBR_ID() {
        return BR_ID;
    }

    public void setBR_ID(String BR_ID) {
        this.BR_ID = BR_ID;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public String getBuddyStatus() {
        return BuddyStatus;
    }

    public void setBuddyStatus(String buddyStatus) {
        BuddyStatus = buddyStatus;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
        Distance = distance;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    @Override
    public int hashCode() {
        return 17 * getUsername().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof User)) {
            return false;
        }
        return getUsername().equals(((User) o).getUsername());
    }

    public String getBuddyRequest() {
        return BuddyRequest;
    }

    public void setBuddyRequest(String buddyRequest) {
        BuddyRequest = buddyRequest;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                ", Gender='" + Gender + '\'' +
                ", unreadMsgCount=" + unreadMsgCount +
                ", header='" + header + '\'' +
                ", avatar='" + avatar + '\'' +
                ", Brithday='" + Brithday + '\'' +
                ", CreditPoint='" + CreditPoint + '\'' +
                ", salaryId='" + salaryId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", jobName='" + jobName + '\'' +
                ", areaId='" + areaId + '\'' +
                ", areaName='" + areaName + '\'' +
                ", iconPath='" + iconPath + '\'' +
                ", BR_ID='" + BR_ID + '\'' +
                ", BuddyStatus='" + BuddyStatus + '\'' +
                ", BuddyRequest='" + BuddyRequest + '\'' +
                ", Distance='" + Distance + '\'' +
                ", UserLevel='" + UserLevel + '\'' +
                ", hxUser='" + hxUser + '\'' +
                ", signtext='" + signtext + '\'' +
                ", mail='" + mail + '\'' +
                ", hobbies='" + hobbies + '\'' +
                '}';
    }
}
