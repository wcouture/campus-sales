package com.zybooks.campussales.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zybooks.campussales.AccountManager;
import com.zybooks.campussales.ContentCards.MessageCardView;
import com.zybooks.campussales.Data.DataRetriever;
import com.zybooks.campussales.Data.Message;
import com.zybooks.campussales.Data.PostRepository;
import com.zybooks.campussales.R;

import java.util.List;

public class InboxFragment extends Fragment implements DataRetriever.MessageRetrievalHandler {

    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set exit transition
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setExitTransition(transitionInflater.inflateTransition(R.transition.slide_exit));
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_enter));

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        this.rootView = rootView;

        AccountManager account = AccountManager.getInstance(rootView.getContext());
        if(account.isLoggedIn() == false){
            TextView label = rootView.findViewById(R.id.loading_messages_textview);
            label.setText("Log in to receive messages");
            return;
        }

        PostRepository repo = PostRepository.getInstance(rootView.getContext());
        repo.getMessages(account.getAuth_id(), this);
    }


    @Override
    public void onMessagesRetrieved(List<Message> messages) {
        TextView loadingTV = rootView.findViewById(R.id.loading_messages_textview);
        LinearLayout content = rootView.findViewById(R.id.content_linearview);

        if(messages.size() == 0){
            loadingTV.setText(getText(R.string.inbox_empty));
            return;
        }

        content.removeAllViews();

        for(Message m : messages){
            MessageCardView messageCardView = new MessageCardView(rootView.getContext());
            messageCardView.setMessageData(m);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(20, 20, 20, 20);


            content.addView(messageCardView, cardParams);
        }

    }

    @Override
    public void onFailure(String error) {
        Toast.makeText(rootView.getContext(), "Error Loading Messages", Toast.LENGTH_SHORT).show();
        System.out.println(error);
    }
}