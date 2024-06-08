package ee.oyatl.ime.make.preset.table

import android.content.res.AssetManager
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import ee.oyatl.ime.make.preset.softkeyboard.Keyboard
import kotlinx.serialization.Serializable

data class MoreKeysTable(
    val map: Map<Int, Keyboard> = mapOf(),
) {
    operator fun plus(another: MoreKeysTable): MoreKeysTable {
        return MoreKeysTable(this.map + another.map)
    }

    @Serializable
    data class RefMap(
        @Serializable val map: Map<String, String> = mapOf(),
    ) {
        fun resolve(assets: AssetManager, yaml: Yaml): MoreKeysTable {
            return MoreKeysTable(map.map { (key, value) ->
                val charCode = key.replaceFirst("0x", "").toInt(16)
                val keyboard = yaml.decodeFromStream<Keyboard>(assets.open(value))
                return@map charCode to keyboard
            }.toMap())
        }
    }
}