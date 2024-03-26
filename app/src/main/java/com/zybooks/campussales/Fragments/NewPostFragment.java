package com.zybooks.campussales.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.zybooks.campussales.AccountManager;
import com.zybooks.campussales.Data.DataRetriever;
import com.zybooks.campussales.Data.Post;
import com.zybooks.campussales.Data.PostRepository;
import com.zybooks.campussales.R;

import java.io.IOException;

public class NewPostFragment extends Fragment {

    private Bitmap img = null;
    private ImageView preview;
    private ActivityResultLauncher<Uri> pickImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent image_picker = new Intent();

        image_picker.setType("image/*");
        image_picker.setAction(Intent.ACTION_GET_CONTENT);

        pickImage = registerForActivityResult(new ActivityResultContract<Uri, Uri>() {
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Uri uri) {
                return image_picker;
            }

            @Override
            public Uri parseResult(int i, @Nullable Intent intent) {
                return intent.getData();
            }
        }, new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                // Handle the returned Uri
                try {
                    img = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), result);
                    preview.setImageBitmap(img);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @NonNull ViewGroup container,
                             Bundle savedInstanceState) {
        // Set enter and exit transitions
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_right));
        setExitTransition(transitionInflater.inflateTransition(R.transition.slide_right));
                // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button select_image = view.findViewById(R.id.select_image_button);
        Button submit_button = view.findViewById(R.id.submit_button);
        NumberPicker numberPicker = view.findViewById(R.id.price_picker);
        numberPicker.setMaxValue(9999);
        numberPicker.setMinValue(0);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                String output = "$ " + String.valueOf(value);
                return output;
            }
        });

        preview = view.findViewById(R.id.image_preview);

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage.launch(null);
            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });

    }

    public void onSubmit() {
        EditText title_field = getView().findViewById(R.id.title_edit_text);
        EditText description_field = getView().findViewById(R.id.desc_edit_text);
        NumberPicker price_picker = getView().findViewById(R.id.price_picker);

        if (title_field.getText().toString().length() < 1 || description_field.getText().toString().length() < 1 || img == null){
            Toast popup = new Toast(getContext());
            CharSequence text = "Enter post details";
            popup.setText(text);
            popup.show();
            return;
        }
        AccountManager account = AccountManager.getInstance(getContext());

        Post newPost = new Post(price_picker.getValue(),title_field.getText().toString(), description_field.getText().toString(), account.getAuth_id(), -1, img);
        PostRepository.getInstance(requireContext()).uploadPost(newPost, new DataRetriever.PostUploadHandler() {
            @Override
            public void onSuccessfulUpload() {
                Navigation.findNavController(getView()).navigate(R.id.nav_dashboard);
            }

            @Override
            public void onFailure(String error) {
                CharSequence text = error;
                Toast toast = Toast.makeText(getContext(), text, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}