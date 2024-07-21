package ee.oyatl.ime.make.module.dictionary

interface Dictionary<T> {
    fun search(key: String): T?
}
