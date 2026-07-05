package com.tftricks.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tftricks.app.MainActivity
import com.tftricks.app.R
import com.tftricks.app.TFTricksApplication
import com.tftricks.app.domain.model.OverlaySettings
import com.tftricks.app.overlay.ui.OverlayContent
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground service hosting the in-game overlay.
 *
 * Renders Compose UI through a [ComposeView] attached directly to the WindowManager,
 * with [OverlayLifecycleOwner] providing the lifecycle/savedstate/viewmodel owners a
 * ComposeView needs outside an Activity. A single window is reused for both states:
 * its LayoutParams morph between the small draggable bubble (wrap-content, not
 * focusable, positioned at the saved edge) and the expanded panel (sized from the
 * user's panel-size setting, focusable so the search field can use the keyboard).
 */
class OverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "overlay"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_STOP = "com.tftricks.app.overlay.action.STOP"

        private val _isRunning = MutableStateFlow(false)

        /** Observed by Home and Overlay Settings for their start/stop toggles. */
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context, Intent(context, OverlayService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main.immediate + serviceJob)

    private lateinit var windowManager: WindowManager
    private val lifecycleOwner = OverlayLifecycleOwner()
    private var overlayView: ComposeView? = null
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var panelState: OverlayPanelState

    private val expanded = MutableStateFlow(false)
    private val settings = MutableStateFlow(OverlaySettings())

    private val container
        get() = (application as TFTricksApplication).container

    override fun onCreate() {
        super.onCreate()
        _isRunning.value = true
        createNotificationChannel()
        startInForeground()

        // Permission can be revoked while the app isn't looking; bail out gracefully.
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        panelState = OverlayPanelState(container, scope)
        lifecycleOwner.create()

        scope.launch {
            container.overlayPrefsRepository.settings.collect { latest ->
                settings.value = latest
                if (overlayView == null) addOverlayView() else applyWindowLayout()
            }
        }
        scope.launch {
            expanded.collect { if (overlayView != null) applyWindowLayout() }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        overlayView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        overlayView = null
        lifecycleOwner.destroy()
        scope.cancel()
        _isRunning.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-clamp the window after rotation / screen size changes.
        if (overlayView != null) applyWindowLayout()
    }

    // ---- Window management -------------------------------------------------

    private fun addOverlayView() {
        layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSLUCENT
        }
        val view = ComposeView(this).apply {
            setContent {
                val isExpanded by expanded.collectAsState()
                val currentSettings by settings.collectAsState()
                OverlayContent(
                    expanded = isExpanded,
                    settings = currentSettings,
                    panelState = panelState,
                    onBubbleTap = { expanded.value = true },
                    onBubbleDrag = ::moveBubbleBy,
                    onBubbleDragEnd = ::snapBubbleToEdge,
                    onCollapse = { expanded.value = false }
                )
            }
        }
        lifecycleOwner.attachTo(view)
        configureLayoutParams()
        windowManager.addView(view, layoutParams)
        overlayView = view
    }

    private fun configureLayoutParams() {
        val current = settings.value
        val metrics = resources.displayMetrics
        layoutParams.alpha = current.opacity
        if (expanded.value) {
            layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            layoutParams.x = 0
            layoutParams.y = (metrics.heightPixels * 0.05f).toInt()
            layoutParams.width = (metrics.widthPixels * 0.94f).toInt()
            layoutParams.height = (metrics.heightPixels * current.panelSize.heightFraction).toInt()
            // Focusable (no NOT_FOCUSABLE flag) so the search field can open the keyboard.
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        } else {
            layoutParams.gravity = Gravity.TOP or Gravity.START
            layoutParams.x = current.buttonX.coerceIn(0, metrics.widthPixels)
            layoutParams.y = current.buttonY.coerceIn(0, metrics.heightPixels)
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
    }

    private fun applyWindowLayout() {
        val view = overlayView ?: return
        configureLayoutParams()
        runCatching { windowManager.updateViewLayout(view, layoutParams) }
    }

    private fun moveBubbleBy(dx: Float, dy: Float) {
        val view = overlayView ?: return
        val metrics = resources.displayMetrics
        val width = view.width.takeIf { it > 0 } ?: 1
        val height = view.height.takeIf { it > 0 } ?: 1
        layoutParams.x = (layoutParams.x + dx.roundToInt())
            .coerceIn(0, metrics.widthPixels - width)
        layoutParams.y = (layoutParams.y + dy.roundToInt())
            .coerceIn(0, metrics.heightPixels - height)
        runCatching { windowManager.updateViewLayout(view, layoutParams) }
    }

    private fun snapBubbleToEdge() {
        val view = overlayView ?: return
        val metrics = resources.displayMetrics
        val width = view.width.takeIf { it > 0 } ?: 1
        val snappedX =
            if (layoutParams.x + width / 2 <= metrics.widthPixels / 2) 0
            else metrics.widthPixels - width
        layoutParams.x = snappedX
        runCatching { windowManager.updateViewLayout(view, layoutParams) }
        val y = layoutParams.y
        scope.launch {
            container.overlayPrefsRepository.setButtonPosition(snappedX, y)
        }
    }

    // ---- Foreground notification -------------------------------------------

    private fun startInForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_overlay)
            .setContentTitle("TFTricks overlay is active")
            .setContentText("The floating button is on screen.")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openAppIntent)
            .addAction(0, "Stop overlay", stopIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Overlay",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps the TFTricks overlay running while you play."
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
