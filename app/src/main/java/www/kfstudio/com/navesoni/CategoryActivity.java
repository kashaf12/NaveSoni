package www.kfstudio.com.navesoni;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
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
import www.kfstudio.com.navesoni.utility.helper;

public class CategoryActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {

    public static final String TITLE_WEDDING = "wedding";
    public static final String TITLE_RECEPTION = "reception";
    public static final String TITLE_MEHNDI = "mehndi";
    public static final String TITLE_ENGAGEMENT = "engagement";
    public static final String TITLE = "title";
    public static final String TITLE_HONEYMOON = "honeymoon";
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Dialog dialog;
    private AppCompatImageView imageView;
    private TextView textView;
    private String title;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private Boolean isAuthenticated = false;
    private CollectionReference dbr = db.collection("Images");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        imageView = findViewById(R.id.categoryImage);
        textView = findViewById(R.id.categorytxt);
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString(TITLE);
        textView.setText(title);
        switch (title) {

            case TITLE_ENGAGEMENT:
                imageView.setImageDrawable(getDrawable(R.drawable.engagement));
                break;
            case TITLE_WEDDING:
                imageView.setImageDrawable(getDrawable(R.drawable.wedding));
                break;
            case TITLE_MEHNDI:
                imageView.setImageDrawable(getDrawable(R.drawable.mehndi));
                break;
            case TITLE_RECEPTION:
                imageView.setImageDrawable(getDrawable(R.drawable.reception));
                break;
            case TITLE_HONEYMOON:
                imageView.setImageDrawable(getDrawable(R.drawable.honeymoon));
                break;
            default:
                imageView.setImageDrawable(getDrawable(R.drawable.wedding));
        }
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = dbr.orderBy("time", Query.Direction.DESCENDING).whereEqualTo("category", title);
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
    public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
        showDialogForImage(documentSnapshot, position);
    }

    private void showDialogForImage(final DocumentSnapshot documentSnapshot, final int position) {

        dialog = new Dialog(CategoryActivity.this);
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
                    Toast.makeText(CategoryActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(CategoryActivity.this, "Already Downloaded", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CategoryActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                }


            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        imageAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
        imageAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAuthenticated = sharedpreferences.getBoolean("isAuthenticated", false);
        imageAdapter.startListening();
    }
}
