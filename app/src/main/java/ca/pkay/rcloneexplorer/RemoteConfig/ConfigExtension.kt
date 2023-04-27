package ca.pkay.rcloneexplorer.RemoteConfig


interface ConfigExtension {

    fun setupRemotes()

    fun next()

    fun useConfigCreate(): Boolean
}