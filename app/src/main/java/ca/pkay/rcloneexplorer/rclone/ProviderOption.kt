package ca.pkay.rcloneexplorer.rclone

import org.json.JSONObject
import java.util.Objects

class ProviderOption {

    var name: String = ""
    var help: String = ""
    var provider: String = ""
    var default: String = ""
    var value: Objects? = null
    var examples: ArrayList<OptionExampleItem> = ArrayList()
    var shortOpt: String = ""
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
        fun newInstance(data: JSONObject): ProviderOption {
            val item = ProviderOption()

            item.name = data.getString("Name")
            item.help = data.getString("Help")
            item.provider = data.getString("Type")
            item.default = data.getString("Default")
            //item.value = data.get("Value")
            item.shortOpt = data.getString("ShortOpt")
            item.hide = data.getInt("Hide")
            item.required = data.getBoolean("Required")
            item.isPassword = data.getBoolean("IsPassword")
            item.noPrefix = data.getBoolean("NoPrefix")
            item.advanced = data.getBoolean("Advanced")
            item.exclusive = data.getBoolean("Exclusive")
            item.defaultStr = data.getString("DefaultStr")
            item.valueStr = data.getString("ValueStr")
            item.type = data.getString("Type")

            val examples = data.optJSONArray("Examples")
            if (examples != null) {
                for (i in 0 until examples.length()) {
                    item.examples.add(OptionExampleItem(
                        examples.getJSONObject(i).getString("Value"),
                        examples.getJSONObject(i).getString("Help"),
                        examples.getJSONObject(i).getString("Provider")
                    ))
                }
            }

            return item
        }
    }


    fun getNameCapitalized(): String {
        var tempName = name
        var wasSpace = false
        var capitalized = tempName[0].uppercaseChar().toString()

        for(s in tempName.drop(1)){
            if(wasSpace){
                capitalized += s.uppercaseChar()
            } else {
                capitalized += if(s == '_') ' ' else s
            }
            wasSpace = s == '_'
        }

        return capitalized
    }


}