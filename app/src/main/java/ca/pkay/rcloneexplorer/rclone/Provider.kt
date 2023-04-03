package ca.pkay.rcloneexplorer.rclone

import org.json.JSONObject

class Provider(val name: String) {

    val options = ArrayList<ProviderOption>()
    var description = ""
    var Prefix = ""
    var CommandHelp = ""


    companion object {
        fun newInstance(data: JSONObject): Provider {
            val item = Provider(data.optString("Name"))

            val options = data.getJSONArray("Options")

            for (i in 0 until options.length()) {
                item.options.add(ProviderOption.newInstance(options.getJSONObject(i)))
            }

            return item
        }
    }

}