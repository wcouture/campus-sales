package com.zybooks.campussales.Data;

public class Message {
    public enum Type{
        GENERAL("General"),
        BUY_REQUEST("Buy Request"),
        MEETING_INFO("Meeting Info");

        private final String name;
        Type(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }
    public static Message current_message_interacted = null;
    private long recipient_auth_id;
    private String recipient_name;
    private long sender_auth_id;
    private String sender_name;


    private long post_id;
    private String post_title;
    private String contents;
    private Type msgType;

    public Message(long sender, String sender_name, long recipient, String recipient_name, long post_id, String post_title, String content, Type msgType){
        this.recipient_auth_id = recipient;
        this.recipient_name = recipient_name;
        this.sender_name = sender_name;
        this.sender_auth_id = sender;
        this.contents = content;
        this.msgType = msgType;
        this.post_id = post_id;
        this.post_title = post_title;
    }

    public long getSender(){
        return sender_auth_id;
    }
    public String getSenderName() { return sender_name; }

    public long getRecipient(){
        return recipient_auth_id;
    }
    public String getRecipientName() { return recipient_name; }

    public long getPostId() { return post_id; }
    public String getPostTitle() { return post_title; }

    public String getContents(){
        return contents;
    }
    public Type getMsgType(){
        return msgType;
    }
}
