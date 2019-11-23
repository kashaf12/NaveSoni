package www.kfstudio.com.navesoni.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Uploadmodel implements Parcelable {

    public static final Creator<Uploadmodel> CREATOR = new Creator<Uploadmodel>() {
        @Override
        public Uploadmodel createFromParcel(Parcel in) {
            return new Uploadmodel(in);
        }

        @Override
        public Uploadmodel[] newArray(int size) {
            return new Uploadmodel[size];
        }
    };
    private Uri imageUri;
    private String category;

    public Uploadmodel(Uri imageUri, String category) {
        this.imageUri = imageUri;
        this.category = category;
    }

    protected Uploadmodel(Parcel in) {
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        category = in.readString();
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(imageUri, flags);
        dest.writeString(category);
    }
}
