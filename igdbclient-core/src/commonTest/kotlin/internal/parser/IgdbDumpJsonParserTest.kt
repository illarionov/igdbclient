/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.igdbclient.library.test.IgnoreAndroid
import at.released.igdbclient.model.dump.IgdbDump
import com.squareup.wire.ofEpochSecond
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertFails

@IgnoreAndroid
class IgdbDumpJsonParserTest {
    private val parser = IgdbParser::igdbDumpJsonParser

    @Test
    fun parser_should_throw_on_malformed_data() = tableOf("MalformedJson")
        .row("")
        .row("{}")
        .row("[{}]")
        .row("""[{"status": "string"}]""")
        .forAll { malformedJson ->
            assertFails {
                parser(Buffer().write(malformedJson.encodeToByteArray()))
            }
        }

    @Test
    fun parser_should_return_correct_result_on_correct_data() = tableOf("Source", "Expected Result")
        .row(
            """
            {
                "s3_url": "S3_DOWNLOAD_URL",
                "endpoint": "games",
                "file_name": "1234567890_games.csv",
                "size_bytes": 123456789,
                "updated_at": 1234567890,
                "schema_version": "1234567890",
                "schema": {
                    "id": "LONG",
                    "name": "STRING",
                    "url": "STRING",
                    "franchises": "LONG[]",
                    "rating": "DOUBLE",
                    "created_at": "TIMESTAMP",
                    "checksum": "UUID"
                }
            }
            """.trimIndent(),
            IgdbDump(
                s3Url = "S3_DOWNLOAD_URL",
                endpoint = "games",
                fileName = "1234567890_games.csv",
                sizeBytes = 123_456_789,
                updatedAt = ofEpochSecond(1_234_567_890, 0),
                schemaVersion = "1234567890",
                schema = mapOf(
                    "id" to "LONG",
                    "name" to "STRING",
                    "url" to "STRING",
                    "franchises" to "LONG[]",
                    "rating" to "DOUBLE",
                    "created_at" to "TIMESTAMP",
                    "checksum" to "UUID",
                ),
            ),
        )
        .forAll { testSource, expectedResult ->
            val source = Buffer().write(testSource.encodeToByteArray())
            val response = parser(source)
            assertThat(response)
                .isEqualTo(expectedResult)
        }
}
