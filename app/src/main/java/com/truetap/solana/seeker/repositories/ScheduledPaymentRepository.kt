package com.truetap.solana.seeker.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.truetap.solana.seeker.domain.model.ScheduledPayment
import com.truetap.solana.seeker.domain.model.PaymentStatus
import com.truetap.solana.seeker.domain.model.RepeatInterval
import com.truetap.solana.seeker.workers.ScheduledPaymentWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scheduled_payments")

@Singleton
class ScheduledPaymentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val dataStore = context.dataStore
    
    companion object {
        private val PAYMENT_ID = stringPreferencesKey("payment_id")
        private val RECIPIENT_ADDRESS = stringPreferencesKey("recipient_address")
        private val RECIPIENT_NAME = stringPreferencesKey("recipient_name")
        private val AMOUNT = stringPreferencesKey("amount")
        private val TOKEN = stringPreferencesKey("token")
        private val MEMO = stringPreferencesKey("memo")
        private val START_DATE = stringPreferencesKey("start_date")
        private val NEXT_EXECUTION_DATE = stringPreferencesKey("next_execution_date")
        private val REPEAT_INTERVAL = stringPreferencesKey("repeat_interval")
        private val MAX_EXECUTIONS = intPreferencesKey("max_executions")
        private val CURRENT_EXECUTIONS = intPreferencesKey("current_executions")
        private val STATUS = stringPreferencesKey("status")
        private val CREATED_AT = stringPreferencesKey("created_at")
        private val LAST_EXECUTED_AT = stringPreferencesKey("last_executed_at")
        private val FAILURE_REASON = stringPreferencesKey("failure_reason")
        
        private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }
    
    suspend fun createScheduledPayment(
        recipientAddress: String,
        recipientName: String?,
        amount: BigDecimal,
        token: String,
        memo: String?,
        startDate: LocalDateTime,
        repeatInterval: RepeatInterval,
        maxExecutions: Int? = null
    ): ScheduledPayment {
        val payment = ScheduledPayment(
            id = UUID.randomUUID().toString(),
            recipientAddress = recipientAddress,
            recipientName = recipientName,
            amount = amount,
            token = token,
            memo = memo,
            startDate = startDate,
            nextExecutionDate = startDate,
            repeatInterval = repeatInterval,
            maxExecutions = maxExecutions,
            currentExecutions = 0,
            status = PaymentStatus.PENDING,
            createdAt = LocalDateTime.now(),
            lastExecutedAt = null,
            failureReason = null
        )
        
        savePayment(payment)
        
        // Enqueue the first execution
        ScheduledPaymentWorker.enqueue(context, payment)
        
        return payment
    }
    
    suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus, failureReason: String? = null) {
        dataStore.edit { preferences ->
            val paymentKey = stringPreferencesKey("payment_${paymentId}_status")
            val reasonKey = stringPreferencesKey("payment_${paymentId}_failure_reason")
            
            preferences[paymentKey] = status.name
            if (failureReason != null) {
                preferences[reasonKey] = failureReason
            }
        }
    }
    
    suspend fun incrementExecutions(paymentId: String) {
        dataStore.edit { preferences ->
            val executionsKey = intPreferencesKey("payment_${paymentId}_current_executions")
            val current = preferences[executionsKey] ?: 0
            preferences[executionsKey] = current + 1
        }
    }
    
    suspend fun scheduleNextExecution(paymentId: String) {
        val payment = getPayment(paymentId) ?: return
        
        if (payment.repeatInterval == RepeatInterval.NONE || 
            (payment.maxExecutions != null && payment.currentExecutions >= payment.maxExecutions)) {
            // Mark as completed
            updatePaymentStatus(paymentId, PaymentStatus.COMPLETED)
            return
        }
        
        // Calculate next execution date
        val nextExecution = calculateNextExecutionDate(payment)
        
        // Update next execution date
        dataStore.edit { preferences ->
            val nextDateKey = stringPreferencesKey("payment_${paymentId}_next_execution_date")
            preferences[nextDateKey] = nextExecution.format(dateFormatter)
        }
        
        // Enqueue next execution
        ScheduledPaymentWorker.enqueueRecurring(context, payment.copy(nextExecutionDate = nextExecution))
    }
    
    suspend fun pausePayment(paymentId: String) {
        // Cancel the current WorkManager work
        WorkManager.getInstance(context).cancelUniqueWork("scheduled_payment_$paymentId")
        
        // Update status to indicate paused (could add PAUSED to PaymentStatus enum)
        updatePaymentStatus(paymentId, PaymentStatus.CANCELLED)
    }
    
    suspend fun resumePayment(paymentId: String) {
        val payment = getPayment(paymentId) ?: return
        
        if (payment.status == PaymentStatus.CANCELLED) {
            // Re-enqueue the payment
            ScheduledPaymentWorker.enqueueRecurring(context, payment)
            updatePaymentStatus(paymentId, PaymentStatus.PENDING)
        }
    }
    
    suspend fun cancelPayment(paymentId: String) {
        // Cancel the WorkManager work
        WorkManager.getInstance(context).cancelUniqueWork("scheduled_payment_$paymentId")
        
        // Mark as cancelled
        updatePaymentStatus(paymentId, PaymentStatus.CANCELLED)
    }
    
    fun getAllPayments(): Flow<List<ScheduledPayment>> {
        return dataStore.data.map { preferences ->
            // This is a simplified implementation
            // In production, you'd want to store multiple payments more efficiently
            // For now, return empty list as placeholder
            emptyList()
        }
    }
    
    fun getActivePayments(): Flow<List<ScheduledPayment>> {
        return getAllPayments().map { payments ->
            payments.filter { it.status == PaymentStatus.PENDING }
        }
    }
    
    private suspend fun savePayment(payment: ScheduledPayment) {
        dataStore.edit { preferences ->
            val prefix = "payment_${payment.id}_"
            
            preferences[stringPreferencesKey("${prefix}recipient_address")] = payment.recipientAddress
            payment.recipientName?.let { 
                preferences[stringPreferencesKey("${prefix}recipient_name")] = it 
            }
            preferences[stringPreferencesKey("${prefix}amount")] = payment.amount.toString()
            preferences[stringPreferencesKey("${prefix}token")] = payment.token
            payment.memo?.let { 
                preferences[stringPreferencesKey("${prefix}memo")] = it 
            }
            preferences[stringPreferencesKey("${prefix}start_date")] = payment.startDate.format(dateFormatter)
            preferences[stringPreferencesKey("${prefix}next_execution_date")] = payment.nextExecutionDate.format(dateFormatter)
            preferences[stringPreferencesKey("${prefix}repeat_interval")] = payment.repeatInterval.name
            payment.maxExecutions?.let { 
                preferences[intPreferencesKey("${prefix}max_executions")] = it 
            }
            preferences[intPreferencesKey("${prefix}current_executions")] = payment.currentExecutions
            preferences[stringPreferencesKey("${prefix}status")] = payment.status.name
            preferences[stringPreferencesKey("${prefix}created_at")] = payment.createdAt.format(dateFormatter)
            payment.lastExecutedAt?.let { 
                preferences[stringPreferencesKey("${prefix}last_executed_at")] = it.format(dateFormatter) 
            }
            payment.failureReason?.let { 
                preferences[stringPreferencesKey("${prefix}failure_reason")] = it 
            }
        }
    }
    
    private suspend fun getPayment(paymentId: String): ScheduledPayment? {
        // This is a simplified implementation
        // In production, you'd want to retrieve the full payment object
        return null
    }
    
    private fun calculateNextExecutionDate(payment: ScheduledPayment): LocalDateTime {
        return when (payment.repeatInterval) {
            RepeatInterval.DAILY -> payment.nextExecutionDate.plusDays(1)
            RepeatInterval.WEEKLY -> payment.nextExecutionDate.plusWeeks(1)
            RepeatInterval.MONTHLY -> payment.nextExecutionDate.plusMonths(1)
            RepeatInterval.NONE -> payment.nextExecutionDate
        }
    }
}
