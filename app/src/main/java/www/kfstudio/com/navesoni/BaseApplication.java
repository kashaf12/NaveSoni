package www.kfstudio.com.navesoni;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
