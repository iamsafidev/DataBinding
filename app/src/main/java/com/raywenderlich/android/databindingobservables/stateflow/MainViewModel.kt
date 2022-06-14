/*
 * Copyright (c) 2021 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.databindingobservables.stateflow

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.databindingobservables.model.PhoneNumber
import com.raywenderlich.android.databindingobservables.model.Session
import com.raywenderlich.android.databindingobservables.utils.getEmailPrefix
import com.raywenderlich.android.databindingobservables.utils.isValidEmail
import kotlinx.coroutines.flow.*
import java.util.*


private const val DEFAULT_FIRST_NAME = ""
private const val DEFAULT_LAST_NAME = ""
private const val DEFAULT_EMAIL = ""
private const val DEFAULT_SHOW_USERNAME = false
private const val DEFAULT_USERNAME = ""
private const val DEFAULT_ENABLE_REGISTRATION = false
private const val TAG = "StateFlow-ViewModel"

class MainViewModel : ViewModel() {

    // TODO: Add first name, last name and email
    val firstName = MutableStateFlow(DEFAULT_FIRST_NAME)
    val lastName = MutableStateFlow(DEFAULT_LAST_NAME)
    val email = MutableStateFlow(DEFAULT_EMAIL)

    // TODO: Add sessions
    val sessions = MutableStateFlow<EnumMap<Session, Boolean>>(EnumMap(Session::class.java)).apply {
        Session.values().forEach { value[it] = false }
    }

    // TODO: Add username
    val showUserName: StateFlow<Boolean> =
        email.mapToStateFlow(::isValidEmail, DEFAULT_SHOW_USERNAME)
    val userName: StateFlow<String> = email.mapToStateFlow(::generateUsername, DEFAULT_USERNAME)

    // TODO: Add a way to enable the registration button
    val enableRegistration: StateFlow<Boolean> = combine(firstName, lastName, email) { _ ->
        isUserInformationValid()
    }.toStateFlow(DEFAULT_ENABLE_REGISTRATION)

    // TODO: Add phone number
    val phoneNumber = PhoneNumber()

    private val _showRegistrationSuccessDialog = MutableStateFlow(false)
    val showRegistrationSuccessDialog: StateFlow<Boolean>
        get() = _showRegistrationSuccessDialog

    /**
     * Callback that's fired when the registration button is clicked.
     * Check out the logs (in Logcat) to see a dump of the user's information.
     */
    fun onRegisterClicked() {
        _showRegistrationSuccessDialog.value = true
        Log.d(TAG, getUserInformation())
    }

    /** Generates a random username from the user's email address. */
    private fun generateUsername(email: String): String {
        val prefix = getEmailPrefix(email)
        val suffix = prefix.length
        return "$prefix$suffix".lowercase()
    }

    /**
     * Returns true if the user's mandatory information is valid, false otherwise.
     * The mandatory user information includes their first name, last name and email address.
     * Everything else is optional.
     */
    private fun isUserInformationValid(): Boolean {
        return firstName.value.isNotBlank()
                && lastName.value.isNotBlank()
                && isValidEmail(email.value)
    }

    private fun getUserInformation(): String {
        return ""
    }

    /** Convenience method to transform a [Flow] to a [StateFlow]. */
    private fun <T> Flow<T>.toStateFlow(initialValue: T): StateFlow<T> {
        return this.stateIn(viewModelScope, SharingStarted.Lazily, initialValue)
    }

    /** Convenience method to map output from a [Flow] and transform it to a [StateFlow]. */
    private fun <T, U> StateFlow<T>.mapToStateFlow(
        mapper: (value: T) -> U,
        initialValue: U
    ): StateFlow<U> {
        return this.map { mapper(it) }.toStateFlow(initialValue)
    }
}
