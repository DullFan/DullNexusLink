package com.dullfan.nexuslink.utils.core

import com.dullfan.nexuslink.entity.ContactPersonEntity
import java.text.Collator
import java.util.Locale

object EntityDataProcessing {

    fun contactPersonEntityAddOrUpdate(
        oldDataList: MutableList<ContactPersonEntity>,
        newDataList: List<ContactPersonEntity>
    ): MutableList<ContactPersonEntity> {
        val oldDataMap = oldDataList.associateBy { it.contactId }.toMutableMap()

        newDataList.forEach { newItem ->
            oldDataMap[newItem.contactId] = newItem
        }

        val updatedList = oldDataMap.values.toMutableList()

        nameSort(updatedList)

        return updatedList
    }

    fun nameSort(list: MutableList<ContactPersonEntity>){
        list.sortWith { o1, o2 ->
            Collator.getInstance(Locale.getDefault()).compare(o1.disPlayName, o2.disPlayName)
        }
    }

}