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
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import es.dmoral.toasty.Toasty


class OnboardingActivity : AppIntro2() {

    companion object {
        private const val REQ_ALL_FILES_ACCESS = 3101

        private const val intro_v1_12_0_completed = "intro_v1_12_0_completed";
        public fun hasAllRequiredPermissions(context: Context): Boolean {
            if(!checkGenericPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                return false
            }
            if(!checkGenericPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                return false
            }
            return true
        }

        private fun checkGenericPermission(context: Context, permission: String): Boolean {
            if(permission == Manifest.permission.SCHEDULE_EXACT_ALARM) {
                val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    false
                }

            } else {
                return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }


    private lateinit var mPreferences: SharedPreferences;

    private var isStorageSlide = false
    private var isAlarmSlide = false
    private var storageRequested = false
    private var alarmRequested = false

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
                    imageDrawable = R.drawable.undraw_the_world_is_mine,
                    backgroundColor = resources.getColor(color)
                    ))
            switchColor()
            permissionChangedSlide = maxSlideId
            maxSlideId++
        }


        if(!checkExternalStorageManagerPermission()) {
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
            if(!checkGenericPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
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
            if(!checkGenericPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                addSlide(
                    AppIntroFragment.newInstance(
                        title = getString(R.string.intro_alarms_title),
                        description = getString(R.string.intro_alarms_description),
                        imageDrawable = R.drawable.undraw_post_online,
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
        if(!isStorageSlide && !isAlarmSlide) {
            return super.onCanRequestNextPage()
        }

        if(storageRequested) {
            return true
        }

        if (alarmRequested) {
            return true
        }

        if(isStorageSlide) {
            if(checkExternalStorageManagerPermission()){
                storageRequested = true
                return true
            }
            tryGrantingAllStorageAccess()
        }

        if(isAlarmSlide) {
            if(checkGenericPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)){
                alarmRequested = true
                return true
            }
            requestAlarmPermission()
        }
        return false
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(RuntimeConfiguration.attach(this, newBase))
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean(intro_v1_12_0_completed, true)
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

    private fun requestAlarmPermission(){
        startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
    }

    private fun switchColor() {
        if(color == R.color.intro_color1) {
            color = R.color.intro_color2
        } else {
            color = R.color.intro_color1
        }
    }
}