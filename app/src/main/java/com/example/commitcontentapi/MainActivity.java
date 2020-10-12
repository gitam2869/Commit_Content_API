package com.example.commitcontentapi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MaterialButton materialButtonEnableKeyboard;
    private MaterialButton materialButtonChooseKeyboard;
    private InputContentInfoCompat mCurrentInputContentInfo;
    private int mCurrentFlags;
    private ImageViewModel imageViewModel;
    private List<ImageModel> imageList;
    private RecyclerView recyclerView;
    private ImageRecyclerViewAdapter imageRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        imageViewModel = new ViewModelProvider(this).get(ImageViewModel.class);
        imageList = new ArrayList<>();
        recyclerView = findViewById(R.id.rv_image_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(imageRecyclerViewAdapter);
        imageRecyclerViewAdapter = new ImageRecyclerViewAdapter(imageList);

        materialButtonEnableKeyboard = findViewById(R.id.btn_enable_keyboard);
        materialButtonChooseKeyboard = findViewById(R.id.btn_choose_keyboard);

        final LinearLayout layout = (LinearLayout) findViewById(R.id.commit_content_sample_edit_boxes);

        layout.addView(createEditTextWithContentMimeTypes(new String[]{"image/png"}));

        materialButtonEnableKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isKeyboardEnabled()) {
                    DisplayUtility.toastMessage(getApplicationContext(), "Your Keyboard is Enabled");
                } else {
                    Intent enableIntent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                    enableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(enableIntent);
                }
            }
        });

        materialButtonChooseKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isKeyboardEnabled()) {
                    InputMethodManager ime = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (ime != null) {
                        ime.showInputMethodPicker();
                    }
                } else {
                    Intent enableIntent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                    enableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
                }
            }
        });


        if (savedInstanceState != null) {
            final InputContentInfoCompat previousInputContentInfo = InputContentInfoCompat.wrap(
                    savedInstanceState.getParcelable(Constant.INPUT_CONTENT_INFO_KEY));
            final int previousFlags = savedInstanceState.getInt(Constant.COMMIT_CONTENT_FLAGS_KEY);
            if (previousInputContentInfo != null) {
                onCommitContentInternal(previousInputContentInfo, previousFlags);
            }
        }

        imageViewModel.getAllImages().observe(this, new Observer<List<ImageModel>>() {
            @Override
            public void onChanged(List<ImageModel> imageModels) {
                imageList = imageModels;
                setAdapter();
            }
        });
    }

    private void setAdapter() {
        imageRecyclerViewAdapter = new ImageRecyclerViewAdapter(imageList);
        recyclerView.setAdapter(imageRecyclerViewAdapter);
        recyclerView.scrollToPosition(imageList.size() - 1);
    }

    private boolean isKeyboardEnabled() {
        String packageLocal = getPackageName();
        boolean isInputDeviceEnabled = false;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> list = inputMethodManager.getEnabledInputMethodList();

        // check if our keyboard is enabled as input method
        for (InputMethodInfo inputMethod : list) {
            String packageName = inputMethod.getPackageName();
            if (packageName.equals(packageLocal)) {
                isInputDeviceEnabled = true;
                break;
            }
        }

        return isInputDeviceEnabled;
    }

    private EditText createEditTextWithContentMimeTypes(String[] contentMimeTypes) {
        final CharSequence hintText;
        final String[] mimeTypes;  // our own copy of contentMimeTypes.
        if (contentMimeTypes == null || contentMimeTypes.length == 0) {
            hintText = "MIME: []";
            mimeTypes = new String[0];
        } else {
            hintText = "MIME: " + Arrays.toString(contentMimeTypes);
            mimeTypes = Arrays.copyOf(contentMimeTypes, contentMimeTypes.length);
        }
        EditText exitText = new EditText(this) {
            @Override
            public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
                final InputConnection ic = super.onCreateInputConnection(editorInfo);
                EditorInfoCompat.setContentMimeTypes(editorInfo, mimeTypes);
                final InputConnectionCompat.OnCommitContentListener callback =
                        new InputConnectionCompat.OnCommitContentListener() {
                            @Override
                            public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                           int flags, Bundle opts) {
                                return MainActivity.this.onCommitContent(
                                        inputContentInfo, flags, opts, mimeTypes);
                            }
                        };
                return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
            }
        };
        exitText.setHint(hintText);
        exitText.setTextColor(Color.BLACK);
        exitText.setHintTextColor(Color.GRAY);
        return exitText;
    }


    private boolean onCommitContent(
            InputContentInfoCompat inputContentInfo,
            int flags,
            Bundle opts,
            String[] contentMimeTypes) {
        // Clear the temporary permission (if any).  See below about why we do this here.
        try {
            if (mCurrentInputContentInfo != null) {
                mCurrentInputContentInfo.releasePermission();
            }
        } catch (Exception e) {
            Log.e("TAG", "InputContentInfoCompat#releasePermission() failed.", e);
        } finally {
            mCurrentInputContentInfo = null;
        }


        boolean supported = false;
        for (final String mimeType : contentMimeTypes) {
            if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
                supported = true;
                break;
            }
        }
        if (!supported) {
            return false;
        }

        return onCommitContentInternal(inputContentInfo, flags);
    }


    private boolean onCommitContentInternal(InputContentInfoCompat inputContentInfo, int flags) {
        if ((flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                inputContentInfo.requestPermission();
            } catch (Exception e) {
                Log.e("TAG", "InputContentInfoCompat#requestPermission() failed.", e);
                return false;
            }
        }

        mCurrentInputContentInfo = inputContentInfo;
        mCurrentFlags = flags;

        if (inputContentInfo.getContentUri().getAuthority().equals(Constant.AUTHORITY)) {
            imageViewModel.insertImage(new ImageModel(inputContentInfo.getContentUri().getLastPathSegment(), System.currentTimeMillis()));
        } else {
            Uri fullPhotoUri = inputContentInfo.getContentUri();
            File outputImage = ImageUtility.createImageFile(this, null);
            ImageUtility.copyImage(this, fullPhotoUri, outputImage);
            imageViewModel.insertImage(new ImageModel(outputImage.getName(), System.currentTimeMillis()));
        }

        return true;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mCurrentInputContentInfo != null) {
            savedInstanceState.putParcelable(Constant.INPUT_CONTENT_INFO_KEY,
                    (Parcelable) mCurrentInputContentInfo.unwrap());
            savedInstanceState.putInt(Constant.COMMIT_CONTENT_FLAGS_KEY, mCurrentFlags);
        }
        mCurrentInputContentInfo = null;
        mCurrentFlags = 0;
        super.onSaveInstanceState(savedInstanceState);
    }
}