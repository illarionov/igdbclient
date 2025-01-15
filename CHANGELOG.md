# Changelog

## [0.7] — 2025-01-15

### Added

- ["GameTimeToBeat"](https://api-docs.igdb.com/#game-time-to-beat) endpoint.

### Changed

- Package and maven coordinates have been changed.
  Replace the prefix "ru.pixnews.igdbclient" in all imports to "at.released.igdbclient".  
  New maven group: "at.released.igdbclient"

- Bump dependency versions:
    * AGP 8.7.3
    * Kotlin 2.1.0
    * AtomicFU 0.27.0
    * Wire 5.2.1
    * Okio 3.10.2
    * Ktor 3.0.3
    * orgjson 20250107
    * Kotlinx.serialization 1.8.0
    * kotlinx-coroutines 1.10.1
    * Other internal dependencies

## [0.6] — 2024-07-03

### Added

- ["Popularity Primitives"](https://api-docs.igdb.com/#popularity-primitive) and
  ["Popularity Types"](https://api-docs.igdb.com/#popularity-type) endpoints.

### Changed

- Bump dependency versions:
  * AGP 8.2.2
  * Kotlin 2.0.0
  * Wire 4.9.9
  * Okio 3.9.0
  * Ktor 2.3.12
  * orgjson 20240303
  * Kotlinx.serialization 1.7.1
  * kotlinx-coroutines 1.8.1
  * AtomicFU 0.25.0
  * Other internal dependencies

## Version 0.5

_2024-01-05_

* New: experimental Dumps API support
* Upgrade: Bump dependency versions
  * Kotlin 1.9.22
  * Kotlinx.serialization 1.6.2
  * Ktor 2.3.7
  * Okio 3.7.0
  * AGP 8.2.1

## Version 0.4

_2023-11-25_

* Add new endpoints: collection types, memberships and relations
* Upgrade: Bump dependency versions
  * Kotlin 1.9.21
  * Kotlinx.serialization 1.6.1
  * Ktor 2.3.6
  * Wire 4.9.3
  * AGP 8.1.4
  * orgjson 20231013

## Version 0.3

_2023-10-11_

 * New: Support for IGDB Gaming Events API
 * New: DSL for forming a list of requested fields (`fields(Game.field.all)`)
 * Fix: Restored Dokka documentation
 * Upgrade: Bump dependency versions
   * Kotlin 1.9.10
   * Kotlinx.serialization 1.6.0
   * Ktor 2.3.5
   * Okio 3.6.0
   * Wire 4.9.1

## Version 0.2

_2023-08-31_

 * Fix: Disabled "where" clause escaping (Issue #31 by @maicol07)
 * Upgrade: Bump dependency versions
   * okio 3.5.0
   * ktor 2.3.3
   * kotlinx-coroutines 1.7.3

## Version 0.1

_2023-07-24_

 * Initial release. API is subject to change.
