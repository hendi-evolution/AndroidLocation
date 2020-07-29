package com.senjuid.location

class LocationPluginOptions private constructor(
        var data: String?,
        var message1: String?,
        var message2: String?
) {
    data class Builder(
            var data: String? = null,
            var message1: String? = null,
            var message2: String? = null
    ) {
        fun setMessage(message1: String?, message2: String?) = apply { this.message1 = message1 }.apply { this.message2 = message2 }
        fun setData(data: String) = apply { this.data = data }
        fun build() = LocationPluginOptions(data, message1, message2)
    }
}