package ca.pkay.rcloneexplorer.rclone

import org.json.JSONObject

class Provider(val name: String) {

    val options = ArrayList<ProviderOption>()
    var description = ""
    var prefix = ""
    var commandHelp = ""


    companion object {
        fun newInstance(data: JSONObject): Provider {
            val item = Provider(data.optString("Name"))
            item.description = data.optString("Description")
            item.prefix = data.optString("Prefix")
            item.commandHelp = data.optString("CommandHelp")

            val options = data.getJSONArray("Options")

            for (i in 0 until options.length()) {
                item.options.add(ProviderOption.newInstance(options.getJSONObject(i)))
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
                capitalized += s
            }
            wasSpace = s == ' '
        }

        return capitalized
    }

}