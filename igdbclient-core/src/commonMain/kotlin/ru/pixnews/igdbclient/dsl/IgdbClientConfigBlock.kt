/*
 * Copyright 2023 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.pixnews.igdbclient.dsl

import ru.pixnews.igdbclient.internal.model.IgdbClientConfig

@IgdbClientDsl
public class IgdbClientConfigBlock<C : IgdbHttpEngineConfig> {
    /**
     * Base url of the IGDB server used to make requests.
     *
     * Can be overridden, for example, when using your own proxy server.
     */
    public var baseUrl: String = IGDB_BASE_URL

    /**
     * Allows you to specify the User-agent header of requests.
     *
     * If not set, the value is defined by the HTTP client being used.
     */
    public var userAgent: String? = null
    private var twitchAuthConfig: TwitchAuthConfig = TwitchAuthConfig().apply { enabled = false }
    private var httpClientConfig: C.() -> Unit = {}
    private var retryPolicy: RetryPolicy = RetryPolicy()
    private var headers: Map<String, List<String>> = emptyMap()

    /**
     * Configuration of the HTTP client used to make requests.
     */
    public fun httpClient(block: C.() -> Unit) {
        val oldConfig = httpClientConfig
        httpClientConfig = {
            oldConfig()
            block()
        }
    }

    /**
     * Specifies custom headers that will be added to each HTTP request.
     *
     * "Client-ID" and "Authorization" headers specified in this block take precedence over the headers
     * added by the twitch authenticator if it is enabled in the [twitchAuth] block.
     */
    public fun headers(block: HeadersBuilder.() -> Unit) {
        headers = HeadersBuilder().apply(block).build()
    }

    /**
     * Enables authentication using the Twitch client credentials grant flow
     * and allows you to customize the authentication parameters.
     *
     * Don't add this block if you want to handle authentication yourself.
     */
    public fun twitchAuth(block: TwitchAuthConfig.() -> Unit) {
        twitchAuthConfig = TwitchAuthConfig().apply(block)
    }

    /**
     * Allows you to configure automatic retry on HTTP 429 "Too Many Requests" error.
     *
     * Enabled by default.
     */
    public fun retryPolicy(block: RetryPolicy.() -> Unit) {
        retryPolicy = RetryPolicy().apply(block)
    }

    internal fun build(): IgdbClientConfig<C> = IgdbClientConfig(
        baseUrl = baseUrl,
        userAgent = userAgent,
        twitchAuthConfig = twitchAuthConfig,
        httpClientConfig = httpClientConfig,
        retryPolicy = retryPolicy,
        headers = headers,
    )

    public companion object {
        public const val IGDB_BASE_URL: String = "https://api.igdb.com/v4/"
        public const val IGDB_IMAGE_URL: String = "https://images.igdb.com/igdb/image/upload/"
    }
}
