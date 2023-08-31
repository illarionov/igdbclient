# Igdbclient

[![Maven Central](https://img.shields.io/maven-central/v/ru.pixnews.igdbclient/igdbclient-core)](https://central.sonatype.com/artifact/ru.pixnews.igdbclient/igdbclient-core/)
[![build](https://github.com/illarionov/igdbclient/actions/workflows/Build.yml/badge.svg)](https://github.com/illarionov/igdbclient/actions/workflows/Build.yml)


Igdbclient is a Kotlin Multiplatform library for fetching information about games from the
[IGDB.com](https://www.igdb.com/) Video Game Database.

- Can be used with [Ktor] or [Okhttp] as an HTTP client
- Implementation of the [IGDB v4 API][IGDB API] protocol using POST requests and Protobuf as the data encoding format
- Support for Twitch Oauth 2 authentication
- Multi-queries and count-requests
- Configurable automatic retry on HTTP `429 Too Many Requests` error
- Helpers for building image URLs using a specified pixel density
- Support for Webhook API
- Android, iOS, Desktop and JavaScript targets

## Releases

The latest release is available on [Maven Central].

```kotlin
repositories {
    mavenCentral()
}
```

Dependency when using *Кtor* client (supported on all targets):

```kotlin
dependencies {
    implementation("ru.pixnews.igdbclient:igdbclient-ktor:0.2")
}
```

In Kotlin Multiplatform projects, this dependency can be added to the *commonMain* sourceSet:

```kotlin
commonMain {
    dependencies {
        implementation("ru.pixnews.igdbclient:igdbclient-ktor:0.2")
    }
}
```

Dependency when using *Okhttp* (JVM and Android):

```kotlin
dependencies {
    implementation("ru.pixnews.igdbclient:igdbclient-okhttp:0.2")
}
```

## Usage

Create a client to access the API:

```kotlin
val client = IgdbClient(IgdbKtorEngine) {
}
```

Example to fetch the games:

```kotlin
val games: GameResult = client.getGames {
    fields("*")
    search("Diablo")
}
log.i("games: $games")
```

Apicalypse query with all the fields:

```kotlin
apicalypseQuery {
    fields("id", "name", "genres.name")
    exclude("rating")
    where("rating >= 80 & release_dates.date > 631152000")
    search("Diablo")
    sort("release_dates.date", DESC)
    offset(5)
    limit(10)
}
```

Each public IGDB API endpoint has a function defined in the client to fetch data.
All the client methods are suspend functions and should be called from the coroutine context.

```kotlin
client.getAgeRatings {…}
client.getAgeRatingContentDescriptions {…}
client.getAlternativeNames {…}
…
client.getThemes {…}
client.getWebsites {…}
```

Example of count — query:

```kotlin
val diabloGamesCount = client.executeOrThrow(
    IgdbEndpoint.GAME.countEndpoint(),
    apicalypseQuery {
        fields("*")
        search("Diablo")
    },
) 

log.i { "games count: $diabloGamesCount" }
```

To make multiple queries in a single request, you can use the `client.multiquery()`:

```kotlin
val response = client.multiquery {
    query(IgdbEndpoint.PLATFORM.countEndpoint(), "Count of Platforms") {}
    query(IgdbEndpoint.GAME, "Playstation Games") {
        fields("name", "category", "platforms.name")
        limit(5)
    }  
}  
val platformCount = response[0].count
val games = response[1].results as List<Game>?
```

See https://api-docs.igdb.com/#multi-query for additional info.

### Ktor client

Pass `IgdbKtorEngine` as the first argument to `IgdbClient()` when using the Ktor client.\
You can pass your own customized Ktor client that will be used to make requests in the *httpClient* block.

```kotlin
val ktorHttpClient: HttpClient = HttpClient(Java) {
    developmentMode = true
    install(Logging) {
        level = LogLevel.ALL
    }
}

val client = IgdbClient(IgdbKtorEngine) {
    httpClient {
        httpClient = ktorHttpClient
        backgroundDispatcher = Dispatchers.IO
    }
}
```

### Okhttp client

Pass `IgdbOkhttpEngine` to `IgdbClient()` when using the Okhttp client.\
A pre-configured client can be specified using the *callFactory* parameter of the *httpClient* configuration block:

```kotlin
val okhttpClient = OkHttpClient.Builder().apply {
    connectionPool(
        ConnectionPool(
            maxIdleConnections = 8,
            keepAliveDuration = 2000,
            timeUnit = TimeUnit.MILLISECONDS,
        ),
    )
    dispatcher(
        Dispatcher().apply {
            maxRequestsPerHost = 8
        },
    )
}.build()

val client = IgdbClient(IgdbOkhttpEngine) {
    httpClient {
        callFactory = okhttpClient
        backgroundDispatcher = Dispatchers.IO
    }
}
```

## Authentication

### Client to server requests

IGDB API does not support making client-to-server requests directly from the mobile and web applications.
As a workaround, it is proposed to set up a proxy server that makes requests in server-to-server mode.\
IGDB suggests [free AWS CloudFormation template](https://api-docs.igdb.com/#proxy) as one of the implementations of
such a server.

You can set the base URL of the server using the `baseUrl` parameter, and use the `headers`
block to configure custom headers to authenticate to that server:

```kotlin
val client = IgdbClient(IgdbOkhttpEngine) {  
    baseUrl = "https://<your-api-gateway-unique-id>.execute-api.us-west-2.amazonaws.com/production/v4/"

    headers {
        append("x-api-key", "PROXY_API_KEY")
    }
}
```

### Server to server requests

IGDB API requires authentication using the [Twitch OAuth Client Credentials Flow](https://dev.twitch.tv/docs/authentication/getting-tokens-oauth/#client-credentials-grant-flow)
to make server-to-server requests. Additional information can be found on the [IGDB documentation](https://api-docs.igdb.com/#account-creation) page.

You can use the library implementation of the authenticator. To do this, add the *twitchAuth* configuration block and
set the authentication parameters:

```kotlin
val client = IgdbClient(IgdbOkhttpEngine) {
    twitchAuth {
        clientId = "app’s registered client ID"
        clientSecret = "app’s registered client secret."
    }
}
```

Alternatively, you can implement authentication yourself and pass the received token in the *headers*:

```kotlin
val client = IgdbClient(IgdbOkhttpEngine) {  
    headers {
        append("Client-ID", "app's Client ID")  
        append("Authorization", "Bearer $received_access_token")  
    }
}
```

The *twitchAuth* block should not be defined in the configuration in this case.

## Other configuration parameters

HTTP *User-Agent* header can be customized using the *userAgent* parameter.\
The `retryPolicy` configuration block is used to set up the request retries on receiving the
`429 Too Many Requests` HTTP error (enabled by default).

Example:

```kotlin
val client = IgdbClient(IgdbOkhttpEngine) {
    userAgent = "game-client"

    retryPolicy {
        enabled = true
        maxRequestRetries = 100
        initialDelay = 50.milliseconds
        delayRange = Duration.ZERO..2.minutes
        jitterFactor = 0.1f
    }
}
```

## Image Builder

`igdbImageUrl` helper function can be used to generate an image URL for a given image type and pixel density according to the documentation https://api-docs.igdb.com/#images.

Example:

```kotlin
val url = igdbImageUrl(
    imageId = "em1y2ugcwy2myuhvb9db",
    imageSize = LOGO_MEDIUM,
    imageFormat = IgdbImageFormat.JPEG,
    size2x = true,
)
// url: https://images.igdb.com/igdb/image/upload/t_logo_med_2x/em1y2ugcwy2myuhvb9db.jpg
```

## R8 / ProGuard

This library does not require any special R8 rules, but the used HTTP client may need its own rules.
Check the setup instructions for your client:

- Okhttp: https://square.github.io/okhttp/features/r8_proguard/
- Ktor: https://ktor.io/docs/client-dependencies.html

## Contributing

Any type of contributions are welcome. Please see [the contribution guide](CONTRIBUTING.md).

[Ktor]: https://ktor.io/
[Okhttp]: https://square.github.io/okhttp/
[IGDB API]: https://api-docs.igdb.com/
[Maven Central]: https://search.maven.org/artifact/ru.pixnews.igdbclient/igdbclient

## License

This library uses the IGDB but is not endorsed or certified by IGDB.\
These services are licensed under Apache 2.0 License.\
Authors and contributors are listed in the [Authors](AUTHORS) file.

```
Copyright 2023 Igdbclient project authors and contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
