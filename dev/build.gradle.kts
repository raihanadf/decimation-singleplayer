
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

// JUnit 5 for the plain Java units (fixes/hosting)
tasks.test { useJUnitPlatform() }

// classpath of the test sources (plan Task 4 step 5: TlsCheck on the game Java)
tasks.register("printTestClasspath") { doLast { println(sourceSets["test"].runtimeClasspath.asPath) } }
