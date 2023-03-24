package ca.pkay.rcloneexplorer.RemoteConfig.DynamicConfigs

import ca.pkay.rcloneexplorer.RemoteConfig.DynamicConfigOptions.ConfigType

class WebdavConfig(): DynamicConfigDescriptor() {

    init {
        addElement(ConfigType.TYPE_STRING, "title", "enter your title", "none")
        addElement(ConfigType.TYPE_STRING, "username", "enter your username", "none")
        addElement(ConfigType.TYPE_PASSWORD, "password", "enter your password", "none")
        val options = arrayListOf(
            "Nextcloud",
            "Owncloud",
            "Sharepoint",
            "Other"
        )
        addSpinner("vendor", "enter your vendor", "none", options)
    }

}