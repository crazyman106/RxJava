package rxbus.sample1

class Message constructor(code: Int, obj: Any) {
    private var code: Int = code
    private var obj: Any = obj
}