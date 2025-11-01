package com.example.lostandfound.feature.notification

import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.remote.ApiEndpoints
import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.domain.repository.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.domain.auth.NotificationRequest
import com.example.lostandfound.domain.auth.NotificationResponse
import com.example.lostandfound.domain.auth.NotificationsListResponse
import com.example.lostandfound.feature.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Notifications and Messages
 */
class NotificationViewModel(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    // ============================================
    // State Flows
    // ============================================

    // Create Notification State
    private val _createNotificationState = MutableStateFlow<Resource<NotificationResponse>>(Resource.None)
    val createNotificationState: StateFlow<Resource<NotificationResponse>> = _createNotificationState.asStateFlow()

    // Notifications List State
    private val _notificationsListState = MutableStateFlow<Resource<NotificationsListResponse>>(Resource.None)
    val notificationsListState: StateFlow<Resource<NotificationsListResponse>> = _notificationsListState.asStateFlow()

    // Single Notification State
    private val _notificationDetailState = MutableStateFlow<Resource<NotificationResponse>>(Resource.None)
    val notificationDetailState: StateFlow<Resource<NotificationResponse>> = _notificationDetailState.asStateFlow()

    // Mark as Read State
    private val _markAsReadState = MutableStateFlow<Resource<NotificationResponse>>(Resource.None)
    val markAsReadState: StateFlow<Resource<NotificationResponse>> = _markAsReadState.asStateFlow()

    // Unread Count
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // ============================================
    // API Methods
    // ============================================

    /**
     * Create a new notification
     * POST /api/notifications/
     */
    fun createNotification(
        notificationType: String,
        title: String,
        message: String,
        lostItem: String? = null
    ) {
        val request = NotificationRequest(
            notificationType = notificationType,
            title = title,
            message = message,
            lostItem = lostItem
        )

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.NOTIFICATIONS,
                httpMethod = HttpMethod.POST
            ).collectAsResource<NotificationResponse>(
                onEmit = { result ->
                    _createNotificationState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Send a message to item owner
     * Helper method that creates a notification of type "message"
     */
    fun sendMessageToOwner(
        recipientTitle: String,
        messageTitle: String,
        messageContent: String,
        relatedItemId: String? = null
    ) {
        val fullMessage = "$messageTitle\n\n$messageContent"

        createNotification(
            notificationType = "message",
            title = messageTitle,
            message = fullMessage,
            lostItem = relatedItemId
        )
    }

    /**
     * Get all notifications for current user
     * GET /api/notifications/
     */
    fun getAllNotifications() {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.NOTIFICATIONS,
                httpMethod = HttpMethod.GET
            ).collectAsResource<NotificationsListResponse>(
                onEmit = { result ->
                    _notificationsListState.value = result

                    // Update unread count
                    if (result is Resource.Success) {
                        val unreadNotifications = result.data.results.count { !it.isRead }
                        _unreadCount.value = unreadNotifications
                    }
                },
                useMock = false
            )
        }
    }

    /**
     * Get notification by ID
     * GET /api/notifications/{id}/
     */
    fun getNotificationById(id: String) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.NOTIFICATION_DETAIL.replace("{id}", id),
                httpMethod = HttpMethod.GET
            ).collectAsResource<NotificationResponse>(
                onEmit = { result ->
                    _notificationDetailState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Mark notification as read
     * POST /api/notifications/{id}/mark-read/
     */
    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.MARK_NOTIFICATION_READ.replace("{id}", id),
                httpMethod = HttpMethod.POST
            ).collectAsResource<NotificationResponse>(
                onEmit = { result ->
                    _markAsReadState.value = result

                    // Refresh notifications list to update unread count
                    if (result is Resource.Success) {
                        getAllNotifications()
                    }
                },
                useMock = false
            )
        }
    }

    /**
     * Delete notification
     * DELETE /api/notifications/{id}/
     */
    fun deleteNotification(id: String) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.NOTIFICATION_DETAIL.replace("{id}", id),
                httpMethod = HttpMethod.DELETE
            ).collectAsResource<String>(
                onEmit = { result ->
                    if (result is Resource.Success) {
                        // Refresh notifications list
                        getAllNotifications()
                    }
                },
                useMock = false
            )
        }
    }

    /**
     * Get unread notifications count
     */
    fun getUnreadCount() {
        val currentList = _notificationsListState.value
        if (currentList is Resource.Success) {
            val unreadNotifications = currentList.data.results.count { !it.isRead }
            _unreadCount.value = unreadNotifications
        } else {
            // If list not loaded, fetch it
            getAllNotifications()
        }
    }

    /**
     * Filter notifications by type
     */
    fun filterNotificationsByType(type: String): List<NotificationResponse> {
        val currentList = _notificationsListState.value
        return if (currentList is Resource.Success) {
            currentList.data.results.filter { it.notificationType == type }
        } else {
            emptyList()
        }
    }

    /**
     * Get unread notifications only
     */
    fun getUnreadNotifications(): List<NotificationResponse> {
        val currentList = _notificationsListState.value
        return if (currentList is Resource.Success) {
            currentList.data.results.filter { !it.isRead }
        } else {
            emptyList()
        }
    }

    // ============================================
    // Reset Methods
    // ============================================

    fun resetCreateState() {
        _createNotificationState.value = Resource.None
    }

    fun resetListState() {
        _notificationsListState.value = Resource.None
    }

    fun resetDetailState() {
        _notificationDetailState.value = Resource.None
    }

    fun resetMarkAsReadState() {
        _markAsReadState.value = Resource.None
    }

    fun resetAllStates() {
        resetCreateState()
        resetListState()
        resetDetailState()
        resetMarkAsReadState()
    }
}