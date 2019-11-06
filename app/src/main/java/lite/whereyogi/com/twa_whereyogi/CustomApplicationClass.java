package lite.whereyogi.com.twa_whereyogi;

import android.app.Application;
import io.branch.referral.Branch;

public class CustomApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Branch logging for debugging
        Branch.enableDebugMode();

        // Branch object initialization
        Branch.getAutoInstance(this);
    }
}
