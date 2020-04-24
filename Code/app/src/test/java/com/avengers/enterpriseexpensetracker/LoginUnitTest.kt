package com.avengers.enterpriseexpensetracker

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avengers.enterpriseexpensetracker.ui.activity.LoginActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Login screen unit tests.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P], manifest = Config.NONE)
class LoginUnitTest {

    private val loginActivity =
        LoginActivity()

    @Test
    fun emptyEmailWithWhiteSpaceTest() {
        val email = "   "
        assert(!loginActivity.isValidEmail(email))
    }

    @Test
    fun emptyEmailTest() {
        val email = ""
        assert(!loginActivity.isValidEmail(email))
    }

    @Test
    fun invalidEmailTest1() {
        val email = "abc@abc.com@"
        assert(!loginActivity.isValidEmail(email))
    }

    @Test
    fun invalidEmailTest2() {
        assert(!loginActivity.isValidEmail("abcabc.abc.com"))
    }

    @Test
    fun invalidEmailTest3() {
        assert(!loginActivity.isValidEmail("abc@@abc.abc.com"))
    }

    @Test
    fun validEmailTest() {
        val email = "abc@abc.com"
        assert(loginActivity.isValidEmail(email))
    }
}
