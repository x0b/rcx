package ca.pkay.rcloneexplorer.RemoteConfig.DynamicConfigs

import ca.pkay.rcloneexplorer.RemoteConfig.DynamicConfigOptions.ConfigType

class WebdavConfig(): DynamicConfigDescriptor() {

    init {
        addElement(ConfigType.TYPE_STRING, "title", "enter your title", "none")
    }

}