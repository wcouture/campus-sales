package com.zybooks.campussales.ContentCards;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;

import com.zybooks.campussales.Data.DataRetriever;
import com.zybooks.campussales.Data.Message;
import com.zybooks.campussales.Data.PostRepository;
import com.zybooks.campussales.MainActivity;
import com.zybooks.campussales.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class MessageCardView extends CardView {

    private Context context;

    public MessageCardView(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public void setMessageData(Message msg) {
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        itemParams.setMargins(20, 5, 20, 5);
        itemParams.gravity = Gravity.CENTER;

        LinearLayout listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);

        TextView messageTitle = new TextView(context);
        messageTitle.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        messageTitle.setText(msg.getMsgType().getName() + " | " + msg.getPostTitle());
        messageTitle.setTextSize(22);

        TextView senderTV = new TextView(context);
        senderTV.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        senderTV.setText("From: " + msg.getSenderName());
        senderTV.setTextSize(17);

        TextView contents = new TextView(context);
        contents.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        contents.setTextSize(14);

        listContainer.addView(messageTitle, itemParams);
        listContainer.addView(senderTV, itemParams);
        listContainer.addView(contents, itemParams);

        switch (msg.getMsgType()){
            case BUY_REQUEST:
                contents.setText(getResources().getText(R.string.buy_request_message) + " " + msg.getPostTitle());
                LinearLayout buttonBar = new LinearLayout(context);
                buttonBar.setOrientation(LinearLayout.HORIZONTAL);
                buttonBar.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                buttonParams.setMargins(10, 10, 10, 10);
                buttonParams.gravity = Gravity.CENTER;

                Button acceptButton = new Button(context);
                acceptButton.setText("Accept");
                acceptButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Open location and time fragment
                        // Message will be sent when that form is submitted
                        Message.current_message_interacted = msg;
                        Navigation.findNavController(v).navigate(R.id.nav_meeting_details);
                    }
                });

                Button declineButton = new Button(context);
                declineButton.setText("Decline");
                declineButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PostRepository.getInstance(context).deleteMessage(msg, new DataRetriever.MessageDeletionHandler() {
                            @Override
                            public void onResponse(boolean result) {
                                String outcome = result ? "Deleted Message" : "Failed to Delete Message";
                                MainActivity.refreshFragment(v);
                                Toast.makeText(context, outcome, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

                buttonBar.addView(acceptButton, buttonParams);
                buttonBar.addView(declineButton, buttonParams);

                listContainer.addView(buttonBar, itemParams);
                break;
            case MEETING_INFO:
                Button deleteButton = new Button(context);
                deleteButton.setText("Delete Message");
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PostRepository.getInstance(context).deleteMessage(msg, new DataRetriever.MessageDeletionHandler() {
                            @Override
                            public void onResponse(boolean result) {
                                String outcome = result ? "Deleted Message" : "Failed to Delete Message";
                                MainActivity.refreshFragment(v);
                                Toast.makeText(context, outcome, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                itemParams.setMargins(100, 10, 100, 10);
                listContainer.addView(deleteButton, itemParams);
                String info = "Failed to retrieve info";
                try{
                    JSONObject details = new JSONObject(msg.getContents());
                    info = details.getString("location") + "\n";
                    info += details.getString("time") + "\n";
                    info += details.getString("date") + "\n";

                }catch(Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                contents.setText(info);
        }


        this.addView(listContainer, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }
}
