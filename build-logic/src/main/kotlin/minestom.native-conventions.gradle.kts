plugins {
    id("minestom.common-conventions")
    id("org.graalvm.buildtools.native")
}

graalvmNative {
    binaries {
        named("main") {
            // One day toolchains will support getting this automagically, but that day is not today.
            toolchainDetection.set(false)
        }
    }
}