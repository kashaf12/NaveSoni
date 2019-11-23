package www.kfstudio.com.navesoni.services;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import www.kfstudio.com.navesoni.MainActivity;
import www.kfstudio.com.navesoni.R;
import www.kfstudio.com.navesoni.models.Uploadmodel;

/**
 * Service to handle uploading files to Firebase Storage.
 */
public class UploadService extends BaseTaskService {

    /**
     * Intent Actions
     **/
    public static final String ACTION_UPLOAD_MULTIPLE = "action_upload_multiple";
    public static final String UPLOAD_COMPLETED = "upload_completed";
    public static final String UPLOAD_ERROR = "upload_error";
    public static final String UPLOAD_FINISHED = "upload_finished";
    /**
     * Intent Extras
     **/
    public static final String EXTRA_FILE_URI = "extra_file_uri";
    public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";
    public static final String EXTRA_ARRAY_FILE_URI = "extra_array_file_uri";
    public static final String EXTRA_UPLOAD_FINISHED = "extra_download_finished";
    private static final String TAG = "UploadService";
    // [START declare_ref]
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    // [END declare_ref]
    private ArrayList<Uri> downloadUrls;
    private Map<String, Object> data;

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPLOAD_COMPLETED);
        filter.addAction(UPLOAD_ERROR);

        return filter;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        // [END get_storage_ref]
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);
        if (ACTION_UPLOAD_MULTIPLE.equals(intent.getAction())) {
            ArrayList<Uploadmodel> fileUri1 = intent.getParcelableArrayListExtra(EXTRA_ARRAY_FILE_URI);
            for (int i = 0; i < fileUri1.size(); i++) {
                getContentResolver().takePersistableUriPermission(
                        fileUri1.get(i).getImageUri(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            uploadFromUri(fileUri1);

        }

        return START_REDELIVER_INTENT;
    }

    private void uploadFromUri(final ArrayList<Uploadmodel> file) {
        Log.d(TAG, "uploadFromUri: " + file.toArray().toString());
        downloadUrls = new ArrayList<>();
        downloadUrls.clear();

        taskStarted();
        for (int i = 0; i < file.size(); i++) {
            // [START_EXCLUDE]
            final int j = i;
            final long time = System.currentTimeMillis();
            showProgressNotification(getString(R.string.progress_uploading) + "(" + (j + 1) + "/" + (file.size()) + ")", 0, 0);

            // [END_EXCLUDE]
            final Uri files = file.get(i).getImageUri();
            final String category = file.get(i).getCategory();
            // [START get_child_ref]
            // Get a reference to store file at photos/<FILENAME>.jpg

            final StorageReference photoRef = mStorageRef.child("photos")
                    .child(time + ".jpg");
            // [END get_child_ref]

            // Upload file to Firebase Storage
            Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
            photoRef.putFile(files).
                    addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            showProgressNotification(getString(R.string.progress_uploading) + "(" + (j + 1) + "/" + (file.size()) + ")",
                                    taskSnapshot.getBytesTransferred(),
                                    taskSnapshot.getTotalByteCount());
                        }
                    })
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            // Forward any exceptions
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            Log.d(TAG, "uploadFromUri: upload success");

                            // Request the public download URL
                            return photoRef.getDownloadUrl();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(@NonNull final Uri downloadUri) {
                            if (data != null) {
                                data.clear();
                            } else {
                                data = new HashMap<>();
                            }
                            data.put("imageUrl", downloadUri.toString().replace(time + ".jpg", time + "_400x400.jpg"));
                            data.put("imageName", time + ".jpg");
                            data.put("thumbnailName", time + "_400x400.jpg");
                            data.put("downloadUrl", downloadUri.toString());
                            data.put("category", category);
                            data.put("time", new Timestamp(new Date()));
                            Log.d(TAG, "uploadFromUri: getDownloadUri success");
                            downloadUrls.add(downloadUri);
                            db.collection("Images").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    broadcastUploadFinished();
                                    if ((j + 1) == file.size()) {
                                        // [START_EXCLUDE]

                                        showUploadFinishedNotification(true);
                                        taskCompleted();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    broadcastUploadFinished();
                                    showUploadFinishedNotification(false);
                                    taskCompleted();
                                }
                            });
                            // Upload succeeded


                            // [END_EXCLUDE]
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Upload failed
                            Log.w(TAG, "uploadFromUri:onFailure", exception);

                            //[START_EXCLUDE]
                            broadcastUploadFinished();
                            showUploadFinishedNotification(false);
                            taskCompleted();
                            // [END_EXCLUDE]
                        }
                    });
        }
    }

    /**
     * Broadcast finished upload (success or failure).
     *
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished() {

        Intent broadcast = new Intent(UPLOAD_FINISHED);
        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    /**
     * Show a notification for a finished upload.
     */
    private void showUploadFinishedNotification(Boolean success) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        String caption = success ? getString(R.string.upload_success) : getString(R.string.upload_failure);
        showFinishedNotification(caption, intent, success);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
