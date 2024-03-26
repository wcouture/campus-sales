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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.zybooks.campussales.AccountManager;
import com.zybooks.campussales.ContentCards.MessageCardView;
import com.zybooks.campussales.Data.DataRetriever;
import com.zybooks.campussales.Data.Message;
import com.zybooks.campussales.Data.Post;
import com.zybooks.campussales.Data.PostRepository;
import com.zybooks.campussales.R;

import org.json.JSONException;
import org.json.JSONObject;

public class MeetingDetailsFragment extends Fragment implements View.OnClickListener{

    private View rootView;

    public MeetingDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Set exit transition
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setExitTransition(transitionInflater.inflateTransition(R.transition.slide_exit));
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_enter));

        return inflater.inflate(R.layout.fragment_meeting_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState){
        this.rootView = rootView;

        Spinner locations = (Spinner) rootView.findViewById(R.id.location_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(rootView.getContext(),
                R.array.meeting_locations, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locations.setAdapter(adapter);

        Button submit_button = (Button) rootView.findViewById(R.id.submit_details_button);
        submit_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        PostRepository repo = PostRepository.getInstance(v.getContext());
        AccountManager account = AccountManager.getInstance(v.getContext());
        Message currentMessage = Message.current_message_interacted;

        Spinner locations = (Spinner) rootView.findViewById(R.id.location_spinner);
        TimePicker time = (TimePicker) rootView.findViewById(R.id.time_picker);
        DatePicker date = (DatePicker) rootView.findViewById(R.id.date_picker);

        JSONObject meetingDetails = new JSONObject();
        try {
            meetingDetails.put("location", locations.getSelectedItem());
            meetingDetails.put("time", (time.getHour() % 12)+ ":" + (time.getMinute() <= 9 ? "0" : "") + time.getMinute() + " " + (time.getHour() / 12 >= 1 ? "pm" : "am"));
            meetingDetails.put("date", date.getMonth() + "/" + date.getDayOfMonth() + "/" + date.getYear());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String details = meetingDetails.toString();
        Message details_message = new Message(account.getAuth_id(), currentMessage.getRecipientName(), currentMessage.getSender(), currentMessage.getSenderName(), currentMessage.getPostId(), currentMessage.getPostTitle(), details, Message.Type.MEETING_INFO);

        repo.sendMessage(details_message, new DataRetriever.MessageTransactionHandler() {
            @Override
            public void onMessageSent() {
                repo.deletePost(new Post(-1, "null", "null",account.getAuth_id(), currentMessage.getPostId(), null), new DataRetriever.PostDeletionHandler() {
                    @Override
                    public void onSuccessfulDeletion() {
                        Toast.makeText(v.getContext(), "Meetings details sent\nPost removed", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(v.getContext(), error, Toast.LENGTH_LONG).show();
                    }
                });

                repo.deleteMessage(Message.current_message_interacted, new DataRetriever.MessageDeletionHandler() {
                    @Override
                    public void onResponse(boolean result) {
                    }

                    @Override
                    public void onFailure(String error) {

                    }
                });
                //Send the meeting details to yourself
                Message detailsToSelf = new Message(
                        details_message.getSender(),
                        details_message.getSenderName(),
                        details_message.getSender(),
                        details_message.getSenderName(),
                        details_message.getPostId(),
                        details_message.getPostTitle(),
                        details_message.getContents(),
                        details_message.getMsgType());
                repo.sendMessage(detailsToSelf, new DataRetriever.MessageTransactionHandler() {
                    @Override
                    public void onMessageSent() {
                        Navigation.findNavController(rootView).navigate(R.id.nav_inbox);
                    }

                    @Override
                    public void onSendFailure(String error) {
                        Toast.makeText(rootView.getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            }


            @Override
            public void onSendFailure(String error) {
                Toast.makeText(v.getContext(), error, Toast.LENGTH_LONG);
            }
        });

    }
}