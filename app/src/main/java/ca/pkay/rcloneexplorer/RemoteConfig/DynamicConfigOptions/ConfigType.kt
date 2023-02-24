package ca.pkay.rcloneexplorer.RemoteConfig.DynamicConfigOptions

enum class ConfigType {
    TYPE_STRING,
    TYPE_NUMBER,
    TYPE_PASSWORD,
    TYPE_URL,
    TYPE_SPINNER,
    TYPE_HIDDEN // this type is used for elements that need to be inserted in a config, but are not user modifiable. The title is then used as the value.
}