package com.example.communication.utils

import com.example.communication.room.entity.CallLogEntity
import com.example.communication.room.entity.ContactPersonEntity
import java.text.Collator
import java.util.Locale


/**
 * 数据处理
 */
object EntityDataProcessing {

    fun contactPersonEntityAddOrUpdate(
        oldDataList: MutableList<ContactPersonEntity>,
        newDataList: MutableList<ContactPersonEntity>
    ): MutableList<ContactPersonEntity> {
        val oldDataMap = oldDataList.associateBy { it.baseInfo.contactId }.toMutableMap()

        newDataList.forEach { newItem ->
            oldDataMap[newItem.baseInfo.contactId] = newItem
        }

        val updatedList = oldDataMap.values.toMutableList()

        nameSort(updatedList)

        return updatedList
    }

    fun nameSort(list: MutableList<ContactPersonEntity>) {
        list.sortWith { o1, o2 ->
            Collator.getInstance(Locale.getDefault()).compare(o1.baseInfo.disPlayName, o2.baseInfo.disPlayName)
        }
    }

    fun recentSort(list: MutableList<CallLogEntity>) {
        list.sortWith { o1, o2 ->
            val dateCompare = o2.date.compareTo(o1.date)

            if (dateCompare == 0) {
                o2.time.compareTo(o1.time)
            } else {
                dateCompare
            }
        }
    }
}