package ca.pkay.rcloneexplorer.RemoteConfig

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.RemoteConfig.ProviderListFragment.Companion.newProviderListConfig
import ca.pkay.rcloneexplorer.RemoteConfig.ProviderListFragment.ProviderSelectedListener
import ca.pkay.rcloneexplorer.RuntimeConfiguration
import ca.pkay.rcloneexplorer.rclone.Provider
import ca.pkay.rcloneexplorer.util.ActivityHelper
import es.dmoral.toasty.Toasty
import org.json.JSONException

class RemoteConfig : AppCompatActivity(), ProviderSelectedListener {

    private val OUTSTATE_TITLE = "ca.pkay.rcexplorer.remoteConfig.TITLE"
    private var mFragment: Fragment? = null

    companion object {
        const val CONFIG_EDIT_CODE = 139
        const val CONFIG_EDIT_TARGET = "CONFIG_EDIT_TARGET"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(RuntimeConfiguration.attach(this, newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_remote_config)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (savedInstanceState != null) {
            mFragment = supportFragmentManager.findFragmentByTag("new config")
            fragmentTransaction.replace(R.id.flFragment, getCurrentFragment(), "new config")
            fragmentTransaction.commit()
            val title = savedInstanceState.getString(OUTSTATE_TITLE)
            if (title != null) {
                supportActionBar!!.setTitle(title)
            }
            return
        }

        mFragment = newProviderListConfig()
        fragmentTransaction.replace(R.id.flFragment, getCurrentFragment(), "config list")
        fragmentTransaction.commit()
        val shouldEdit = intent.getStringExtra(CONFIG_EDIT_TARGET)
        if (shouldEdit != null) {
            if (!shouldEdit.isEmpty()) {
                val rclone = Rclone(this)
                val config = rclone.getConfig(intent.getStringExtra(CONFIG_EDIT_TARGET))
                if (config != null) {
                    try {
                        val provider = rclone.getProvider(config["type"])
                        mFragment = DynamicRemoteConfigFragment(provider.name, config)
                        startConfig(provider)
                    } catch (e: JSONException) {
                        throw RuntimeException(e)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (supportActionBar != null) {
            val title = supportActionBar!!.title
            if (title != null) {
                outState.putString(OUTSTATE_TITLE, title.toString())
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        handleBackAction()
        return true
    }

    override fun onBackPressed() {
        handleBackAction()
    }

    private fun handleBackAction() {
        if (mFragment is ProviderListFragment) {
            finish()
        } else if (mFragment is DynamicRemoteConfigFragment) {
            if ((mFragment as DynamicRemoteConfigFragment).isEditConfig()) {
                finish()
            } else {
                startProviderlist()
            }
        } else {
            startProviderlist()
        }
    }

    override fun onProviderSelected(provider: Provider) {
        if (provider == null) {
            Toasty.error(this, getString(R.string.nothing_selected), Toast.LENGTH_SHORT, true)
                .show()
            return
        }
        mFragment = when (provider.name) {
            "box", "dropbox", "pcloud", "yandex", "drive", "google photos" -> DynamicRemoteConfigFragment(
                provider.name,
                true
            )

            else -> DynamicRemoteConfigFragment(provider.name)
        }
        startConfig(provider)
    }

    private fun applyTheme() {
        ActivityHelper.applyTheme(this)
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
        window.statusBarColor = typedValue.data
    }

    private fun startConfig(provider: Provider) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, getCurrentFragment(), "new config")
        transaction.commit()
        if (supportActionBar != null) {
            supportActionBar!!.title = provider.getNameCapitalized()
        }
    }

    private fun startProviderlist() {
        mFragment = newProviderListConfig()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.flFragment, getCurrentFragment(), "config list")
        fragmentTransaction.commit()
        if (supportActionBar != null) {
            supportActionBar!!.setTitle(R.string.title_activity_remote_config)
        }
    }

    /**
     * This defauls to the provider list
     */
    private fun getCurrentFragment(): Fragment {
        if(mFragment == null) {
            return newProviderListConfig()
        }
        return mFragment!!
    }
}