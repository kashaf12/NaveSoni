package www.kfstudio.com.navesoni;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

import www.kfstudio.com.navesoni.adapters.UploadAdapter;
import www.kfstudio.com.navesoni.models.Image;
import www.kfstudio.com.navesoni.models.Uploadmodel;
import www.kfstudio.com.navesoni.services.UploadService;

public class UploadActivity extends AppCompatActivity implements UploadAdapter.FilesClickListener {

    private static final String TAG = "UploadActivity";
    private static final int RC_TAKE_PICTURE = 101;
    private static final String KEY_FILE_URI = "key_file_uri";
    private RecyclerView recyclerView;
    private TextView noImageText, title;
    private Button chooseImage, uploadImage;
    private ArrayList<Image> file;
    private ArrayList<Uploadmodel> mfiles;
    private Uri mFileUri = null;
    private ArrayList<Uri> mFileUris = null;
    private UploadAdapter uploadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        recyclerView = findViewById(R.id.recyclerViewUpload);
        noImageText = findViewById(R.id.noImageText);
        chooseImage = findViewById(R.id.buttonChoose);
        uploadImage = findViewById(R.id.buttonUpload);
        title = findViewById(R.id.title);
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
        // Local broadcast receiver

        file = new ArrayList<>();
        if (file == null || file.size() < 1) {
            changeVisibility(1);
        } else {
            changeVisibility(0);
        }
        uploadAdapter = new UploadAdapter(file, this);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        uploadAdapter.setCheckedListener(this);
        recyclerView.setAdapter(uploadAdapter);
        recyclerView.setHasFixedSize(true);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mfiles != null) {
                    mfiles.clear();
                } else {
                    mfiles = new ArrayList<>();
                }

                for (int i = 0; i < file.size(); i++) {
                    if (file.get(i).getChecked()) {
                        mfiles.add(new Uploadmodel(file.get(i).getFile(), file.get(i).getCategory()));
                    }
                }
                uploadFromUri(mfiles);

            }
        });

    }

    private void changeVisibility(int n) {
        if (n == 0) {
            noImageText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            title.setText("Selected Images");
            uploadImage.setVisibility(View.VISIBLE);
            chooseImage.setVisibility(View.GONE);
        } else {
            noImageText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            title.setText("Upload Images");
            uploadImage.setVisibility(View.GONE);
            chooseImage.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (file != null)
            file.clear();

        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {

                if (data.getClipData() != null) {

                    changeVisibility(0);
                    int totalItemSelected = data.getClipData().getItemCount();
                    for (int i = 0; i < totalItemSelected; i++) {
                        file.add(new Image(data.getClipData().getItemAt(i).getUri(), true, "wedding"));
                        uploadAdapter.notifyDataSetChanged();

                    }
//                    newFile=file;

                } else if (data.getData() != null) {
                    mFileUri = data.getData();
                    if (mFileUri != null) {
                        changeVisibility(0);
                        file.add(new Image(mFileUri, true, "wedding"));
                        uploadAdapter.notifyDataSetChanged();
//                        newFile=new ArrayList<>();
//                        newFile.add(mFileUri);

                    } else {
                        changeVisibility(1);
                        Log.w(TAG, "Image not found!");
                    }
                }

            } else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void uploadFromUri(ArrayList<Uploadmodel> file) {
        // Save the File URI
//        mfiles = file;

        // Clear the last download, if any

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, UploadService.class)
                .putParcelableArrayListExtra(UploadService.EXTRA_ARRAY_FILE_URI, file)
                .setAction(UploadService.ACTION_UPLOAD_MULTIPLE));
        finish();
    }


    private void launchCamera() {
        Log.d(TAG, "launchCamera");

        // Pick an image from storage
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RC_TAKE_PICTURE);
    }


    @Override
    public void onfileClick(ArrayList<Image> checkedfiles) {
        file = checkedfiles;

    }
}
