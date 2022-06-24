package com.keyinvalidationtest

import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.DIGEST_SHA256
import android.security.keystore.KeyProperties.PURPOSE_SIGN
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import java.security.*
import java.security.interfaces.ECKey
import java.security.spec.ECGenParameterSpec
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.manufacturer).text = Build.MANUFACTURER
        findViewById<TextView>(R.id.model).text = Build.MODEL

        findViewById<Button>(R.id.createButton).setOnClickListener {
            if (getPrivateKey() == null) {
                try {
                    generateKeyPair()
                    snack("Key created")
                } catch (e: Exception) {
                    snack(e)
                }
            } else {
                snack("Key already exists")
            }
        }

        findViewById<Button>(R.id.useButton).setOnClickListener {
            if (getPrivateKey() != null) {
                sign()
            } else {
                snack("No key found")
            }
        }

        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            if (getPrivateKey() != null) {
                deleteKey()
                snack("Key deleted")
            } else {
                snack("No key found")
            }
        }
    }

    private fun sign() {
        try {
            val privateKey = getPrivateKey()
            signature.initSign(privateKey as PrivateKey)

            BiometricPrompt(this, ContextCompat.getMainExecutor(this),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        try {
                            signature.update(Byte.MAX_VALUE)
                            val signed = signature.sign()
                            snack("Key has been used to sign")
                        } catch (e: java.lang.Exception) {
                            snack(e)
                        }
                    }
                }
            )
                .authenticate(
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Hello")
                        .setNegativeButtonText("Cancel")
                        .setConfirmationRequired(false)
                        .build(),
                    BiometricPrompt.CryptoObject(signature)
                )
        } catch (e: Exception) {
            snack(e)
        }
    }

    private fun generateKeyPair(): KeyPair =
        KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        ).apply {
            setAlias()
            initialize(
                KeyGenParameterSpec.Builder(getAlias(), PURPOSE_SIGN)
                    .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                    .setDigests(DIGEST_SHA256)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(true)
                    .setIsStrongBoxBacked(true)
                    .build()
            )
        }
            .generateKeyPair()

    private fun getPrivateKey(): ECKey? =
        getKeyStore().getKey(getAlias(), null) as ECKey?

    private fun deleteKey() =
        getKeyStore().deleteEntry(getAlias())

    private fun getKeyStore(): KeyStore =
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

    private fun snack(exception: Exception) {
        Log.e(null, null, exception)
        snack("Error: ${exception.message}")
    }

    private fun snack(message: String) =
        Snackbar.make(findViewById(R.id.content), message, LENGTH_SHORT).show()

    private fun setAlias() {
        val uuid = UUID.randomUUID()
        getPreferences(MODE_PRIVATE)
            .edit()
            .putString(alias, uuid.toString())
            .apply()
    }

    private fun getAlias(): String {
        val alias = getPreferences(MODE_PRIVATE).getString(alias, "asdf")!!
        Log.i(null, "Alias: $alias")
        return alias
    }

    companion object {
        const val alias = "the_alias"
        val signature: Signature = Signature.getInstance("SHA256withECDSA")
    }
}
