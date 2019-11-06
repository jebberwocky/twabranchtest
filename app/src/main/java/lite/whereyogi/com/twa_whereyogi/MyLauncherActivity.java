package lite.whereyogi.com.twa_whereyogi;

import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import android.util.Log;


import android.os.Bundle;
import android.net.Uri;

import android.support.customtabs.trusted.TrustedWebActivityIntentBuilder;

import android.support.customtabs.trusted.LauncherActivityMetadata;
import android.support.annotation.Nullable;
import android.support.customtabs.trusted.TwaLauncher;
import android.support.customtabs.TrustedWebUtils;
import android.support.customtabs.trusted.splashscreens.PwaWrapperSplashScreenStrategy;
import android.support.v4.content.ContextCompat;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;


public class MyLauncherActivity extends AppCompatActivity {


    private static boolean sChromeVersionChecked;
    private LauncherActivityMetadata mMetadata;
    private boolean mBrowserWasLaunched;
    @Nullable
    private PwaWrapperSplashScreenStrategy mSplashScreenStrategy;
    @Nullable
    private TwaLauncher mTwaLauncher;
    private static final String BROWSER_WAS_LAUNCHED_KEY =
            "android.support.customtabs.trusted.BROWSER_WAS_LAUNCHED_KEY";


    private boolean splashScreenNeeded() {
        // Splash screen was not requested.
        if (mMetadata.splashImageDrawableId == 0) return false;
        // If this activity isn't task root, then a TWA is already running in this task. This can
        // happen if a VIEW intent (without Intent.FLAG_ACTIVITY_NEW_TASK) is being handled after
        // launching a TWA. In that case we're only passing a new intent into existing TWA, and
        // don't show the splash screen.
        return isTaskRoot();
    }

    private int getColorCompat(int splashScreenBackgroundColorId) {
        return ContextCompat.getColor(this, splashScreenBackgroundColorId);
    }

    @NonNull
    protected ImageView.ScaleType getSplashImageScaleType() {
        return ImageView.ScaleType.CENTER;
    }
    /**
     * Override to set a transformation matrix for the image displayed on a splash screen.
     * See {@link ImageView#setImageMatrix}.
     * Has any effect only if {@link #getSplashImageScaleType()} returns {@link
     * ImageView.ScaleType#MATRIX}.
     */
    @Nullable
    protected Matrix getSplashImageTransformationMatrix() {
        return null;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mBrowserWasLaunched) {
            finish(); // The user closed the Trusted Web Activity and ended up here.
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTwaLauncher != null) {
            mTwaLauncher.destroy();
        }
        if (mSplashScreenStrategy != null) {
            mSplashScreenStrategy.destroy();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BROWSER_WAS_LAUNCHED_KEY, mBrowserWasLaunched);
    }
    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (mSplashScreenStrategy != null) {
            mSplashScreenStrategy.onActivityEnterAnimationComplete();
        }
    }

    protected Uri getLaunchingUrl() {
        Uri uri = getIntent().getData();
        if (uri != null) {
            return uri;
        }
        if (mMetadata.defaultUrl != null) {
            return Uri.parse(mMetadata.defaultUrl);
        }
        return Uri.parse("https://www.example.com/");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getBoolean(BROWSER_WAS_LAUNCHED_KEY)) {
            // This activity died in the background after launching Trusted Web Activity, then
            // the user closed the Trusted Web Activity and ended up here.
            finish();
            return;
        }
        mMetadata = LauncherActivityMetadata.parse(this);
        if (splashScreenNeeded()) {
            mSplashScreenStrategy = new PwaWrapperSplashScreenStrategy(this,
                    mMetadata.splashImageDrawableId,
                    getColorCompat(mMetadata.splashScreenBackgroundColorId),
                    getSplashImageScaleType(),
                    getSplashImageTransformationMatrix(),
                    mMetadata.splashScreenFadeOutDurationMillis,
                    mMetadata.fileProviderAuthority);
        }

        //appending query
        String _url= getLaunchingUrl().toString()+"?branch_param=jeff";
        TrustedWebActivityIntentBuilder twaBuilder =
                new TrustedWebActivityIntentBuilder(Uri.parse(_url))
                        .setToolbarColor(getColorCompat(mMetadata.statusBarColorId))
                        .setNavigationBarColor(getColorCompat(mMetadata.navigationBarColorId));
        mTwaLauncher = new TwaLauncher(this);
        mTwaLauncher.launch(twaBuilder, mSplashScreenStrategy, () -> mBrowserWasLaunched = true);
        if (!sChromeVersionChecked) {
            TrustedWebUtils.promptForChromeUpdateIfNeeded(this, mTwaLauncher.getProviderPackage());
            sChromeVersionChecked = true;
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        // Branch init
        Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    Log.i("BRANCH SDK", referringParams.toString());
                    // Retrieve deeplink keys from 'referringParams' and evaluate the values to determine where to route the user
                    // Check '+clicked_branch_link' before deciding whether to use your Branch routing logic
                } else {
                    Log.i("BRANCH SDK", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
    }

}
