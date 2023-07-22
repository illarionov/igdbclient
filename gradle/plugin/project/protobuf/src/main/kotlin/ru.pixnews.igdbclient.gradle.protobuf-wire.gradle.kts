/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

import ru.pixnews.igdbclient.gradle.base.versionCatalog

plugins {
    id("com.squareup.wire")
}

dependencies {
    add("implementation", versionCatalog.findLibrary("wire.runtime").orElseThrow())
}

wire {
    kotlin {
        javaInterop = false
        emitDeclaredOptions = false
        emitAppliedOptions = false
    }
}
