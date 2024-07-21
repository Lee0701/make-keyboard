package ee.oyatl.ime.make.module.dictionary

interface PredictingDictionary<T>: ListDictionary<T> {

    fun predict(key: String): List<Pair<String, T>>
}