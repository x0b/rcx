package ca.pkay.rcloneexplorer.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import ca.pkay.rcloneexplorer.BuildConfig
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.RuntimeConfiguration
import ca.pkay.rcloneexplorer.util.ActivityHelper
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import es.dmoral.toasty.Toasty


class OnboardingActivity : AppIntro2() {

    companion object {
        private const val REQ_ALL_FILES_ACCESS = 3101
    }

    private var isStorageSlide = false
    private var storageRequested = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setImmersiveMode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        isWizardMode = true
        isColorTransitionsEnabled = true

        // dont allow the intro to be bypassed
        isSystemBackButtonLocked = true


        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.intro_welcome_title),
                description = getString(R.string.intro_welcome_description),
                imageDrawable = R.drawable.undraw_hello,
                backgroundColor = resources.getColor(R.color.intro_color1),
        ))

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.intro_community_title),
                description = getString(R.string.intro_community_description),
                imageDrawable = R.drawable.undraw_the_world_is_mine,
                backgroundColor = resources.getColor(R.color.intro_color2),

            ))

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.intro_storage_title),
                description = getString(R.string.intro_storage_description),
                imageDrawable = R.drawable.ic_intro_storage,
                backgroundColor = resources.getColor(R.color.intro_color1),
            ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            addSlide(
                AppIntroFragment.newInstance(
                    title = getString(R.string.intro_notifications_title),
                    description = getString(R.string.intro_notifications_description),
                    imageDrawable = R.drawable.undraw_post_online,
                    backgroundColor = resources.getColor(R.color.intro_color2),
                ))

            askForPermissions(
                permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                slideNumber = 4,
                required = false)
        }

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.intro_success),
                description = getString(R.string.intro_successful_setup),
                imageDrawable = R.drawable.undraw_sync,
                backgroundColor = resources.getColor(R.color.intro_color1),
            ))
    }

    override fun onPageSelected(position: Int) {
        isStorageSlide = position == 2
    }

    override fun onCanRequestNextPage(): Boolean {
        if(!isStorageSlide) {
            return super.onCanRequestNextPage()
        }

        if(storageRequested) {
            return true
        }

        if(checkExternalStorageManagerPermission()){
            storageRequested = true
            return true
        }

        tryGrantingAllStorageAccess()
        return false
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(RuntimeConfiguration.attach(this, newBase))
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean(getString(R.string.pref_key_intro_v1_12_0), true)
            .apply()
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleStorageRequestResult(requestCode)

    }

    fun handleStorageRequestResult(requestCode: Int) {
        if (requestCode == REQ_ALL_FILES_ACCESS) {
            if(checkExternalStorageManagerPermission()){
                Toasty.success(this, getString(R.string.intro_manage_external_storage_granted), Toast.LENGTH_SHORT, true).show()
                goToNextSlide()
            } else {
                Toasty.info(this, getString(R.string.intro_manage_external_storage_failed), Toast.LENGTH_LONG, true).show()
            }
            storageRequested = true
        }
    }


    private fun tryGrantingAllStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.fromParts(
                "package",
                BuildConfig.APPLICATION_ID,
                null
            )
            ActivityHelper.tryStartActivityForResult(this, intent,
                REQ_ALL_FILES_ACCESS
            )
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQ_ALL_FILES_ACCESS
            )
        }
    }

    private fun checkExternalStorageManagerPermission(): Boolean  {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
}