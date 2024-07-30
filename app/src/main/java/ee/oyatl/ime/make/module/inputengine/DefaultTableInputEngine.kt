package ee.oyatl.ime.make.module.inputengine

import ee.oyatl.ime.make.modifiers.DefaultShiftKeyHandler
import ee.oyatl.ime.make.preset.table.CharOverrideTable
import ee.oyatl.ime.make.preset.table.CodeConvertTable
import ee.oyatl.ime.make.preset.table.MoreKeysTable

class DefaultTableInputEngine(
    convertTable: CodeConvertTable,
    overrideTable: CharOverrideTable,
    moreKeysTable: MoreKeysTable,
    shiftKeyHandler: DefaultShiftKeyHandler,
    listener: InputEngine.Listener
): BasicTableInputEngine(convertTable, overrideTable, moreKeysTable, shiftKeyHandler, listener)
