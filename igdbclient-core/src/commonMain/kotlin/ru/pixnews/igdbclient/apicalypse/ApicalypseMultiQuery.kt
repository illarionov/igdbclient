/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
