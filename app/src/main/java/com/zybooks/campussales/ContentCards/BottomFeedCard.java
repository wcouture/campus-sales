package com.zybooks.campussales.ContentCards;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;

import com.zybooks.campussales.MainActivity;
import com.zybooks.campussales.R;

public class BottomFeedCard extends CardView {
    private final Context mcontext;
    public BottomFeedCard(@NonNull Context context) {
        super(context);
        this.mcontext = context;

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.BOTTOM);

        LinearLayout.LayoutParams buttonparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonparams.setMargins(20,200, 20, 20);
        buttonparams.gravity = Gravity.CENTER;

        Button toTopButton = new Button(context);
        toTopButton.setText("Refresh");
        toTopButton.setBackgroundColor(getResources().getColor(R.color.gold));
        toTopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.refreshFragment(v);
            }
        });

        layout.addView(toTopButton, buttonparams);
        this.addView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
    }


}
