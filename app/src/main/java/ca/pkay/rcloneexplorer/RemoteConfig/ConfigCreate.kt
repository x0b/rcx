package ca.pkay.rcloneexplorer.RemoteConfig

import android.annotation.SuppressLint
import android.content.Context
import ca.pkay.rcloneexplorer.Rclone
import android.os.AsyncTask
import es.dmoral.toasty.Toasty
import ca.pkay.rcloneexplorer.R
import android.widget.Toast
import android.content.Intent
import android.view.View
import ca.pkay.rcloneexplorer.Activities.MainActivity
import java.util.ArrayList

@SuppressLint("StaticFieldLeak")
class ConfigCreate internal constructor(
    options: ArrayList<String>?,
    formView: View,
    authView: View,
    context: Context,
    rclone: Rclone
) : AsyncTask<Void?, Void?, Boolean>() {
    private val options: ArrayList<String>
    private val process: Process? = null
    private val mContext: Context
    private val mRclone: Rclone
    private val mFormView: View
    private val mAuthView: View

    init {
        this.options = ArrayList(options)
        mFormView = formView
        mAuthView = authView
        mContext = context
        mRclone = rclone
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mAuthView.visibility = View.VISIBLE
        mFormView.visibility = View.GONE
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        return OauthHelper.createOptionsWithOauth(options, mRclone, mContext)
    }

    override fun onCancelled() {
        super.onCancelled()
        process?.destroy()
    }

    override fun onPostExecute(success: Boolean) {
        super.onPostExecute(success)
        if (!success) {
            Toasty.error(
                mContext,
                mContext.getString(R.string.error_creating_remote),
                Toast.LENGTH_SHORT,
                true
            ).show()
        } else {
            Toasty.success(
                mContext,
                mContext.getString(R.string.remote_creation_success),
                Toast.LENGTH_SHORT,
                true
            ).show()
        }
        val intent = Intent(mContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext.startActivity(intent)
    }
}