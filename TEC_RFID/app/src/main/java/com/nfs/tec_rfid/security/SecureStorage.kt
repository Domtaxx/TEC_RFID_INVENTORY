package com.nfs.tec_rfid.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecureStorage(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(user: String, password: String) {
        sharedPreferences.edit().apply {
            putString("db_user", user)
            putString("db_password", password)
            apply()
        }
    }

    fun getUser(): String? = sharedPreferences.getString("db_user", null)
    fun getPassword(): String? = sharedPreferences.getString("db_password", null)
}
