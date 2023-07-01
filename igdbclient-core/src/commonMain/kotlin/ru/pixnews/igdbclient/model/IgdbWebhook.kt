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
