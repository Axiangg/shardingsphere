/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package io.shardingjdbc.proxy.transport.mysql.packet.command.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prepared statement registry.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class PreparedStatementRegistry {
    
    private static final PreparedStatementRegistry INSTANCE = new PreparedStatementRegistry();
    
    private final ConcurrentMap<String, Integer> sqlToNumColumnsMap = new ConcurrentHashMap<>(65535, 1);
    
    private final ConcurrentMap<Integer, String> statementIdtoSqlMap = new ConcurrentHashMap<>(65535, 1);
    
    private final AtomicInteger sequence = new AtomicInteger();
    
    /**
     * Get prepared statement registry instance.
     * 
     * @return prepared statement registry instance
     */
    public static PreparedStatementRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register SQL.
     * 
     * @param sql SQL
     * @param numColumns num columns
     * @return statement ID
     */
    public int register(final String sql, final int numColumns) {
        Integer result = sqlToNumColumnsMap.get(sql);
        if (null != result) {
            return result;
        }
        int statementId;
        do {
            statementId = sequence.incrementAndGet();
        } while (null != statementIdtoSqlMap.putIfAbsent(statementId, sql));
        sqlToNumColumnsMap.putIfAbsent(sql, numColumns);
        return statementId;
    }
    
    /**
     * Get statement ID.
     *
     * @param sql SQL
     * @return statement ID
     */
    public int getStatementId(final String sql) {
        return sqlToNumColumnsMap.get(sql);
    }
    
    /**
     * Get SQL.
     *
     * @param statementId statement ID
     * @return SQL
     */
    public String getSql(final int statementId) {
        return statementIdtoSqlMap.get(statementId);
    }
    
    /**
     * Get number columns.
     *
     * @param statementId statement ID
     * @return number columns
     */
    public int getNumColumns(final int statementId) {
        String sql = statementIdtoSqlMap.get(statementId);
        return sqlToNumColumnsMap.get(sql);
    }
}
