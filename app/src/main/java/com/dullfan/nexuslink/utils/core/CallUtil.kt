package com.dullfan.nexuslink.utils.core

import android.content.ContentResolver
import android.database.Cursor
import android.provider.CallLog
import android.util.Log
import com.dullfan.nexuslink.room.entity.CallLogEntity
import com.google.i18n.phonenumbers.PhoneNumberToCarrierMapper
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 通话记录工具类
 */
object CallLogUtil {

    private val carrierMap = mapOf(
        "China Mobile" to "移动",
        "China Unicom" to "联通",
        "China Telecom" to "电信",
    )

    private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    private val carrierMapper: PhoneNumberToCarrierMapper = PhoneNumberToCarrierMapper.getInstance()
    private val geocoder: PhoneNumberOfflineGeocoder = PhoneNumberOfflineGeocoder.getInstance()
    private const val CHINA_LANGUAGE: String = "CN"

    /**
     * 根据号码查询通话记录
     */
    fun getCallLogByNumber(contentResolver: ContentResolver, number: String): Cursor? {
        val projection = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )

        val selection = "${CallLog.Calls.NUMBER} = ?"
        val selectionArgs = arrayOf(number)

        return contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            CallLog.Calls.DEFAULT_SORT_ORDER
        )
    }

    /**
     * 获取所有通话记录
     */
    suspend fun ContentResolver.queryCallLog(
        callLogData: (map: LinkedHashMap<String, MutableList<CallLogEntity>>, list: MutableList<CallLogEntity>) -> Unit
    ) {
        val startTime = System.currentTimeMillis()

        val linkedHashMap = linkedMapOf<String, MutableList<CallLogEntity>>()
        val callLogList = mutableListOf<CallLogEntity>()

        val cursor = queryCallLogs()

        cursor?.let {
            queryData(cursor,linkedHashMap,callLogList)
            callLogData(linkedHashMap, callLogList)
            val endTime = System.currentTimeMillis()
            Log.d("TAG", "获取通话记录耗时：${endTime - startTime} 毫秒")
        }
    }


    /**
     * 获取指定时间戳以后的通话记录
     */
    suspend fun ContentResolver.queryCallLogAfterTimestamp(
        timestamp: Long,
        callLogData: (map: LinkedHashMap<String, MutableList<CallLogEntity>>, list: MutableList<CallLogEntity>) -> Unit
    ) {
        val startTime = System.currentTimeMillis()

        val linkedHashMap = linkedMapOf<String, MutableList<CallLogEntity>>()
        val callLogList = mutableListOf<CallLogEntity>()

        val selection = "${CallLog.Calls.DATE} > ?"
        val selectionArgs = arrayOf(timestamp.toString())

        val cursor = queryCallLogs(selection, selectionArgs)

        cursor?.let {
            queryData(cursor,linkedHashMap,callLogList)
            callLogData(linkedHashMap, callLogList)
            val endTime = System.currentTimeMillis()
            Log.d("TAG", "获取通话记录耗时：${endTime - startTime} 毫秒")
        }
    }

    private suspend fun ContentResolver.queryCallLogs(
        selection: String? = null,
        selectionArgs: Array<String>? = null
    ): Cursor? {
        val columns = arrayOf(
            CallLog.Calls.CACHED_NAME,  // 通话记录的联系人
            CallLog.Calls.NUMBER,  // 通话记录的电话号码
            CallLog.Calls.DATE,  // 通话记录的日期
            CallLog.Calls.DURATION,  // 通话时长
            CallLog.Calls.TYPE // 通话类型
        )

        return query(
            CallLog.Calls.CONTENT_URI,
            columns,
            selection,
            selectionArgs,
            CallLog.Calls.DEFAULT_SORT_ORDER
        )
    }

    private suspend fun ContentResolver.queryData(
        cursor: Cursor,
        linkedHashMap: LinkedHashMap<String, MutableList<CallLogEntity>>,
        callLogList: MutableList<CallLogEntity>
    ) {
        val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
        val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
        val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
        val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)

        var count = 0
        while (cursor.moveToNext() && count < 20) {
            // 姓名
            val name = cursor.getString(nameIndex)
            // 号码
            val number = cursor.getString(numberIndex)
            // 获取通话日期
            val dateLong = cursor.getLong(dateIndex)
            // 通话日期
            val date: String =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateLong))
            // 通话时间
            val time: String =
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(dateLong))
            // 获取通话时长，单位:秒
            val duration = cursor.getInt(durationIndex)
            // 获取通话类型：1.呼入2.呼出3.未接
            val type = cursor.getInt(typeIndex)
            count++

            val callLogEntity = CallLogEntity(
                phoneNumber = number,
                name = name,
                timestamp = dateLong,
                date = date,
                time = time,
                belongPlace = getCarrier(number),
                netName = getGen(number),
                duration = duration,
                type = type,
            )

            callLogList.add(callLogEntity)
            // 检查手机号是否已在Map中
            if (linkedHashMap.containsKey(number)) {
                // 如果存在，添加到现有的列表中
                linkedHashMap[number]?.add(callLogEntity)
            } else {
                // 如果不存在，新建列表并添加到Map中
                linkedHashMap[number] = mutableListOf(callLogEntity)
            }
        }

        cursor.close()
    }


    // 初始化通话时长
    fun formatDuration(time: Long): String {
        val s = time % 60
        val m = time / 60
        val h = time / 60 / 60
        val sb = StringBuilder()
        if (h > 0) {
            sb.append(h).append("小时")
        }
        if (m > 0) {
            sb.append(m).append("分")
        }
        sb.append(s).append("秒")
        return sb.toString()
    }

    private fun getCarrier(phoneNumber: String): String {
        val referencePhoneNumber = phoneNumberUtil.parse(phoneNumber, CHINA_LANGUAGE)
        val carrierEn = carrierMapper.getNameForNumber(referencePhoneNumber, Locale.ENGLISH)
        return carrierMap[carrierEn] ?: ""
    }

    private fun getGen(phoneNumber: String): String {
        val referencePhoneNumber = phoneNumberUtil.parse(phoneNumber, CHINA_LANGUAGE)
        val descriptionForNumber =
            geocoder.getDescriptionForNumber(referencePhoneNumber, Locale.CHINA)
        return descriptionForNumber.ifBlank { "未知归属地" }
    }
}