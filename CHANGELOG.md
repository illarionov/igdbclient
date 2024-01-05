# Change Log

## Version 0.5

_2023-01-05_

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
