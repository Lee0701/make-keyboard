package ee.oyatl.ime.make.module.dictionary

interface HanjaDictionary: ListDictionary<HanjaDictionary.Entry> {
    data class Entry(
        val result: String,
        val extra: String?,
        val frequency: Int,
    )
}
