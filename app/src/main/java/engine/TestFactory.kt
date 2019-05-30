package engine

import engine.impl.GlideEngine

object TestFactory {

    fun main() {
        ImageLoaderFactory.createImageEngine(GlideEngine::class.java)
    }
}
