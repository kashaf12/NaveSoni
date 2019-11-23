package www.kfstudio.com.navesoni.services;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import www.kfstudio.com.navesoni.MainActivity;
import www.kfstudio.com.navesoni.R;

public class DeleteService extends BaseTaskService {

    /**
     * Intent Actions
     **/
    public static final String ACTION_DELETE = "delete";
    public static final String DELETE_COMPLETED = "delete_completed";
    public static final String DELETE_ERROR = "delete_error";
    /**
     * Intent Extras
     **/
    public static final String EXTRA_FILE_NAME = "extra_file_name";
    public static final String EXTRA_DOCUMENT_NAME = "document_name";
    public static final String EXTRA_DOCUMENT_URL = "document_url";
    private static final String TAG = "DeleteService";
    private StorageReference mStorageRef;
    private FirebaseFirestore db;

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
        if (ACTION_DELETE.equals(intent.getAction())) {
            String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
            String docName = intent.getStringExtra(EXTRA_DOCUMENT_NAME);
            String downloadUrl = intent.getStringExtra(EXTRA_DOCUMENT_URL);
//            ArrayList<Uploadmodel> fileUri1 =intent.getParcelableArrayListExtra(EXTRA_ARRAY_FILE_URI);
//            for(int i=0;i<fileUri1.size();i++) {
//                getContentResolver().takePersistableUriPermission(
//                        fileUri1.get(i).getImageUri(),
//                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            }
            deleteFromUri(fileName, docName, downloadUrl);

        }

        return START_REDELIVER_INTENT;
    }


    private void deleteFromUri(final String fileName, final String docName, final String docUrl) {
        taskStarted();


        db.collection("Images").document(docName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                final StorageReference photoRef = mStorageRef.child("photos").child(fileName);
                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mStorageRef.child("photos").child(docUrl).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                showUploadFinishedNotification(true);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showUploadFinishedNotification(false);
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showUploadFinishedNotification(false);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showUploadFinishedNotification(false);
            }
        });


    }

    private void showUploadFinishedNotification(Boolean success) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        String caption = success ? getString(R.string.delete_success) : getString(R.string.delete_success);
        showFinishedNotification(caption, intent, success);
    }
}
