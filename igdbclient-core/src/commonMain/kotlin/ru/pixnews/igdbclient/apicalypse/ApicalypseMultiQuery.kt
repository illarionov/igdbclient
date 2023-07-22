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
package ru.pixnews.igdbclient.apicalypse

import ru.pixnews.igdbclient.IgdbEndpoint

/**
 * Builds a new [ApicalypseMultiQuery]
 */
public fun apicalypseMultiQuery(builder: ApicalypseMultiQueryBuilder.() -> Unit): ApicalypseMultiQuery {
    return ApicalypseMultiQueryBuilder().apply(builder).build()
}

/**
 * Apicalypse multi-query.
 *
 * Use [apicalypseMultiQuery] builder function to create and configure a new instance.
 */
public interface ApicalypseMultiQuery : ApicalypseQuery {
    public val subqueries: List<Subquery>

    override fun toString(): String

    public class Subquery(
        public val endpoint: IgdbEndpoint<*>,
        public val resultName: String = "",
        public val query: ApicalypseQuery,
    )
}
