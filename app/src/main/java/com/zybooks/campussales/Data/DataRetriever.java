package com.zybooks.campussales.Data;

import static android.service.controls.ControlsProviderService.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataRetriever {
    public interface MessageDeletionHandler{
        void onResponse(boolean result);
        void onFailure(String error);
    }
    public interface MessageTransactionHandler{
        void onMessageSent();
        void onSendFailure(String error);
    }
    public interface MessageRetrievalHandler{
        void onMessagesRetrieved(List<Message> messages);
        void onFailure(String error);
    }
    public interface PostDataHandler{
        void onPostsRetrieved(List<Post> posts);
        void onFailureToRetrieve(String error);
    }
    public interface PostUploadHandler{
        void onSuccessfulUpload();
        void onFailure(String error);
    }
    public interface PostDeletionHandler{
        void onSuccessfulDeletion();
        void onFailure(String error);
    }
    public static class Filter{
        public final boolean filtered;
        public final long author_id;
        public final int oldest_timestamp;
        public final int newest_timestamp;
        public final int price_min;
        public final int price_max;

        public Filter(boolean filtered, int oldest_timestamp, int newest_timestamp, int price_max, int price_min, long author_id){
            this.filtered = filtered;
            this.oldest_timestamp = oldest_timestamp;
            this.newest_timestamp = newest_timestamp;
            this.price_max = price_max;
            this.price_min = price_min;
            this.author_id = author_id;
        }
    }
    private static final String BASE_URL = "https://fo-stats.willc-dev.net/sales/";
    private static final boolean USE_DEFAULT_DATA = false;
    private RequestQueue requestQueue;
    private Context context;
    public DataRetriever(Context context){
        requestQueue = Volley.newRequestQueue(context);
        this.context = context;
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void uploadPost(Post post, PostUploadHandler PUH){
        String url = BASE_URL + "/upload";
        VolleyMultipartRequest postUploadRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String result = "";
                        if(response.statusCode == 200){
                            result = "Successfully Uploaded Post";
                            PUH.onSuccessfulUpload();
                        }
                        else{
                            result = "Failure to Upload Post";
                            PUH.onFailure(response.data.toString());
                        }
                        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                        PUH.onFailure(error.getMessage());
                    }
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart(post.getImg().toString(),getFileDataFromDrawable(post.getImg())));
                return params;
            }

            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("title", post.getTitle());
                params.put("desc", post.getDescription());
                params.put("price", String.valueOf(post.getPrice()));
                params.put("authId", String.valueOf(post.getAuthor_id()));
                params.put("postId", String.valueOf(post.getPost_id()));
                return params;
            }
        };

        requestQueue.add(postUploadRequest);
    }

    public void retrievePosts(Filter filter, int num_posts, PostDataHandler pdh){
        String url = BASE_URL + "/posts";
        if (filter.filtered) url += "/filtered";

        url = Uri.parse(url).buildUpon()
                .appendQueryParameter("price_min", String.valueOf(filter.price_min))
                .appendQueryParameter("price_max", String.valueOf(filter.price_max))
                .appendQueryParameter("oldest_stamp", String.valueOf(filter.oldest_timestamp))
                .appendQueryParameter("newest_stamp", String.valueOf(filter.newest_timestamp))
                .appendQueryParameter("auth_id", String.valueOf(filter.author_id))
                .build().toString();

        // Request all subjects
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    // Get post data
                    List<Post> postData = jsonToPosts(response);
                    if(postData.size() <= 0){
                        pdh.onPostsRetrieved(postData);
                        return;
                    }
                    // Get image for each post
                    for(int i = 0; i < postData.size(); i++){
                        long post_id = postData.get(i).getPost_id();
                        int postIndex = i;

                        String getImageURL = Uri.parse(BASE_URL + "/image").buildUpon().appendQueryParameter("postId", String.valueOf(post_id)).build().toString();

                        ImageRequest imageRequest = new ImageRequest(getImageURL, new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap iresponse) {
                                if(postData.size() > 0) postData.get(postIndex).setImg(iresponse);
                                if(postIndex == (postData.size() - 1)) pdh.onPostsRetrieved(postData);
                            }
                        }, 500, 500, ImageView.ScaleType.CENTER_CROP, null, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("IMG", "Error retrieving post image: " + error.toString());
                                if(postIndex == (postData.size() - 1)) pdh.onPostsRetrieved(postData);
                            }

                        });
                        requestQueue.add(imageRequest);
                    }
                },
                error -> pdh.onFailureToRetrieve(error.toString()));

        requestQueue.add(request);
    }

    public void removePost(PostDeletionHandler deleteHandler, long post_id, long auth_id){
        try {
            JSONObject deletionDetails = new JSONObject();
            deletionDetails.put("postId", post_id);
            deletionDetails.put("authId", auth_id);
            JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL + "/delete/post", deletionDetails, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("result")){
                            deleteHandler.onSuccessfulDeletion();
                        }
                        else{
                            deleteHandler.onFailure("Failed to verify post and/or authorization.");
                        }
                    }catch(JSONException exception){
                        deleteHandler.onFailure("Improperly formatted response");
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    deleteHandler.onFailure(error.toString());
                }
            });
            requestQueue.add(deleteRequest);
        }catch(JSONException jsonException){
            deleteHandler.onFailure(jsonException.toString());
        }

    }

    public void sendMessage(Message msg, MessageTransactionHandler messageTransactionHandler){
        try{
            JSONObject message = new JSONObject();
            message.put("recipient", msg.getRecipient());
            message.put("sender", msg.getSender());
            message.put("type", msg.getMsgType());
            message.put("contents", msg.getContents());
            message.put("post_id", msg.getPostId());
            message.put("post_title", msg.getPostTitle());

            JsonObjectRequest sendMessageRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL + "/send", message,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                if(response.getBoolean("result")){
                                    messageTransactionHandler.onMessageSent();
                                }
                                else{
                                    messageTransactionHandler.onSendFailure("Failure sending Message");
                                }
                            }catch(JSONException exception){
                                exception.printStackTrace();
                                messageTransactionHandler.onSendFailure(exception.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            messageTransactionHandler.onSendFailure(error.getMessage());
                        }
            });

            requestQueue.add(sendMessageRequest);
        }catch (JSONException exception){
            exception.printStackTrace();
            messageTransactionHandler.onSendFailure(exception.toString());
        }
    }

    public void getMessages(long auth_id, MessageRetrievalHandler handler){
        try{
            String url = Uri.parse(BASE_URL + "/messages").buildUpon().appendQueryParameter("authId", String.valueOf(auth_id)).build().toString();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    List<Message> messages = jsonToMessages(response);
                    handler.onMessagesRetrieved(messages);
                }
            }, (error -> {
                handler.onFailure(error.getMessage());
            }));
            requestQueue.add(request);
        }catch(Exception exception){
            handler.onFailure(exception.toString());
        }
    }

    public void deleteMessage(Message msg, MessageDeletionHandler handler){
        try{
            JSONObject details = new JSONObject();
            details.put("recipientId", msg.getRecipient());
            details.put("senderId", msg.getSender());
            details.put("type", msg.getMsgType());
            details.put("postId", msg.getPostId());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL + "/delete/message", details, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println("Delete response received");
                    try {
                        handler.onResponse(response.getBoolean("result"));
                    } catch (JSONException e) {
                        handler.onFailure(e.getMessage());
                    }
                }
            }, (error -> { handler.onFailure(error.getMessage()); }));
            requestQueue.add(request);
        }catch(Exception e){
            handler.onFailure(e.getMessage());
        }
    }

    public List<Message> jsonToMessages(JSONObject json){
        List<Message> messageList = new ArrayList<>();
        try{
            JSONArray messageArray = json.getJSONArray("messages");

            for(int i = 0; i < messageArray.length(); i++){
                JSONObject message = messageArray.getJSONObject(i);

                System.out.println(message.toString());
                Message msg = new Message(
                        message.getLong("sender"),
                        message.getString("sender_name"),
                        message.getLong("recipient"),
                        message.getString("recipient_name"),
                        message.getLong("post_id"),
                        message.getString("post_title"),
                        message.getString("contents"),
                        stringToMessageType(message.getString("type")));


                messageList.add(msg);
            }
        }catch(Exception e){
            Log.e(TAG, "Field missing in the JSON data: " + e.getMessage());
        }
        return messageList;
    }

    public Message.Type stringToMessageType(String type){
        switch(type){
            case "BUY_REQUEST":
                return Message.Type.BUY_REQUEST;
            case "MEETING_INFO":
                return Message.Type.MEETING_INFO;
            default:
                return Message.Type.GENERAL;
        }
    }

    public List<Post> jsonToPosts(JSONObject json){
        // Create a list of subjects
        List<Post> postList = new ArrayList<>();

        try {
            JSONArray postArray = json.getJSONArray("posts");

            for (int i = 0; i < postArray.length(); i++) {
                JSONObject postObj = postArray.getJSONObject(i);

                Post post = new Post(postObj.getInt("price"), postObj.getString("title"), postObj.getString("desc"), postObj.getLong("auth_id"), postObj.getLong("post_id"), null);

                postList.add(post);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Field missing in the JSON data: " + e.getMessage());
        }
        return postList;
    }
}
