package com.huanchengfly.tieba.post.arch

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.WindowSizeClass
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.calculateWindowSizeClass
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccountProvider
import com.huanchengfly.tieba.post.utils.ThemeUtil

abstract class BaseComposeActivityWithParcelable<DATA : Parcelable> :
    BaseComposeActivityWithData<DATA>() {
    abstract val dataExtraKey: String

    override fun parseData(intent: Intent): DATA? {
        return intent.extras?.getParcelable(dataExtraKey)
    }
}

abstract class BaseComposeActivityWithData<DATA> : BaseComposeActivity() {
    var data: DATA? = null

    abstract fun parseData(intent: Intent): DATA?

    override fun onCreate(savedInstanceState: Bundle?) {
        data = parseData(intent)
        super.onCreate(savedInstanceState)
    }

    @Composable
    final override fun Content() {
        data?.let { data ->
            Content(data)
        }
    }

    @Composable
    abstract fun Content(data: DATA)
}

abstract class BaseComposeActivity : BaseActivity() {
    override val isNeedImmersionBar: Boolean = false
    override val isNeedFixBg: Boolean = false
    override val isNeedSetTheme: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            TiebaLiteTheme {
                // TODO: replace to new api
                // val systemUiController = rememberSystemUiController()
                // SideEffect {
                //     systemUiController.apply {
                //         setStatusBarColor(
                //             Color.Transparent,
                //             darkIcons = ThemeUtil.isStatusBarFontDark()
                //         )
                //         setNavigationBarColor(
                //             Color.Transparent,
                //             darkIcons = ThemeUtil.isNavigationBarFontDark(),
                //             navigationBarContrastEnforced = false
                //         )
                //     }
                // }
                val darkTheme = ThemeUtil.isNightMode()
                DisposableEffect(darkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                            detectDarkMode = { darkTheme }
                        ),
                    )
                    onDispose {}
                }

                LaunchedEffect(key1 = "onCreateContent") {
                    onCreateContent()
                }

                LocalAccountProvider {
                    CompositionLocalProvider(
                        LocalWindowSizeClass provides calculateWindowSizeClass(activity = this)
                    ) {
                        Content()
                    }
                }
            }
        }
    }

    /**
     * 在创建内容前执行
     *
     */
    open fun onCreateContent() {}

    @Composable
    abstract fun Content()

    fun handleCommonEvent(event: CommonUiEvent) {
        when (event) {
            is CommonUiEvent.Toast -> {
                Toast.makeText(this, event.message, event.length).show()
            }

            else -> {}
        }
    }

    companion object {
        val LocalWindowSizeClass =
            staticCompositionLocalOf {
                WindowSizeClass.calculateFromSize(DpSize(0.dp, 0.dp))
            }
    }
}

sealed interface CommonUiEvent : UiEvent {
    data object ScrollToTop : CommonUiEvent

    data object NavigateUp : CommonUiEvent

    data class Toast(
        val message: CharSequence,
        val length: Int = android.widget.Toast.LENGTH_SHORT
    ) : CommonUiEvent

    @Composable
    fun BaseViewModel<*, *, *, *>.BindScrollToTopEvent(lazyListState: LazyListState) {
        onEvent<ScrollToTop> {
            lazyListState.scrollToItem(0, 0)
        }
    }
}