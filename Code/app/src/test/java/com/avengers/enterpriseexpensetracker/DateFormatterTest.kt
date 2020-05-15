package com.avengers.enterpriseexpensetracker

import com.avengers.enterpriseexpensetracker.util.EETrackerDateFormatManager
import org.junit.Assert
import org.junit.Test

class DateFormatterTest {

    private val dateFormatMgr = EETrackerDateFormatManager()

    @Test
    fun parseMonthDateOrdinalTest() {
        val answer = dateFormatMgr.parseDate("January 3rd 2020")
        Assert.assertEquals("01/03/2020", answer)
    }

    @Test
    fun parseMonthDateWithoutOrdinalTest() {
        val answer = dateFormatMgr.parseDate("January 3 2020")
        Assert.assertEquals("01/03/2020", answer)
    }

    @Test
    fun parseDateWithoutOrdinalMonthTest() {
        val answer = dateFormatMgr.parseDate("12 January 2020")
        Assert.assertEquals("01/12/2020", answer)
    }

    @Test
    fun parseDateWithOrdinalMonthTest() {
        val answer = dateFormatMgr.parseDate("14th January 2020")
        Assert.assertEquals("01/14/2020", answer)
    }

    @Test
    fun parseDateWithOrdinalSmallMonthTest() {
        val answer = dateFormatMgr.parseDate("1st Jan 2020")
        Assert.assertEquals("01/01/2020", answer)
    }

    @Test
    fun parseSmallMonthDateWithOrdinalTest() {
        val answer = dateFormatMgr.parseDate("Feb 22nd 2020")
        Assert.assertEquals("02/22/2020", answer)
    }

    @Test
    fun parseSmallMonthDateWithoutOrdinalTest() {
        val answer = dateFormatMgr.parseDate("Feb 22 2020")
        Assert.assertEquals("02/22/2020", answer)
    }

    @Test
    fun parseDateWithoutOrdinalSmallMonthTest() {
        val answer = dateFormatMgr.parseDate("13 Jan 2020")
        Assert.assertEquals("01/13/2020", answer)
    }
}