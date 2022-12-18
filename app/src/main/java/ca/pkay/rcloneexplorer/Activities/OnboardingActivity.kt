package ca.pkay.rcloneexplorer.Activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import ca.pkay.rcloneexplorer.RuntimeConfiguration
import ca.pkay.rcloneexplorer.BuildConfig
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.util.ActivityHelper
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import es.dmoral.toasty.Toasty

class OnboardingActivity : AppIntro2() {

    companion object {
        private const val TAG = "OnboardingActivity"
        private const val REQ_ALL_FILES_ACCESS = 3101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // You can customize your parallax parameters in the constructors.
        setTransformer(
            AppIntroPageTransformerType.Parallax(
                titleParallaxFactor = 1.0,
                imageParallaxFactor = -1.0,
                descriptionParallaxFactor = 2.0
            ))

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.intro_welcome_title),
                description = getString(R.string.intro_welcome_description),
                imageDrawable = R.drawable.ic_dino2,
                backgroundColor = resources.getColor(R.color.seed),
        ))

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.intro_community_title),
                description = getString(R.string.intro_community_description),
                imageDrawable = R.drawable.ic_heart_red_24dp,
                backgroundColor = resources.getColor(R.color.md_theme_light_tertiary),

            ))

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.intro_storage_title),
                description = getString(R.string.intro_storage_description),
                imageDrawable = R.drawable.ic_intro_storage,
                backgroundColor = resources.getColor(R.color.seed),
            ))
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(RuntimeConfiguration.attach(this, newBase))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ALL_FILES_ACCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                Toasty.success(
                    this,
                    getString(R.string.intro_successful_setup),
                    Toast.LENGTH_LONG,
                    true
                ).show()
            } else {
                Toasty.info(this, getString(R.string.intro_unsuccessful_setup), Toast.LENGTH_LONG, true).show()
            }
            finish()
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        tryGrantingAllStorageAccess()
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean(getString(R.string.pref_key_intro_v1_12_0), true)
            .apply()
    }

    private fun tryGrantingAllStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()){
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.fromParts(
                    "package",
                    BuildConfig.APPLICATION_ID,
                    null
                )
                ActivityHelper.tryStartActivityForResult(this, intent, REQ_ALL_FILES_ACCESS)
            } else {
                finish()
            }
        } else {
            finish()
        }
    }
}