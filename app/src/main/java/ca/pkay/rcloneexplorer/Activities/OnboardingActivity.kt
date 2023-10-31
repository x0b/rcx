package ca.pkay.rcloneexplorer.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.RuntimeConfiguration
import ca.pkay.rcloneexplorer.util.PermissionManager
import com.github.appintro.AppIntro2
import de.felixnuesse.extract.onboarding.IdentifiableAppIntroFragment
import es.dmoral.toasty.Toasty


class OnboardingActivity : AppIntro2() {

    companion object {
        private const val intro_v1_12_0_completed = "intro_v1_12_0_completed2"

        private const val SLIDE_ID_WELCOME = "SLIDE_ID_WELCOME"
        private const val SLIDE_ID_COMMUNITY = "SLIDE_ID_COMMUNITY"
        private const val SLIDE_ID_PERMCHANGE = "SLIDE_ID_PERMCHANGE"
        private const val SLIDE_ID_STORAGE = "SLIDE_ID_STORAGE"
        private const val SLIDE_ID_NOTIFICATIONS = "SLIDE_ID_NOTIFICATIONS"
        private const val SLIDE_ID_ALARMS = "SLIDE_ID_ALARMS"
        private const val SLIDE_ID_SUCCESS = "SLIDE_ID_SUCCESS"
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
            WindowCompat.setDecorFitsSystemWindows(window, false)
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
                IdentifiableAppIntroFragment.createInstance(
                    title = getString(R.string.intro_welcome_title),
                    description = getString(R.string.intro_welcome_description),
                    imageDrawable = R.drawable.undraw_hello,
                    backgroundColorRes = color,
                    id = SLIDE_ID_WELCOME
                ))
            switchColor()
            welcomeSlide = maxSlideId
            maxSlideId++

            addSlide(
                IdentifiableAppIntroFragment.createInstance(
                    title = getString(R.string.intro_community_title),
                    description = getString(R.string.intro_community_description),
                    imageDrawable = R.drawable.undraw_the_world_is_mine,
                    backgroundColorRes = color,
                    id = SLIDE_ID_COMMUNITY
                    ))
            switchColor()
            communitySlide = maxSlideId
            maxSlideId++
        } else {
            addSlide(
                IdentifiableAppIntroFragment.createInstance(
                    title = getString(R.string.intro_permission_changed_title),
                    description = getString(R.string.intro_permission_changed_description),
                    imageDrawable = R.drawable.undraw_completion,
                    backgroundColorRes = color,
                    id = SLIDE_ID_PERMCHANGE
                    ))
            switchColor()
            permissionChangedSlide = maxSlideId
            maxSlideId++
        }


        if(!mPermissions.grantedStorage()) {
            addSlide(
                IdentifiableAppIntroFragment.createInstance(
                    title = getString(R.string.intro_storage_title),
                    description = getString(R.string.intro_storage_description),
                    imageDrawable = R.drawable.ic_intro_storage,
                    backgroundColorRes = color,
                    id = SLIDE_ID_STORAGE
                ))
            switchColor()
            storageSlide = maxSlideId
            maxSlideId++
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(!mPermissions.grantedNotifications()) {
                addSlide(
                    IdentifiableAppIntroFragment.createInstance(
                        title = getString(R.string.intro_notifications_title),
                        description = getString(R.string.intro_notifications_description),
                        imageDrawable = R.drawable.undraw_post_online,
                        backgroundColorRes = color,
                        id = SLIDE_ID_NOTIFICATIONS
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
            if(!mPermissions.grantedAlarms()) {
                addSlide(
                    IdentifiableAppIntroFragment.createInstance(
                        title = getString(R.string.intro_alarms_title),
                        description = getString(R.string.intro_alarms_description),
                        imageDrawable = R.drawable.undraw_time_management,
                        backgroundColorRes = color,
                        id = SLIDE_ID_ALARMS
                    ))
                switchColor()
                alarmSlide = maxSlideId
                maxSlideId++
            }
        }

        addSlide(
            IdentifiableAppIntroFragment.createInstance(
                title = getString(R.string.intro_success),
                description = getString(R.string.intro_successful_setup),
                imageDrawable = R.drawable.undraw_sync,
                backgroundColorRes = color,
                id = SLIDE_ID_SUCCESS
            ))
        switchColor()
        successSlide = maxSlideId
        maxSlideId++
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)

        isStorageSlide = false
        isAlarmSlide = false

        if(isFragment(newFragment, SLIDE_ID_STORAGE)){
            isStorageSlide = true
        }

        if(isFragment(newFragment, SLIDE_ID_ALARMS)){
            isAlarmSlide = true
        }
    }

    private fun isFragment(newFragment: Fragment?, slideIdStorage: String): Boolean {
        if(newFragment is IdentifiableAppIntroFragment) {
            return newFragment.slideId == slideIdStorage
        }
        return false
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
            if(mPermissions.grantedStorage()){
                storageGranted = true
                return true
            }
            requestStoragePermission()
            // dont allow slide to continue, this is a hard requirement
            return false
        }

        if(isAlarmSlide) {
            if(mPermissions.grantedAlarms()){
                alarmGranted = true
                return true
            }
            requestAlarmPermission()
            return false
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
        mPermissions.requestStorage(this)
        storageRequested = true
    }

    private fun requestAlarmPermission(){
        if (alarmRequested) {
            return
        }
        mPermissions.requestAlarms()
        alarmRequested = true
    }

    override fun onResume() {
        super.onResume()

        if(isStorageSlide) {
            if(mPermissions.grantedStorage()){
                storageGranted = true
                goToNextSlide()
            } else {
                denied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                storageRequested = false
            }
        }

        if(isAlarmSlide) {
            if(mPermissions.grantedAlarms()){
                alarmGranted = true
                goToNextSlide()
            } else {
                denied(Manifest.permission.SCHEDULE_EXACT_ALARM)
                // allow slide to continue
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