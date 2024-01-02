package com.example.koti.util

import android.util.Patterns

fun validateEmail(email: String): RegisterValidation {
    if (email.isEmpty())
        return RegisterValidation.Failed("Email cannot be empty")
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Wrong email format")

    return RegisterValidation.Success
}

fun validatePassword(password: String, passwordConfirm: String): RegisterValidation {
    if (password.isEmpty())
        return RegisterValidation.Failed("Password cannot be empty")
    if (password.length < 6)
        return RegisterValidation.Failed("Password must contain at least 6 characters")
    if (passwordConfirm.isEmpty())
        return RegisterValidation.Failed("Please confirm your password")
    if (password != passwordConfirm)
        return RegisterValidation.Failed("Passwords are not the same")
    return RegisterValidation.Success
}