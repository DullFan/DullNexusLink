package com.example.communication.calllog

object CallLogQueryFactory {
    fun create(type: QueryType, param: Any? = null): CallLogQuery {
        return when (type) {
            QueryType.DEFAULT, QueryType.INIT -> DefaultCallLogQuery()
            QueryType.BY_PHONE -> {
                require(param is String) { "Phone number must be String" }
                PhoneNumberCallLogQuery(param)
            }

            QueryType.ALL_IDS -> AllIdCallLogQuery()
            QueryType.AFTER_TIME -> {
                require(param is Long) { "Timestamp must be Long" }
                AfterTimestampCallLogQuery(param)
            }
        }
    }
}

enum class QueryType {
    DEFAULT, BY_PHONE, ALL_IDS, AFTER_TIME, INIT
}