package io.metamask.androidsdk

import io.metamask.androidsdk.KeyExchangeMessageType.*

data class KeyExchangeMessage(
    val type: String,
    val publicKey: String?
    )

internal class KeyExchange(private val crypto: Crypto = Crypto()) {
    companion object {
        const val TYPE = "type"
        const val PUBLIC_KEY = "public_key"
    }

    private var privateKey: String? = null
    var publicKey: String? = null
    private var theirPublicKey: String? = null
    var keysExchanged = false

    init {
        generateNewKeys()
    }

    fun generateNewKeys() {
        privateKey = crypto.generatePrivateKey()
        privateKey?.let {
            publicKey = crypto.publicKey(it)
        }
        keysExchanged = false
        theirPublicKey = null
    }

    fun encrypt(message: String): String {
        val key: String = theirPublicKey ?: throw NullPointerException("theirPublicKey is null")
        return crypto.encrypt(key, message)
    }

    fun decrypt(message: String): String {
        val key: String = privateKey ?: throw NullPointerException("privateKey is null")
        return crypto.decrypt(key, message)
    }

    fun complete() {
        Logger.log("Key exchange complete")
        keysExchanged = true
    }

    fun nextKeyExchangeMessage(current: KeyExchangeMessage): KeyExchangeMessage? {
        current.publicKey?.let {
            theirPublicKey = it
        }

        return when(current.type) {
            KEY_HANDSHAKE_START.name -> KeyExchangeMessage(KEY_HANDSHAKE_SYN.name, publicKey)
            KEY_HANDSHAKE_SYN.name -> KeyExchangeMessage(KEY_HANDSHAKE_SYNACK.name, publicKey)
            KEY_HANDSHAKE_SYNACK.name -> KeyExchangeMessage(KEY_HANDSHAKE_ACK.name, publicKey)
            KEY_HANDSHAKE_ACK.name -> null
            else -> null
        }
    }
}