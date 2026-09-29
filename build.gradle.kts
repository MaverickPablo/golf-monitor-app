plugins {
    id("com.android.application") apply false
    kotlin("android") apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
