package ca.pkay.rcloneexplorer.rclone

import org.json.JSONObject
import java.util.Objects

class ProviderItem {

    var name: String = ""
    var help: String = ""
    var provider: String = ""
    var default: String = ""
    var value: Objects? = null
    var ShortOpt: String = ""
    var hide: Int = 0
    var required: Boolean = false
    var isPassword: Boolean = false
    var noPrefix: Boolean = false
    var advanced: Boolean = false
    var exclusive: Boolean = false
    var defaultStr: String = ""
    var valueStr: String = ""
    var type: String = ""


    companion object {
        fun newInstance(data: JSONObject): ProviderItem {
            val item = ProviderItem()

            item.name = data.getString("Name")
            item.help = data.getString("Help")
            item.type = data.getString("Type")

            return item
        }
    }


}