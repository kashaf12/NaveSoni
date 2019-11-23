package www.kfstudio.com.navesoni.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
    private Uri file;
    private Boolean isChecked;
    private String category;

    protected Image(Parcel in) {
        file = in.readParcelable(Uri.class.getClassLoader());
        byte tmpIsChecked = in.readByte();
        isChecked = tmpIsChecked == 0 ? null : tmpIsChecked == 1;
        category = in.readString();
    }

    public Image(Uri file, Boolean isChecked, String category) {
        this.file = file;
        this.isChecked = isChecked;
        this.category = category;
    }

    public Uri getFile() {
        return file;
    }

    public void setFile(Uri file) {
        this.file = file;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
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
        dest.writeParcelable(file, flags);
        dest.writeByte((byte) (isChecked == null ? 0 : isChecked ? 1 : 2));
        dest.writeString(category);
    }
}
