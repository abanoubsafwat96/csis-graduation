package com.csis.social.app.models;

public class ModelPost {
    //use same name as we given while uploading post
    String pId;
    String pTitle;
    String pDescr;
    String pLikes;
    String pComments;
    String pImage;
    String pTime;
    String uid;
    String uEmail;
    String uDp;
    String uName;
    String subject;
    String userNode;
    String pVideo;
    String pVideoName;

    public String getUserNode() {
        return userNode;
    }

    public void setUserNode(String userNode) {
        this.userNode = userNode;
    }

    public ModelPost() {
    }

    public ModelPost(String pId, String pTitle, String pDescr, String pLikes, String pComments, String pImage, String pTime, String uid, String uEmail, String uDp, String uName, String subject, String userNode, String pVideo, String pVideoName) {
        this.pId = pId;
        this.pTitle = pTitle;
        this.pDescr = pDescr;
        this.pLikes = pLikes;
        this.pComments = pComments;
        this.pImage = pImage;
        this.pTime = pTime;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;
        this.subject = subject;
        this.userNode = userNode;
        this.pVideo = pVideo;
        this.pVideoName = pVideoName;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTitle() {
        return pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getpDescr() {
        return pDescr;
    }

    public void setpDescr(String pDescr) {
        this.pDescr = pDescr;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getpVideo() {
        return pVideo;
    }

    public void setpVideo(String pVideo) {
        this.pVideo = pVideo;
    }

    public String getpVideoName() {
        return pVideoName;
    }

    public void setpVideoName(String pVideoName) {
        this.pVideoName = pVideoName;
    }
}
