package com.zybooks.campussales.ContentCards;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.zybooks.campussales.AccountManager;
import com.zybooks.campussales.Data.Message;
import com.zybooks.campussales.Data.Post;
import com.zybooks.campussales.Data.DataRetriever;
import com.zybooks.campussales.Data.PostRepository;
import com.zybooks.campussales.R;

public class PostContentView extends CardView {
    public static LinearLayout.LayoutParams CardLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
    public static LinearLayout.LayoutParams BottomCardLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private Context context;
    public PostContentView(@NonNull Context context) {
        super(context);
        CardLayoutParams.setMargins(30, 40, 30, 0);
        CardLayoutParams.gravity = Gravity.BOTTOM;

        BottomCardLayoutParams.setMargins(30, 40, 30, 200);
        BottomCardLayoutParams.gravity = Gravity.BOTTOM;
        this.context = context;
    }

    public void setPostData(Post p){
        // Create image and label objects
        LinearLayout layout = new LinearLayout(context);
        layout.setDividerDrawable(getResources().getDrawable(R.drawable.baseline_message_24));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.BOTTOM);

        LinearLayout.LayoutParams ItemLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        ItemLayoutParams.setMargins(10, 10, 10, 10);


        TextView title = new TextView(context);
        title.setText(p.getTitle() + " | $" + String.valueOf(p.getPrice()));
        title.setTextAppearance(R.style.postTitle);
        title.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        TextView descriptionBox = new TextView(context);
        descriptionBox.setText(p.getDescription());
        descriptionBox.setTextAppearance(R.style.postDescription);
        descriptionBox.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        CardView imageWrapper = new CardView(context);
        LinearLayout.LayoutParams imageWrapperLayout =  new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageWrapperLayout.setMargins(0,50, 0, 0);
        imageWrapperLayout.gravity = Gravity.CENTER;

        imageWrapper.setRadius(50);
        ImageView postImage = new ImageView(context);
        postImage.setAdjustViewBounds(true);
        postImage.setMaxHeight(1000);

        postImage.setImageBitmap(p.getImg());

        imageWrapper.addView(postImage);
        imageWrapper.setElevation(20);

        // Like and Delete Buttons
        AccountManager myAccount = AccountManager.getInstance(context);

        LinearLayout buttonBar = new LinearLayout(context);
        buttonBar.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams buttonBarParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonBarParams.gravity = Gravity.CENTER;

        Button likeButton = new Button(context);
        likeButton.setText("Like");
        likeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PostRepository repo = PostRepository.getInstance(context);
                Message msg = new Message(myAccount.getAuth_id(), "", p.getAuthor_id(), "", p.getPost_id(), p.getTitle(), "", Message.Type.BUY_REQUEST);
                repo.sendMessage(msg, new DataRetriever.MessageTransactionHandler() {
                    @Override
                    public void onMessageSent() {
                        Toast.makeText(context, "Request Sent to Poster", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSendFailure(String error) {
                        Toast.makeText(context, "Error Sending Message", Toast.LENGTH_SHORT).show();
                        System.out.println(error);
                    }
                });
            }
        });
        Button deleteButton = new Button(context);
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Deleting post...", Toast.LENGTH_SHORT).show();
                PostRepository repo = PostRepository.getInstance(context);
                repo.deletePost(p, new DataRetriever.PostDeletionHandler() {
                    @Override
                    public void onSuccessfulDeletion() {
                        Toast.makeText(context, "Successfully Deleted Post", Toast.LENGTH_LONG).show();
                        NavDestination currDestination = Navigation.findNavController(v).getCurrentDestination();
                        Navigation.findNavController(v).navigate(currDestination.getId());
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(context, "Failure Deleting Post", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        if(myAccount.isLoggedIn() && myAccount.getAuth_id() == p.getAuthor_id())
            buttonBar.addView(deleteButton, buttonParams);
        else if(myAccount.isLoggedIn())
            buttonBar.addView(likeButton, buttonParams);

        // Add components to content view
        layout.addView(imageWrapper, imageWrapperLayout);
        layout.addView(title, ItemLayoutParams);
        layout.addView(descriptionBox, ItemLayoutParams);
        if (myAccount.isLoggedIn()) layout.addView(buttonBar, buttonBarParams);


        this.addView(layout, new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        this.setElevation(50);
    }


}
