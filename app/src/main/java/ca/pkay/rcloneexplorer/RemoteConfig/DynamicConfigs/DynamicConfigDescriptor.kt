package ca.pkay.rcloneexplorer.RemoteConfig.DynamicConfigs

import ca.pkay.rcloneexplorer.RemoteConfig.DynamicConfigOptions.ConfigType
import ca.pkay.rcloneexplorer.RemoteConfig.DynamicConfigOptions.ConfigTypeHolder

open class DynamicConfigDescriptor {

    private val mTypeList: ArrayList<ConfigTypeHolder> = ArrayList()

    public fun addElement(type: ConfigType, title: String, description: String, rcloneOption: String) {
        val holder = ConfigTypeHolder(type, title, description, rcloneOption, null)
        mTypeList.add(holder)
    }

    fun addSpinner(title: String, description: String, rcloneOption: String, spinnerElements: ArrayList<String>) {
        val holder = ConfigTypeHolder(ConfigType.TYPE_SPINNER, title, description, rcloneOption, spinnerElements)
        mTypeList.add(holder)
    }

    fun getTypes (): ArrayList<ConfigTypeHolder> {
        return mTypeList
    }


}