package com.gbsoft.chitchat.model;

/**
 * Created by chiranjeevi on 2/15/18.
 */

public class Message {
    private String userName;
    private String messageContent;
    private String dateSent;
    private String messageId;
    private String photoUrl;

    public Message() {}

    public Message(String userName, String messageContent, String dateSent, String messageId, String photoUrl) {
        this.userName = userName;
        this.messageContent = messageContent;
        this.dateSent = dateSent;
        this.messageId = messageId;
        this.photoUrl = photoUrl;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDateSent() {
        return dateSent;
    }

    public void setDateSent(String dateSent) {
        this.dateSent = dateSent;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
