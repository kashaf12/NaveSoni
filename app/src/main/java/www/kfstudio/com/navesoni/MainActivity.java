package www.kfstudio.com.navesoni;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;

import www.kfstudio.com.navesoni.adapters.ImageAdapter;
import www.kfstudio.com.navesoni.models.FileModal;
import www.kfstudio.com.navesoni.services.DeleteService;
import www.kfstudio.com.navesoni.services.DownloadService;
import www.kfstudio.com.navesoni.services.UploadService;
import www.kfstudio.com.navesoni.utility.helper;

public class MainActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String IsAuthenticated = "isAuthenticated";
    private static final String TAG = "MainActivity";
    static Boolean isAuthenticated = false;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Dialog dialog;
    private TextView login;
    private Dialog dl;
    private Button upload;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private CollectionReference dbr = db.collection("Images");
    private BroadcastReceiver mBroadcastReceiver;
    private CardView cv_wedding, cv_engagement, cv_mehndi, cv_soni, cv_naveen, cv_reception;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = findViewById(R.id.title);
        upload = findViewById(R.id.upload);
        cv_wedding = findViewById(R.id.weddingCardView);
        cv_engagement = findViewById(R.id.engagementCardView);
        cv_mehndi = findViewById(R.id.mehndiCardView);
        cv_soni = findViewById(R.id.soniCardView);
        cv_naveen = findViewById(R.id.naveenCardView);
        cv_reception = findViewById(R.id.receptionCardView);
        cv_wedding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                intent.putExtra(CategoryActivity.TITLE, CategoryActivity.TITLE_WEDDING);
                startActivity(intent);
            }
        });
        cv_reception.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                intent.putExtra(CategoryActivity.TITLE, CategoryActivity.TITLE_RECEPTION);
                startActivity(intent);
            }
        });
        cv_engagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                intent.putExtra(CategoryActivity.TITLE, CategoryActivity.TITLE_ENGAGEMENT);
                startActivity(intent);
            }
        });
        cv_mehndi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                intent.putExtra(CategoryActivity.TITLE, CategoryActivity.TITLE_MEHNDI);
                startActivity(intent);
            }
        });
        cv_naveen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Admin.class);
                intent.putExtra(Admin.TITLE, Admin.TITLE_NAVEEN);
                startActivity(intent);
            }
        });
        cv_soni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Admin.class);
                intent.putExtra(Admin.TITLE, Admin.TITLE_SONI);
                startActivity(intent);
            }
        });
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        editor.putBoolean("isAuthenticated", false);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);

                imageAdapter.notifyDataSetChanged();
            }
        };
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAuthenticated) {
                    ShowDialog();
                } else {
                    isAuthenticated = false;
                    editor.putBoolean(IsAuthenticated, false);
                    editor.apply();
                    upload.setVisibility(View.GONE);

                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = dbr.orderBy("time", Query.Direction.DESCENDING).limit(10);
        FirestoreRecyclerOptions<FileModal> options = new FirestoreRecyclerOptions.Builder<FileModal>()
                .setQuery(query, FileModal.class).build();
        imageAdapter = new ImageAdapter(options, this);
        recyclerView = findViewById(R.id.rv);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(imageAdapter);
        imageAdapter.setOnItemClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        isAuthenticated = sharedpreferences.getBoolean("isAuthenticated", false);
        imageAdapter.startListening();
    }

    public void ShowDialog() {
        dl = new Dialog(this, R.style.Theme_Dialog);
        dl.setContentView(R.layout.activity_login);

        final EditText password = dl.findViewById(R.id.password);
        Button btn = dl.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (password.getText().toString().equals("naveen@admin") || password.getText().toString().equals("soni@admin")) {
                    Toast.makeText(MainActivity.this, "Welcome" + password.getText().toString(), Toast.LENGTH_SHORT).show();
                    isAuthenticated = true;
                    editor.putBoolean(IsAuthenticated, true);
                    editor.apply();
                    upload.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "Wrong Input", Toast.LENGTH_SHORT).show();
                }
                dl.dismiss();
            }
        });
        dl.show();

    }

    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
        showDialogForImage(documentSnapshot, position);
    }

    @Override
    protected void onStart() {
        super.onStart();
        imageAdapter.startListening();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, UploadService.getIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();
        imageAdapter.stopListening();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        imageAdapter.stopListening();
    }

    private void showDialogForImage(final DocumentSnapshot documentSnapshot, final int position) {

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.image_show_layout);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ImageView image = dialog.findViewById(R.id.imageD);
        Button downlaod = dialog.findViewById(R.id.download);
        ImageView delete = dialog.findViewById(R.id.delete);
        Glide.with(getApplicationContext())
                .load(documentSnapshot.getString("imageUrl"))
                .fitCenter()
                .placeholder(R.drawable.progress_animation)
                .useAnimationPool(true)
                .into(image);
        isAuthenticated = sharedpreferences.getBoolean("isAuthenticated", false);
        if (isAuthenticated) {
            Log.d(TAG, "showDialogForImage: isAuthenticated" + isAuthenticated);
            delete.setVisibility(View.VISIBLE);
        } else {
            delete.setVisibility(View.GONE);
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helper.isNetworkAvailable(getApplicationContext())) {
                    imageAdapter.notifyItemRemoved(position);
                    startService(new Intent(getApplicationContext(), DeleteService.class)
                            .putExtra(DeleteService.EXTRA_FILE_NAME, documentSnapshot.getString("imageName"))
                            .putExtra(DeleteService.EXTRA_DOCUMENT_NAME, documentSnapshot.getId())
                            .putExtra(DeleteService.EXTRA_DOCUMENT_URL, documentSnapshot.getString("thumbnailName"))
                            .setAction(DeleteService.ACTION_DELETE));
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
        downlaod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helper.isNetworkAvailable(getApplicationContext())) {
                    File rootPath = new File(Environment.getExternalStorageDirectory(), "NaveSoni");
                    if (!rootPath.exists()) {
                        rootPath.mkdirs();
                    }

                    final File localFile = new File(rootPath, documentSnapshot.getString("imageName"));
                    if (localFile.exists()) {
                        Toast.makeText(MainActivity.this, "Already Downloaded", Toast.LENGTH_SHORT).show();
                    } else {
                        final String path = localFile.getAbsolutePath();

                        Intent intent = new Intent(getApplicationContext(), DownloadService.class)
                                .putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, documentSnapshot.getString("downloadUrl"))
                                .putExtra(DownloadService.EXTRA_FILE_DOWNLOADED, path)
                                .setAction(DownloadService.ACTION_DOWNLOAD);
                        startService(intent);
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                }


            }
        });

        dialog.show();
    }
}
