package com.truetap.solana.seeker.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.truetap.solana.seeker.ui.screens.contacts.ModernContact
import com.truetap.solana.seeker.ui.screens.contacts.ContactWallet
import com.truetap.solana.seeker.data.models.WalletType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.contactsDataStore by preferencesDataStore(name = "contacts_prefs")

@Serializable
data class SerializableContact(
    val id: String,
    val name: String,
    val initials: String,
    val wallets: List<SerializableContactWallet>,
    val isFavorite: Boolean = false
)

@Serializable
data class SerializableContactWallet(
    val id: String,
    val name: String,
    val address: String,
    val type: String // Serialize enum as string
)

@Singleton
class ContactsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private val CONTACTS_KEY = stringPreferencesKey("contacts_json")
    }

    fun getContactsFlow(): Flow<List<ModernContact>> = context.contactsDataStore.data.map { prefs ->
        val contactsJson = prefs[CONTACTS_KEY] ?: "[]"
        try {
            val serializableContacts = json.decodeFromString<List<SerializableContact>>(contactsJson)
            serializableContacts.map { serializableContact ->
                ModernContact(
                    id = serializableContact.id,
                    name = serializableContact.name,
                    initials = serializableContact.initials,
                    wallets = serializableContact.wallets.map { serializableWallet ->
                        ContactWallet(
                            id = serializableWallet.id,
                            name = serializableWallet.name,
                            address = serializableWallet.address,
                            type = try {
                                WalletType.valueOf(serializableWallet.type)
                            } catch (e: Exception) {
                                WalletType.PERSONAL // Default fallback
                            }
                        )
                    },
                    isFavorite = serializableContact.isFavorite
                )
            }
        } catch (e: Exception) {
            // If parsing fails, return empty list
            emptyList()
        }
    }

    suspend fun getContacts(): List<ModernContact> {
        return getContactsFlow().first()
    }

    suspend fun addContact(contact: ModernContact) {
        val currentContacts = getContacts().toMutableList()
        currentContacts.add(contact)
        saveContacts(currentContacts)
    }

    suspend fun updateContact(contact: ModernContact) {
        val currentContacts = getContacts().toMutableList()
        val index = currentContacts.indexOfFirst { it.id == contact.id }
        if (index != -1) {
            currentContacts[index] = contact
            saveContacts(currentContacts)
        }
    }

    suspend fun deleteContact(contactId: String) {
        val currentContacts = getContacts().toMutableList()
        currentContacts.removeAll { it.id == contactId }
        saveContacts(currentContacts)
    }

    suspend fun addWalletToContact(contactId: String, wallet: ContactWallet) {
        val currentContacts = getContacts().toMutableList()
        val contactIndex = currentContacts.indexOfFirst { it.id == contactId }
        if (contactIndex != -1) {
            val contact = currentContacts[contactIndex]
            val updatedWallets = contact.wallets.toMutableList()
            updatedWallets.add(wallet)
            currentContacts[contactIndex] = contact.copy(wallets = updatedWallets)
            saveContacts(currentContacts)
        }
    }

    suspend fun updateWalletInContact(contactId: String, wallet: ContactWallet) {
        val currentContacts = getContacts().toMutableList()
        val contactIndex = currentContacts.indexOfFirst { it.id == contactId }
        if (contactIndex != -1) {
            val contact = currentContacts[contactIndex]
            val updatedWallets = contact.wallets.toMutableList()
            val walletIndex = updatedWallets.indexOfFirst { it.id == wallet.id }
            if (walletIndex != -1) {
                updatedWallets[walletIndex] = wallet
                currentContacts[contactIndex] = contact.copy(wallets = updatedWallets)
                saveContacts(currentContacts)
            }
        }
    }

    suspend fun deleteWalletFromContact(contactId: String, walletId: String) {
        val currentContacts = getContacts().toMutableList()
        val contactIndex = currentContacts.indexOfFirst { it.id == contactId }
        if (contactIndex != -1) {
            val contact = currentContacts[contactIndex]
            val updatedWallets = contact.wallets.toMutableList()
            updatedWallets.removeAll { it.id == walletId }
            currentContacts[contactIndex] = contact.copy(wallets = updatedWallets)
            saveContacts(currentContacts)
        }
    }

    private suspend fun saveContacts(contacts: List<ModernContact>) {
        val serializableContacts = contacts.map { contact ->
            SerializableContact(
                id = contact.id,
                name = contact.name,
                initials = contact.initials,
                wallets = contact.wallets.map { wallet ->
                    SerializableContactWallet(
                        id = wallet.id,
                        name = wallet.name,
                        address = wallet.address,
                        type = wallet.type.name
                    )
                },
                isFavorite = contact.isFavorite
            )
        }
        
        val contactsJson = json.encodeToString(serializableContacts)
        context.contactsDataStore.edit { prefs ->
            prefs[CONTACTS_KEY] = contactsJson
        }
    }
    
    suspend fun initializeSampleDataIfEmpty() {
        val currentContacts = getContacts()
        if (currentContacts.isEmpty()) {
            val sampleContacts = listOf(
                ModernContact(
                    id = "1",
                    name = "Charlie Brown",
                    initials = "CB",
                    wallets = listOf(
                        ContactWallet("1", "Main Wallet", "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM", WalletType.PERSONAL),
                        ContactWallet("2", "Savings", "8VzCXwBbmkg9ZTbNMqUxvQRAyrZzDsGYdLVL9zYtBXXN", WalletType.PERSONAL)
                    ),
                    isFavorite = true
                ),
                ModernContact(
                    id = "2",
                    name = "Diana Prince",
                    initials = "DP",
                    wallets = listOf(
                        ContactWallet("3", "Business", "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU", WalletType.BUSINESS)
                    ),
                    isFavorite = true
                ),
                ModernContact(
                    id = "3",
                    name = "Alice Johnson",
                    initials = "AJ",
                    wallets = listOf(
                        ContactWallet("4", "Personal", "5dSHdvJBQ38YuuHdKHDHFLhMhLCvdV7xB5QH5Y8z9CXD", WalletType.PERSONAL)
                    ),
                    isFavorite = false
                ),
                ModernContact(
                    id = "4",
                    name = "Bob Smith",
                    initials = "BS",
                    wallets = listOf(
                        ContactWallet("5", "Trading", "3mKQrv8fHpPQHhcQdAKYzuG8SN7eCPThjGhNkEKvXX5C", WalletType.PERSONAL)
                    ),
                    isFavorite = false
                )
            )
            saveContacts(sampleContacts)
        }
    }
}