package com.zybooks.campussales.Data;

import android.content.Context;
import android.widget.Toast;

import com.zybooks.campussales.AccountManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PostRepository {

    private static ExecutorService repoThreads = Executors.newFixedThreadPool(2);
    public interface PostVisualUpdater{
        public void onPostsRetrieved(List<Post> posts);
    }
    public static PostRepository instance;
    private DataRetriever data_retriever;
    private List<Post> post_cache;
    private int refresh_count = 4;
    private final Context context;
    private final int DEFAULT_POST_INCREMENT = 20;
    public static PostRepository getInstance(Context context) {
        if (instance == null){
            instance = new PostRepository(context);
        }
        return instance;
    }

    private PostRepository(Context context) {
        post_cache = new ArrayList<>();
        data_retriever = new DataRetriever(context);
        this.context = context;
    }

    private int inCache(long post_id){
        for(int i = 0; i < post_cache.size(); i++){
            if(post_cache.get(i).getPost_id() == post_id) return i;
        }
        return -1;
    }

    private void cachePosts(int num, PostVisualUpdater pvu, DataRetriever.Filter filter) {
        repoThreads.execute(() -> {
                DataRetriever.PostDataHandler post_cacher = new DataRetriever.PostDataHandler() {
                    @Override
                    public void onPostsRetrieved(List<Post> posts) {
                        post_cache.clear();
                        post_cache = posts;
                        pvu.onPostsRetrieved(post_cache);
                    }

                    @Override
                    public void onFailureToRetrieve(String error) {
                        pvu.onPostsRetrieved(post_cache);
                        CharSequence message = error;
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }
                };

                data_retriever.retrievePosts(filter, num, post_cacher);
        });
    }

    public Post getPost(long post_id){
        int index = inCache(post_id);
        if(index < 0){
            // Post not cached, fetch post from database

        }
        return post_cache.get(index);
    }

    public void sendMessage(Message msg, DataRetriever.MessageTransactionHandler callback){
        repoThreads.execute(()->{
          data_retriever.sendMessage(msg, callback);
        });
    }

    public void getMessages(long auth_id, DataRetriever.MessageRetrievalHandler handler){
        repoThreads.execute(()->{
            data_retriever.getMessages(auth_id, handler);
        });

    }

    public void deletePost(Post p, DataRetriever.PostDeletionHandler deleteHandler){
        repoThreads.execute(()->{
            data_retriever.removePost(deleteHandler, p.getPost_id(), AccountManager.getInstance(context).getAuth_id());
        });
    }

    public void getPosts(int num, PostVisualUpdater pvu, DataRetriever.Filter queryFilter) {
        repoThreads.execute(()->{
            cachePosts(num, pvu, queryFilter);
        });
    }

    public void uploadPost(Post newPost, DataRetriever.PostUploadHandler PUH){
        data_retriever.uploadPost(newPost, PUH);
    }

    public void deleteMessage(Message msg, DataRetriever.MessageDeletionHandler handler){
        repoThreads.execute(()->{
            data_retriever.deleteMessage(msg, handler);
        });
    }

}
