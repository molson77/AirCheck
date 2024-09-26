package com.example.aircheck.data

import com.google.gson.*
import java.lang.reflect.Type

class AqiResponseDeserializer : JsonDeserializer<AqiResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): AqiResponse {
        val jsonObject = json.asJsonObject
        val status = jsonObject.get("status").asString

        return if (status == "ok") {
            // Deserialize to AqiSuccessResponse
            val data = context.deserialize<Data>(jsonObject.get("data"), Data::class.java)
            AqiSuccessResponse(status, data)
        } else {
            // Deserialize to AqiErrorResponse
            val errorData = jsonObject.get("data").asString // Assuming error data is a string
            AqiErrorResponse(status, errorData)
        }
    }
}