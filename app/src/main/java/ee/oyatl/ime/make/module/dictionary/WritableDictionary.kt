package ee.oyatl.ime.make.module.dictionary

interface WritableDictionary<T>: Dictionary<T> {
    fun insert(key: String, value: T)
    fun remove(key: T)
}
