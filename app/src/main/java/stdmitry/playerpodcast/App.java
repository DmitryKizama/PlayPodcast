package stdmitry.playerpodcast;

import android.app.Application;

import com.activeandroid.ActiveAndroid;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }
}
