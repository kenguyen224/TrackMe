package com.example.kenv.trackme.domain.transform

import android.text.TextUtils
import com.example.kenv.lazyinit.resettableLazy
import com.example.kenv.lazyinit.resettableManager
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.reflect.Type
import org.json.JSONException
import org.json.JSONObject

object GSonUtils {

    private val NumberTypeAdapter: TypeAdapter<Number> = object : TypeAdapter<Number>() {
        /**
         * Writes one JSON value (an array, object, string, number, boolean or null)
         * for `value`.
         *
         * @param out
         * @param value the Java object to write. May be null.
         */
        @Throws(IOException::class)
        override fun write(out: JsonWriter, value: Number?) {
            out.value(value)
        }

        /**
         * Reads one JSON value (an array, object, string, number, boolean or null)
         * and converts it to a Java object. Returns the converted object.
         *
         * @param in
         * @return the converted Java object. May be null.
         */
        @Throws(IOException::class)
        override fun read(`in`: JsonReader): Number? {
            if (`in`.peek() == JsonToken.NULL) {
                `in`.nextNull()
                return null
            }
            val result = `in`.nextString()
            if ("" == result) {
                return null
            }
            try {
                return result.toLong()
            } catch (e: NumberFormatException) {
                // empty catch exception to try another parser Double
            }
            return try {
                result.toDouble()
            } catch (e: NumberFormatException) {
                throw JsonSyntaxException(e)
            }
        }
    }

    private val gSonBuilder: GsonBuilder = GsonBuilder().apply {
        setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        registerTypeAdapter(Long::class.javaPrimitiveType, NumberTypeAdapter)
        registerTypeAdapter(Long::class.java, NumberTypeAdapter)
        registerTypeAdapter(Double::class.javaPrimitiveType, NumberTypeAdapter)
        registerTypeAdapter(Double::class.java, NumberTypeAdapter)
    }
    private val lazyManager = resettableManager()
    private val gSon: Gson by resettableLazy(lazyManager) {
        gSonBuilder.create()
    }

    fun toJsonString(obj: Any?): String {
        return gSon.toJson(obj)
    }

    fun <T> fromJsonString(sJson: String?, t: Type): T? {
        return if (TextUtils.isEmpty(sJson)) {
            null
        } else gSon.fromJson(sJson, t)
    }
}
