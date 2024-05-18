/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.model

public class IgdbWebhook(
    public val id: IgdbWebhookId,
    public val url: String,
    public val category: String = "",
    public val subCategory: String = "",
    public val active: Boolean = false,
    public val numberOfRetries: Long = 0,
    public val apiKey: String = "",
    public val secret: String = "",
    public val createdAt: Long = 0,
    public val updatedAt: Long = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as IgdbWebhook

        if (id != other.id) {
            return false
        }
        if (url != other.url) {
            return false
        }
        if (category != other.category) {
            return false
        }
        if (subCategory != other.subCategory) {
            return false
        }
        if (active != other.active) {
            return false
        }
        if (numberOfRetries != other.numberOfRetries) {
            return false
        }
        if (apiKey != other.apiKey) {
            return false
        }
        if (secret != other.secret) {
            return false
        }
        if (createdAt != other.createdAt) {
            return false
        }
        if (updatedAt != other.updatedAt) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + subCategory.hashCode()
        result = 31 * result + active.hashCode()
        result = 31 * result + numberOfRetries.hashCode()
        result = 31 * result + apiKey.hashCode()
        result = 31 * result + secret.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }

    override fun toString(): String {
        return "IgdbWebhook(id='$id'" +
                ", url='$url'" +
                ", category='$category'" +
                ", subCategory='$subCategory'" +
                ", active=$active" +
                ", numberOfRetries=$numberOfRetries" +
                ", apiKey=<REDACTED>" +
                ", secret=<REDACTED>" +
                ", createdAt=$createdAt" +
                ", updatedAt=$updatedAt)"
    }
}
