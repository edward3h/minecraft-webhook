plugins {
    java
    id("com.diffplug.spotless")
    id("com.github.jakemarsden.git-hooks")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    sourceCompatibility = JavaVersion.toVersion("25")
    targetCompatibility = JavaVersion.toVersion("25")
}

spotless {
    format("misc") {
        target("*.md", ".gitignore")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
        licenseHeader("/* (C) Edward Harman \$YEAR */").updateYearWithLatest(true)
    }
    kotlinGradle {
        ktlint()
    }
}

gitHooks {
    hooks = mapOf("pre-commit" to "check")
    hooksDirectory = file("${project.rootDir}/.git/hooks")
}
