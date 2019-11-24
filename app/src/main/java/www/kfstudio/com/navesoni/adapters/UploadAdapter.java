package www.kfstudio.com.navesoni.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import www.kfstudio.com.navesoni.R;
import www.kfstudio.com.navesoni.models.Image;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Image> details;
    private int size;
    private FilesClickListener filesClickListener;


    public UploadAdapter(ArrayList<Image> fileList, Context mContext) {
        this.details = fileList;
        this.mContext = mContext;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_image_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Uri file = details.get(position).getFile();
        final Boolean isCheck = details.get(position).getChecked();
        final int fposition = position;

        Glide.with(mContext)
                .load(file)
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                .into(holder.imageView);

        if (isCheck) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }


        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                details.get(position).setChecked(isChecked);

                if (filesClickListener != null)
                    filesClickListener.onfileClick(details);
            }
        });

        // Spinner click listener
        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();

                details.get(fposition).setCategory(item);
                if (filesClickListener != null)
                    filesClickListener.onfileClick(details);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(0);
            }
        });


    }

    @Override
    public int getItemCount() {
        this.size = details.size();
        return size;
    }

    public void setCheckedListener(FilesClickListener filesClickListener) {
        this.filesClickListener = filesClickListener;
    }


    public interface FilesClickListener {
        void onfileClick(ArrayList<Image> files);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private Spinner spinner;
        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            spinner = itemView.findViewById(R.id.spinner);
            checkBox = itemView.findViewById(R.id.checkbox);

            // Spinner Drop down elements
            List<String> categories = new ArrayList<String>();
            categories.add("wedding");
            categories.add("mehndi");
            categories.add("reception");
            categories.add("naveen");
            categories.add("soni");
            categories.add("engagement");
            categories.add("honeymoon");

            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, categories);

            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // attaching data adapter to spinner
            spinner.setAdapter(dataAdapter);


        }
    }
}
