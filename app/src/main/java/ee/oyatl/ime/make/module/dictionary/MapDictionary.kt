package ee.oyatl.ime.make.module.dictionary

class MapDictionary<T>(private val entries: Map<String, T>): Dictionary<T> {
    override fun search(key: String): T? {
        return entries[key]
    }
}
