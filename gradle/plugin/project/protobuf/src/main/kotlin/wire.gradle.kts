/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.gradle.protobuf.igdb

import org.gradle.kotlin.dsl.dependencies

plugins {
    id("com.squareup.wire")
}

dependencies {
    add("implementation", versionCatalogs.named("libs").findLibrary("wire.runtime").orElseThrow())
}

wire {
    kotlin {
        javaInterop = false
        emitDeclaredOptions = false
        emitAppliedOptions = false
        exclusive = false
    }
    custom {
        schemaHandlerFactoryClass = IgdbFieldsDslGeneratorFactory::class.java.canonicalName
        exclusive = false
        out = layout.buildDirectory.dir("generated/source/wire-igdb-fields").get().toString()
    }
}
