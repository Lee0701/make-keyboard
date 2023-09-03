package ee.oyatl.ime.make.table

data class JamoCombinationTable(
    val map: Map<Pair<Int, Int>, Int> = mapOf(),
) {
    constructor(list: List<List<Int>>): this(list.associate { (a, b, result) -> (a to b) to result })
    constructor(vararg list: List<Int>): this(list.toList())

    operator fun plus(another: JamoCombinationTable): JamoCombinationTable {
        return JamoCombinationTable(this.map + another.map)
    }

    operator fun get(key: Pair<Int, Int>): Int? = map[key]
}