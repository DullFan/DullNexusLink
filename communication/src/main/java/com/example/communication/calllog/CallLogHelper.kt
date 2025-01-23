package com.example.communication.calllog

import com.google.i18n.phonenumbers.PhoneNumberToCarrierMapper
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder
import java.util.Locale

object CallLogHelper {

    private val carrierMap = mapOf(
        "China Mobile" to "移动",
        "China Unicom" to "联通",
        "China Telecom" to "电信",
    )

    private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    private val carrierMapper: PhoneNumberToCarrierMapper = PhoneNumberToCarrierMapper.getInstance()
    private val geocoder: PhoneNumberOfflineGeocoder = PhoneNumberOfflineGeocoder.getInstance()
    private const val CHINA_LANGUAGE: String = "CN"

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

    /**
     * 获取手机号码归属地
     */
    fun getCarrier(phoneNumber: String): String {
        val referencePhoneNumber = phoneNumberUtil.parse(phoneNumber, CHINA_LANGUAGE)
        val carrierEn = carrierMapper.getNameForNumber(referencePhoneNumber, Locale.CHINA)
        return carrierMap[carrierEn] ?: "未知归属地"
    }

    /**
     * 获取手机号码运营商
     */
    fun getGen(phoneNumber: String): String {
        val referencePhoneNumber = phoneNumberUtil.parse(phoneNumber, CHINA_LANGUAGE)
        val descriptionForNumber =
            geocoder.getDescriptionForNumber(referencePhoneNumber, Locale.CHINA)
        return descriptionForNumber.ifBlank { "未知运营商" }
    }
}