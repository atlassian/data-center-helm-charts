plugins {
    java
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation("org.assertj:assertj-core:3.17.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
}

group = "com.atlassian.k8s"
version = "0.1.0-SNAPSHOT"
description = "data-center-helm-charts"
java.sourceCompatibility = JavaVersion.VERSION_11

tasks.withType<Test> {
    useJUnitPlatform()
}
