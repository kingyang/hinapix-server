package com.hinacom.pix.po;

import java.util.Date;

public class MessageStore {
    private Integer id;

    private String ip;

    private String outmessage;

    private String inmessage;

    private Date messagedate;

    private String messageid;

    private String messagecode;

    private String triggerevent;

    private String errormessage;

    private String sendingfacility;

    private String sendingapplication;

    private String receivingapplication;

    private String receivingfacility;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public String getOutmessage() {
        return outmessage;
    }

    public void setOutmessage(String outmessage) {
        this.outmessage = outmessage == null ? null : outmessage.trim();
    }

    public String getInmessage() {
        return inmessage;
    }

    public void setInmessage(String inmessage) {
        this.inmessage = inmessage == null ? null : inmessage.trim();
    }

    public Date getMessagedate() {
        return messagedate;
    }

    public void setMessagedate(Date messagedate) {
        this.messagedate = messagedate;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid == null ? null : messageid.trim();
    }

    public String getMessagecode() {
        return messagecode;
    }

    public void setMessagecode(String messagecode) {
        this.messagecode = messagecode == null ? null : messagecode.trim();
    }

    public String getTriggerevent() {
        return triggerevent;
    }

    public void setTriggerevent(String triggerevent) {
        this.triggerevent = triggerevent == null ? null : triggerevent.trim();
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage == null ? null : errormessage.trim();
    }

    public String getSendingfacility() {
        return sendingfacility;
    }

    public void setSendingfacility(String sendingfacility) {
        this.sendingfacility = sendingfacility == null ? null : sendingfacility.trim();
    }

    public String getSendingapplication() {
        return sendingapplication;
    }

    public void setSendingapplication(String sendingapplication) {
        this.sendingapplication = sendingapplication == null ? null : sendingapplication.trim();
    }

    public String getReceivingapplication() {
        return receivingapplication;
    }

    public void setReceivingapplication(String receivingapplication) {
        this.receivingapplication = receivingapplication == null ? null : receivingapplication.trim();
    }

    public String getReceivingfacility() {
        return receivingfacility;
    }

    public void setReceivingfacility(String receivingfacility) {
        this.receivingfacility = receivingfacility == null ? null : receivingfacility.trim();
    }
}