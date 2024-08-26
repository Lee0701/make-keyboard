package ee.oyatl.ime.make.preset

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.module.candidates.CandidateListener
import ee.oyatl.ime.make.module.component.CandidatesComponent
import ee.oyatl.ime.make.module.component.EmptyComponent
import ee.oyatl.ime.make.module.component.InputViewComponent
import ee.oyatl.ime.make.module.component.KeyboardComponent
import ee.oyatl.ime.make.module.component.LanguageTabBarComponent
import ee.oyatl.ime.make.settings.KeyboardLayoutSettingsFragment

enum class InputViewComponentType(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
) {
    MainKeyboard(
        R.drawable.baseline_keyboard_24,
        R.string.pref_layout_component_main_keyboard_title),
    NumberRow(
        R.drawable.baseline_123_24,
        R.string.pref_layout_component_number_row_title),
    Candidates(
        R.drawable.baseline_abc_24,
        R.string.pref_layout_component_candidates_title),
    TextEdit(R.drawable.baseline_text_select_move_forward_character,
        R.string.pref_layout_component_text_edit_title),
    LanguageTabBar(R.drawable.baseline_language_24,
        R.string.pref_layout_component_language_switcher_title);

    fun inflate(context: Context, preset: InputEnginePreset, mode: InputEnginePreset.Mode): InputViewComponent {
        val presetLoader = PresetLoader(context)
        val rowHeight = preset.size.rowHeight
        return when(this) {
            MainKeyboard -> {
                KeyboardComponent(
                    keyboard = InputEnginePreset.loadSoftKeyboards(context, preset.layout.softKeyboard),
                    rowHeight = rowHeight,
                    disableTouch = mode.disableTouch
                )
            }
            NumberRow -> {
                val layouts = listOf(KeyboardLayoutSettingsFragment.NUMBER_ROW_SOFT_ID)
                KeyboardComponent(
                    keyboard = InputEnginePreset.loadSoftKeyboards(context, layouts),
                    rowHeight = rowHeight,
                    disableTouch = mode.disableTouch
                )
            }
            Candidates -> {
                CandidatesComponent(
                    width = context.resources.displayMetrics.widthPixels,
                    height = rowHeight,
                    disableTouch = mode.disableTouch
                ).apply {
                    if(context is CandidateListener) listener = context
                }
            }
            TextEdit -> {
                val layouts = listOf(KeyboardLayoutSettingsFragment.TEXT_EDIT_SOFT_ID)
                KeyboardComponent(
                    keyboard = InputEnginePreset.loadSoftKeyboards(context, layouts),
                    rowHeight = rowHeight,
                    disableTouch = mode.disableTouch
                )
            }
            LanguageTabBar -> {
                preset.hangul
                LanguageTabBarComponent(
                    listener = context as? LanguageTabBarComponent.Listener,
                    width = context.resources.displayMetrics.widthPixels,
                    disableTouch = mode.disableTouch
                ).apply {
                    // Add example tabs
                    if(mode != InputEnginePreset.Mode.Runtime) {
                        tabs += LanguageTabBarComponent.Tab(0, R.string.lang_label_en, true)
                        tabs += LanguageTabBarComponent.Tab(1, R.string.lang_label_ko)
                    }
                }
            }
            else -> {
                EmptyComponent
            }
        }
    }
}