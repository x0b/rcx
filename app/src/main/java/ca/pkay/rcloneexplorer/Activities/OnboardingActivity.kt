package ca.pkay.rcloneexplorer.Activities

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import ca.pkay.rcloneexplorer.BuildConfig
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.RuntimeConfiguration
import ca.pkay.rcloneexplorer.util.ActivityHelper
import ca.pkay.rcloneexplorer.util.PermissionManager
import ca.pkay.rcloneexplorer.util.PermissionManager.Companion.PERM_ALARMS
import ca.pkay.rcloneexplorer.util.PermissionManager.Companion.PERM_NOTIFICATIONS
import ca.pkay.rcloneexplorer.util.PermissionManager.Companion.PERM_STORAGE
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import es.dmoral.toasty.Toasty


class OnboardingActivity : AppIntro2() {

    companion object {
        private const val REQ_ALL_FILES_ACCESS = 3101

        private const val intro_v1_12_0_completed = "intro_v1_12_0_completed";
    }


    private lateinit var mPreferences: SharedPreferences
    private lateinit var mPermissions: PermissionManager

    private var isStorageSlide = false
    private var isAlarmSlide = false
    private var storageRequested = false
    private var storageGranted = false
    private var alarmRequested = false
    private var alarmGranted = false

    private var color = R.color.intro_color1


    private var welcomeSlide = 0
    private var permissionChangedSlide = 0
    private var communitySlide = 0
    private var storageSlide = 0
    private var notificationSlide = 0
    private var alarmSlide = 0
    private var successSlide = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setImmersiveMode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mPermissions = PermissionManager(this)
        isWizardMode = true
        isColorTransitionsEnabled = true

        // dont allow the intro to be bypassed
        isSystemBackButtonLocked = true


        var maxSlideId = 1

        if (!mPreferences.getBoolean(intro_v1_12_0_completed, false)) {
            addSlide(
                AppIntroFragment.newInstance(
                    title = getString(R.string.intro_welcome_title),
                    description = getString(R.string.intro_welcome_description),
                    imageDrawable = R.drawable.undraw_hello,
                    backgroundColor = resources.getColor(color),
                ))
            switchColor()
            welcomeSlide = maxSlideId
            maxSlideId++

            addSlide(
                AppIntroFragment.newInstance(
                    title = getString(R.string.intro_community_title),
                    description = getString(R.string.intro_community_description),
                    imageDrawable = R.drawable.undraw_the_world_is_mine,
                    backgroundColor = resources.getColor(color)
                    ))
            switchColor()
            communitySlide = maxSlideId
            maxSlideId++
        } else {
            addSlide(
                AppIntroFragment.newInstance(
                    title = getString(R.string.intro_permission_changed_title),
                    description = getString(R.string.intro_permission_changed_description),
                    imageDrawable = R.drawable.undraw_completion,
                    backgroundColor = resources.getColor(color)
                    ))
            switchColor()
            permissionChangedSlide = maxSlideId
            maxSlideId++
        }


        if(!mPermissions.checkGenericPermission(PERM_STORAGE)) {
            addSlide(
                AppIntroFragment.newInstance(
                    title = getString(R.string.intro_storage_title),
                    description = getString(R.string.intro_storage_description),
                    imageDrawable = R.drawable.ic_intro_storage,
                    backgroundColor = resources.getColor(color),
                ))
            switchColor()
            storageSlide = maxSlideId
            maxSlideId++
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(!mPermissions.checkGenericPermission(PERM_NOTIFICATIONS)) {
                addSlide(
                    AppIntroFragment.newInstance(
                        title = getString(R.string.intro_notifications_title),
                        description = getString(R.string.intro_notifications_description),
                        imageDrawable = R.drawable.undraw_post_online,
                        backgroundColor = resources.getColor(color),
                    ))
                switchColor()
                notificationSlide = maxSlideId
                maxSlideId++
                askForPermissions(
                    permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    slideNumber = notificationSlide,
                    required = false)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(!mPermissions.checkGenericPermission(PermissionManager.PERM_ALARMS)) {
                addSlide(
                    AppIntroFragment.newInstance(
                        title = getString(R.string.intro_alarms_title),
                        description = getString(R.string.intro_alarms_description),
                        imageDrawable = R.drawable.undraw_time_management,
                        backgroundColor = resources.getColor(color),
                    ))
                switchColor()
                alarmSlide = maxSlideId
                maxSlideId++
            }
        }

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.intro_success),
                description = getString(R.string.intro_successful_setup),
                imageDrawable = R.drawable.undraw_sync,
                backgroundColor = resources.getColor(color),
            ))
        switchColor()
        successSlide = maxSlideId
        maxSlideId++
    }

    override fun onPageSelected(position: Int) {
        if(storageSlide != 0) {
            isStorageSlide = position+1 == storageSlide
        } else {
            isStorageSlide = false
        }
        if(alarmSlide != 0) {
            isAlarmSlide = position+1 == alarmSlide
        } else {
            isAlarmSlide = false
        }
    }

    override fun onCanRequestNextPage(): Boolean {

        if(isStorageSlide && storageGranted) {
            return true
        }

        if (isAlarmSlide && alarmGranted) {
            return true
        }

        if(isStorageSlide) {
            if(mPermissions.checkGenericPermission(PERM_STORAGE)){
                storageGranted = true
                return true
            }
            requestStoragePermission()
        }

        if(isAlarmSlide) {
            if(mPermissions.checkGenericPermission(PERM_ALARMS)){
                alarmGranted = true
                return true
            }
            requestAlarmPermission()
        }

        return super.onCanRequestNextPage()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(RuntimeConfiguration.attach(this, newBase))
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean(intro_v1_12_0_completed, true)
            .apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun requestStoragePermission() {
        if(storageRequested) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.fromParts(
                "package",
                BuildConfig.APPLICATION_ID,
                null
            )
            ActivityHelper.tryStartActivityForResult(this, intent, REQ_ALL_FILES_ACCESS)
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQ_ALL_FILES_ACCESS
            )
        }
        storageRequested = true
    }

    private fun requestAlarmPermission(){
        if (alarmRequested) {
            return
        }
        startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        alarmRequested = true
    }

    override fun onResume() {
        super.onResume()

        if(isStorageSlide) {
            if(mPermissions.checkGenericPermission(PERM_STORAGE)){
                storageGranted = true
                goToNextSlide()
            } else {
                denied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                storageRequested = false
            }
        }

        if(isAlarmSlide) {
            if(mPermissions.checkGenericPermission(PERM_ALARMS)){
                alarmGranted = true
                goToNextSlide()
            } else {
                denied(Manifest.permission.SCHEDULE_EXACT_ALARM)
                alarmRequested = false
            }
        }
    }

    override fun onUserDeniedPermission(permissionName: String) {
        super.onUserDeniedPermission(permissionName)
        denied(permissionName)
    }

    override fun onUserDisabledPermission(permissionName: String) {
        super.onUserDisabledPermission(permissionName)
        denied(permissionName)
    }
    private fun denied(permissionName: String){
        if(permissionName == Manifest.permission.POST_NOTIFICATIONS) {
            Toasty.info(this, getString(R.string.intro_notifications_denied), Toast.LENGTH_SHORT, true).show()
        }
        if(permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            Toasty.info(this, getString(R.string.intro_write_external_storage_denied), Toast.LENGTH_LONG, true).show()
        }
        //if(permissionName == Manifest.permission.SCHEDULE_EXACT_ALARM) {
        //    Toasty.info(this, getString(R.string.intro_alarms_denied), Toast.LENGTH_SHORT, true).show()
        //}
    }

    private fun switchColor() {
        if(color == R.color.intro_color1) {
            color = R.color.intro_color2
        } else {
            color = R.color.intro_color1
        }
    }
}