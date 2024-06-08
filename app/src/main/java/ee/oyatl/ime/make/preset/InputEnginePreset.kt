package ee.oyatl.ime.make.preset

import android.content.Context
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import ee.oyatl.ime.make.module.component.InputViewComponent
import ee.oyatl.ime.make.module.component.KeyboardComponent
import ee.oyatl.ime.make.module.inputengine.CodeConverterInputEngine
import ee.oyatl.ime.make.module.inputengine.HangulInputEngine
import ee.oyatl.ime.make.module.inputengine.InputEngine
import ee.oyatl.ime.make.preset.softkeyboard.Include
import ee.oyatl.ime.make.preset.softkeyboard.Keyboard
import ee.oyatl.ime.make.preset.softkeyboard.Row
import ee.oyatl.ime.make.preset.softkeyboard.RowItem
import ee.oyatl.ime.make.preset.table.CharOverrideTable
import ee.oyatl.ime.make.preset.table.CodeConvertTable
import ee.oyatl.ime.make.preset.table.JamoCombinationTable
import ee.oyatl.ime.make.preset.table.MoreKeysTable
import ee.oyatl.ime.make.preset.table.SimpleCodeConvertTable
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule

@Serializable
data class InputEnginePreset(
    val type: Type = Type.Latin,
    val size: Size = Size(),
    val layout: Layout = Layout(),
    val hanja: Hanja = Hanja(),
    val components: List<InputViewComponentType> = listOf(),
    val autoUnlockShift: Boolean = true,
    val candidatesView: Boolean = false,
) {

    fun inflate(context: Context, rootListener: InputEngine.Listener, disableTouch: Boolean = false): InputEngine {
        // Soft keyboards will be resolved later by components.
        val moreKeysTable = loadMoreKeysTable(context, names = layout.moreKeysTable)
        val convertTable = loadConvertTable(context, names = layout.codeConvertTable)
        val overrideTable = loadOverrideTable(context, names = layout.overrideTable)
        val combinationTable = loadCombinationTable(context, names = layout.combinationTable)

        fun inflateComponents(preset: InputEnginePreset): List<InputViewComponent> {
            val rows = this.components
                .map { it.inflate(context, preset, disableTouch) }
                .filterIsInstance<KeyboardComponent>()
                .sumOf { it.keyboard.rows.size }
            val rowHeight = if(this.size.unifyHeight && rows != 0) size.rowHeight * 4 / rows else size.rowHeight
            return this.components
                .map { it.inflate(context, preset.copy(size = preset.size.copy(rowHeight = rowHeight)), disableTouch) }
        }

        fun getHangulInputEngine(listener: InputEngine.Listener): InputEngine {
            return HangulInputEngine(
                convertTable = convertTable,
                moreKeysTable = moreKeysTable,
                overrideTable = overrideTable,
                jamoCombinationTable = combinationTable,
                listener = listener,
            )
        }

        fun getTableInputEngine(listener: InputEngine.Listener): InputEngine {
            return CodeConverterInputEngine(
                convertTable = convertTable,
                moreKeysTable = moreKeysTable,
                overrideTable = overrideTable,
                listener = listener,
            )
        }

        fun getInputEngine(listener: InputEngine.Listener): InputEngine {
            return when(type) {
                Type.Latin -> getTableInputEngine(listener)
                Type.Hangul -> getHangulInputEngine(listener)
                Type.Symbol -> getTableInputEngine(listener)
            }.apply {
                components = inflateComponents(this@InputEnginePreset)
                components.filterIsInstance<KeyboardComponent>().forEach {
                    it.connectedInputEngine = this
                    it.updateView()
                }
            }
        }

        return getInputEngine(rootListener)
    }

    fun mutable(): Mutable {
        return Mutable(
            type = this.type,
            size = size.mutable(),
            layout = this.layout.mutable(),
            hanja = this.hanja.mutable(),
            components = this.components.toMutableList(),
            autoUnlockShift = this.autoUnlockShift,
        )
    }

    data class Mutable (
        var type: Type = Type.Latin,
        var size: Size.Mutable = Size.Mutable(),
        var layout: Layout.Mutable = Layout.Mutable(),
        var hanja: Hanja.Mutable = Hanja.Mutable(),
        var components: MutableList<InputViewComponentType> = mutableListOf(),
        var autoUnlockShift: Boolean = true
    ) {
        fun commit(): InputEnginePreset {
            return InputEnginePreset(
                type = type,
                size = size.commit(),
                layout = layout.commit(),
                hanja = hanja.commit(),
                components = components.toList(),
                autoUnlockShift = autoUnlockShift,
            )
        }
    }

    @Serializable
    enum class Type {
        Latin, Hangul, Symbol
    }

    @Serializable
    data class Size(
        val unifyHeight: Boolean = false,
        val defaultHeight: Boolean = true,
        val rowHeight: Int = 55,
    ) {
        fun mutable(): Mutable {
            return Mutable(
                unifyHeight = unifyHeight,
                defaultHeight = defaultHeight,
                rowHeight = rowHeight,
            )
        }

        data class Mutable(
            var unifyHeight: Boolean = false,
            var defaultHeight: Boolean = true,
            var rowHeight: Int = 55,
        ) {
            fun commit(): Size {
                return Size(
                    unifyHeight = unifyHeight,
                    defaultHeight = defaultHeight,
                    rowHeight = rowHeight,
                )
            }
        }
    }

    @Serializable
    data class Layout(
        val softKeyboard: List<String> = listOf(),
        val moreKeysTable: List<String> = listOf(),
        val codeConvertTable: List<String> = listOf(),
        val overrideTable: List<String> = listOf(),
        val combinationTable: List<String> = listOf(),
    ) {
        fun mutable(): Mutable {
            return Mutable(
                softKeyboard = softKeyboard,
                moreKeysTable = moreKeysTable,
                codeConvertTable = codeConvertTable,
                overrideTable = overrideTable,
                combinationTable = combinationTable,
            )
        }

        data class Mutable(
            var softKeyboard: List<String> = listOf(),
            var moreKeysTable: List<String> = listOf(),
            var codeConvertTable: List<String> = listOf(),
            var overrideTable: List<String> = listOf(),
            var combinationTable: List<String> = listOf(),
        ) {
            fun commit(): Layout {
                return Layout(
                    softKeyboard = softKeyboard,
                    moreKeysTable = moreKeysTable,
                    codeConvertTable = codeConvertTable,
                    overrideTable = overrideTable,
                    combinationTable = combinationTable,
                )
            }
        }
    }

    @Serializable
    data class Hanja(
        val conversion: Boolean = false,
        val additionalDictionaries: Set<String> = mutableSetOf(),
    ) {
        fun mutable(): Mutable {
            return Mutable(
                conversion = conversion,
                additionalDictionaries = additionalDictionaries.toMutableSet(),
            )
        }

        data class Mutable(
            var conversion: Boolean = false,
            var prediction: Boolean = false,
            var sortByContext: Boolean = false,
            var additionalDictionaries: MutableSet<String> = mutableSetOf(),
        ) {
            fun commit(): Hanja {
                return Hanja(
                    conversion = conversion,
                    additionalDictionaries = additionalDictionaries,
                )
            }
        }
    }

    companion object {
        private val yamlConfig = YamlConfiguration(encodeDefaults = false)
        val yaml = Yaml(EmptySerializersModule(), yamlConfig)

        fun resolveSoftKeyIncludes(context: Context, row: Row): List<RowItem> {
            return row.keys.flatMap { rowItem ->
                if(rowItem is Include) resolveSoftKeyIncludes(context,
                    yaml.decodeFromStream(context.assets.open(rowItem.name)))
                else listOf(rowItem)
            }
        }

        fun loadSoftKeyboards(context: Context, names: List<String>): Keyboard {
            val resolved = names.mapNotNull { filename ->
                val keyboard = kotlin
                    .runCatching { yaml.decodeFromStream<Keyboard>(context.assets.open(filename)) }
                    .getOrNull()
                    ?: return@mapNotNull null
                keyboard.copy(
                    rows = keyboard.rows.map { it.copy(resolveSoftKeyIncludes(context, it)) }
                )
            }
            return resolved.fold(Keyboard()) { acc, input -> acc + input }
        }

        fun loadConvertTable(context: Context, names: List<String>): CodeConvertTable {
            val resolved = names.map { filename ->
                yaml.decodeFromStream<CodeConvertTable>(context.assets.open(filename)) }
            return resolved.fold(SimpleCodeConvertTable() as CodeConvertTable) { acc, input -> acc + input }
        }

        fun loadOverrideTable(context: Context, names: List<String>): CharOverrideTable {
            val resolved = names.map { filename ->
                yaml.decodeFromStream<CharOverrideTable>(context.assets.open(filename)) }
            return resolved.fold(CharOverrideTable()) { acc, input -> acc + input }
        }

        fun loadCombinationTable(context: Context, names: List<String>): JamoCombinationTable {
            val resolved = names.map { filename ->
                yaml.decodeFromStream<JamoCombinationTable>(context.assets.open(filename)) }
            return resolved.fold(JamoCombinationTable()) { acc, input -> acc + input }
        }

        fun loadMoreKeysTable(context: Context, names: List<String>): MoreKeysTable {
            val resolved = names.map { filename ->
                val refMap = yaml.decodeFromStream<MoreKeysTable.RefMap>(context.assets.open(filename))
                refMap.resolve(context.assets, yaml)
            }
            return resolved.fold(MoreKeysTable()) { acc, input -> acc + input }
        }
    }
}