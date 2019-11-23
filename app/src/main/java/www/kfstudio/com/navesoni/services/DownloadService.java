package www.kfstudio.com.navesoni.services;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import www.kfstudio.com.navesoni.MainActivity;
import www.kfstudio.com.navesoni.R;

/**
 * Service to handle downloading files from Firebase Storage.
 */
public class DownloadService extends BaseTaskService {

    /**
     * Actions
     **/
    public static final String ACTION_DOWNLOAD = "action_download";
    public static final String DOWNLOAD_COMPLETED = "download_completed";
    public static final String DOWNLOAD_ERROR = "download_error";
    /**
     * Extras
     **/
    public static final String EXTRA_DOWNLOAD_PATH = "extra_download_path";
    public static final String EXTRA_DOWNLOAD_FINISHED = "extra_download_finished";
    public static final String EXTRA_FILE_DOWNLOADED = "extra_file_downloaded";
    private static final String TAG = "Storage#DownloadService";
    private StorageReference mStorageRef;
    private FirebaseStorage storage;

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DOWNLOAD_COMPLETED);
        filter.addAction(DOWNLOAD_ERROR);

        return filter;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Storage
        storage = FirebaseStorage.getInstance();


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);

        if (ACTION_DOWNLOAD.equals(intent.getAction())) {
            // Get the path to download from the intent
            String downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH);
            String fileP = intent.getStringExtra(EXTRA_FILE_DOWNLOADED);
            mStorageRef = storage.getReferenceFromUrl(downloadPath);
            downloadFromPath(downloadPath, fileP);
        }

        return START_REDELIVER_INTENT;
    }

    /**
     * Broadcast finished download (success or failure).
     *
     * @return true if a running receiver received the broadcast.
     */

    private void downloadFromPath(final String downloadPath, final String filePath) {
        Log.d(TAG, "downloadFromPath:" + downloadPath);

        // Mark task started
        taskStarted();
        showDownloadingNotification(getString(R.string.progress_downloading), 0, 0);

        // Download and get total bytes
        File file = new File(filePath);
        mStorageRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                showDownloadFinishedNotification((int) taskSnapshot.getTotalByteCount());

                // Mark task completed
                taskCompleted();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.w(TAG, "download:FAILURE", exception);


                        showDownloadFinishedNotification(-1);

                        // Mark task completed
                        taskCompleted();
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                showDownloadingNotification("Downloading", taskSnapshot.getBytesTransferred(), taskSnapshot.getTotalByteCount());
            }
        });
    }

    /**
     * Show a notification for a finished download.
     */
    private void showDownloadFinishedNotification(int bytesDownloaded) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainActivity.class)
                .putExtra(EXTRA_DOWNLOAD_FINISHED, true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean success = bytesDownloaded != -1;
        String caption = success ? getString(R.string.download_success) : getString(R.string.download_failure);
        showFinishedNotification(caption, intent, success);
    }
}