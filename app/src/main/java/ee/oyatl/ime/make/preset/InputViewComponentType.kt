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
        R.string.pref_layout_component_text_edit_title);
//    LanguageTab(R.drawable.baseline_language_24,
//        R.string.pref_layout_component_language_switcher_title);

    fun inflate(context: Context, preset: InputEnginePreset, disableTouch: Boolean): InputViewComponent {
        val loader = PresetLoader(context)
        val rowHeight = preset.size.rowHeight
        return when(this) {
            MainKeyboard -> {
                KeyboardComponent(
                    keyboard = InputEnginePreset.loadSoftKeyboards(context, preset.layout.softKeyboard),
                    rowHeight = rowHeight,
                    autoUnlockShift = preset.autoUnlockShift,
                    disableTouch = disableTouch,
                )
            }
            NumberRow -> {
                val layouts = loader.modFilenames(
                    listOf(KeyboardLayoutSettingsFragment.NUMBER_SOFT_ID))
                KeyboardComponent(
                    keyboard = InputEnginePreset.loadSoftKeyboards(context, layouts),
                    rowHeight = rowHeight,
                    direct = true,
                    autoUnlockShift = preset.autoUnlockShift,
                    disableTouch = disableTouch,
                )
            }
            Candidates -> {
                CandidatesComponent(
                    width = context.resources.displayMetrics.widthPixels,
                    height = rowHeight,
                    disableTouch = disableTouch,
                ).apply {
                    if(context is CandidateListener) listener = context
                }
            }
            TextEdit -> {
                val layouts = loader.modFilenames(
                    listOf(KeyboardLayoutSettingsFragment.TEXT_EDIT_SOFT_ID))
                KeyboardComponent(
                    keyboard = InputEnginePreset.loadSoftKeyboards(context, layouts),
                    rowHeight = rowHeight,
                    autoUnlockShift = preset.autoUnlockShift,
                    disableTouch = disableTouch,
                )
            }
            else -> {
                EmptyComponent
            }
        }
    }
}