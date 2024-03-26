package com.zybooks.campussales.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zybooks.campussales.AccountManager;
import com.zybooks.campussales.ContentCards.BottomFeedCard;
import com.zybooks.campussales.Data.DataRetriever;
import com.zybooks.campussales.Data.Post;
import com.zybooks.campussales.ContentCards.PostContentView;
import com.zybooks.campussales.Data.PostRepository;
import com.zybooks.campussales.R;

import java.util.List;

public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private void refreshFeed(View rootView){
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.content_linearview);
        layout.setBackground(getResources().getDrawable(R.color.feed_background));
        TextView tv = (TextView) rootView.findViewById(R.id.debugTextView);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));


        PostRepository repo = PostRepository.getInstance(requireContext());
        PostRepository.PostVisualUpdater updateGraphics = new PostRepository.PostVisualUpdater() {
            @Override
            public void onPostsRetrieved(List<Post> posts) {
                layout.removeAllViews();

                if(posts.size() == 0){
                    TextView noPostsLabel = new TextView(rootView.getContext());
                    noPostsLabel.setText(getText(R.string.no_feed_posts));
                    noPostsLabel.setTextSize(20);
                    noPostsLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    layout.addView(noPostsLabel, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    return;
                }

                for(int i = 0; i < posts.size(); i++) {
                    // Create post card view
                    PostContentView contentView = new PostContentView(rootView.getContext());
                    contentView.setPostData(posts.get(i));

                    // Add contentview to the main LinearLayout
                    layout.addView(contentView, PostContentView.CardLayoutParams);
                }
                BottomFeedCard divider = new BottomFeedCard(rootView.getContext());
                divider.setMinimumHeight(800);
                divider.setElevation(0);
                divider.setBackgroundColor(getResources().getColor(R.color.feed_background));
                layout.addView(divider, PostContentView.CardLayoutParams);
            }
        };

        repo.getPosts(20, updateGraphics, new DataRetriever.Filter(false, -1, -1, -1, -1, -1));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        // Initialize floating action button to transition to add post fragment
        FloatingActionButton addPost = rootView.findViewById(R.id.add_post_button);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open page to add post information
                if(AccountManager.getInstance(requireContext()).isLoggedIn())
                    Navigation.findNavController(rootView).navigate(R.id.nav_new_post);
                else{
                    CharSequence text = "Must be logged in to post";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(requireContext(), text, duration);
                    toast.show();
                }
            }
        });

        // Set enter and exit transitions
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setExitTransition(transitionInflater.inflateTransition(R.transition.slide_exit));
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_enter));

        // Add posts to view
        refreshFeed(rootView);

    }
}