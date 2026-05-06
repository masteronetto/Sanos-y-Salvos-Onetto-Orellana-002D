// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.2.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    id("org.springframework.boot") version "3.4.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    kotlin("jvm") version "2.1.10" apply false
    kotlin("plugin.spring") version "2.1.10" apply false
}

import java.net.InetSocketAddress
import java.net.Socket

// Development helpers: start/stop backend services (docker-compose)
tasks.register("startBackendServices") {
    group = "dev"
    description = "Start backend services via docker-compose (configurable with -PbackendComposeFile=... )"
    doLast {
        val composeFile = (project.findProperty("backendComposeFile") as? String)
            ?: File(project.rootDir, "docker-compose.yml").absolutePath
        println("Starting backend services using compose file: $composeFile")
        // Try modern `docker compose` first, fallback to `docker-compose`
        try {
            val pb = ProcessBuilder("docker", "compose", "-f", composeFile, "up", "-d", "--remove-orphans")
            pb.inheritIO()
            val p = pb.start()
            val rc = p.waitFor()
            if (rc != 0) throw GradleException("docker compose up failed with exit code $rc")
        } catch (t: Throwable) {
            val pb = ProcessBuilder("docker-compose", "-f", composeFile, "up", "-d", "--remove-orphans")
            pb.inheritIO()
            val p = pb.start()
            val rc = p.waitFor()
            if (rc != 0) throw GradleException("docker-compose up failed with exit code $rc")
        }
    }
}

tasks.register("stopBackendServices") {
    group = "dev"
    description = "Stop backend services started by docker-compose (use -PbackendComposeFile to override)"
    doLast {
        val composeFile = (project.findProperty("backendComposeFile") as? String)
            ?: File(project.rootDir, "docker-compose.yml").absolutePath
        println("Stopping backend services using compose file: $composeFile")
        try {
            val pb = ProcessBuilder("docker", "compose", "-f", composeFile, "down")
            pb.inheritIO()
            val p = pb.start()
            val rc = p.waitFor()
            if (rc != 0) throw GradleException("docker compose down failed with exit code $rc")
        } catch (t: Throwable) {
            val pb = ProcessBuilder("docker-compose", "-f", composeFile, "down")
            pb.inheritIO()
            val p = pb.start()
            val rc = p.waitFor()
            if (rc != 0) throw GradleException("docker-compose down failed with exit code $rc")
        }
    }
}

tasks.register("waitForBackend") {
    group = "dev"
    description = "Wait until configured backend ports are accepting connections (defaults: 8080,8081). Use -PbackendWaitPorts=8080,8081 and -PbackendWaitTimeout=60 to override."
    doLast {
        val portsProp = (project.findProperty("backendWaitPorts") as? String) ?: "5672"
        val ports = portsProp.split(',').mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toInt() }
        val timeoutSeconds = (project.findProperty("backendWaitTimeout") as? String)?.toInt() ?: 60
        val startTime = System.currentTimeMillis()
        ports.forEach { port ->
            print("Waiting for localhost:$port ... ")
            var ok = false
            while (!ok) {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress("localhost", port), 2000)
                    }
                    ok = true
                    println("OK")
                } catch (e: Exception) {
                    if (System.currentTimeMillis() - startTime > timeoutSeconds * 1000) {
                        throw GradleException("Timeout waiting for port $port after ${timeoutSeconds}s")
                    }
                    Thread.sleep(1000)
                }
            }
        }
    }
}

// Convenience task that starts services and waits before building
tasks.register("prepareBackend") {
    group = "dev"
    description = "Start backend services and wait for them to be ready"
    dependsOn("startBackendServices", "waitForBackend")
}

// Make assembleDebug depend on backend preparation when present in the task graph
gradle.taskGraph.whenReady {
    // Ensure we only configure existing `assembleDebug` tasks across all projects.
    allprojects {
        tasks.matching { it.name == "assembleDebug" }.configureEach {
            dependsOn("prepareBackend")
        }
    }
}