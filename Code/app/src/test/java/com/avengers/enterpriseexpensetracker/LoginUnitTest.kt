package com.avengers.enterpriseexpensetracker

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avengers.enterpriseexpensetracker.util.Utility
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Login screen unit tests.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P], manifest = Config.NONE)
class LoginUnitTest {

    @Test
    fun emptyEmailWithWhiteSpaceTest() {
        assert(!Utility.getInstance().isValidEmail("   "))
    }

    @Test
    fun emptyEmailTest() {
        assert(!Utility.getInstance().isValidEmail(""))
    }

    @Test
    fun invalidEmailTest1() {
        assert(!Utility.getInstance().isValidEmail("abc@abc.com@"))
    }

    @Test
    fun invalidEmailTest2() {
        assert(!Utility.getInstance().isValidEmail("abcabc.abc.com"))
    }

    @Test
    fun invalidEmailTest3() {
        assert(!Utility.getInstance().isValidEmail("abc@@abc.abc.com"))
    }

    @Test
    fun validEmailTest() {
        assert(Utility.getInstance().isValidEmail("abc@abc.com"))
    }
}
