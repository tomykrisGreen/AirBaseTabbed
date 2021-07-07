package com.tomykrisgreen.airbasetabbed.Models;

public class ModelChatList {
    String id;  // We will need this id to get chat list, sender/receiver uid

    public ModelChatList() {
    }

    public ModelChatList(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
