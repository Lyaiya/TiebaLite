package com.huanchengfly.tieba.post.ui.page.search.thread

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.page.search.SearchUiEvent
import com.huanchengfly.tieba.post.ui.utils.rememberPullToRefreshState
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalShouldLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.SearchThreadList
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchThreadPage(
    keyword: String,
    initialSortType: Int = SearchThreadSortType.SORT_TYPE_NEWEST,
    viewModel: SearchThreadViewModel = pageViewModel(),
) {
    val navigator = LocalNavigator.current
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(SearchThreadUiIntent.Refresh(keyword, initialSortType))
        viewModel.initialized = true
    }
    val currentKeyword by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::keyword,
        initial = ""
    )
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::isRefreshing,
        initial = true
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::isLoadingMore,
        initial = false
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::error,
        initial = null
    )
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::data,
        initial = persistentListOf()
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::currentPage,
        initial = 1
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::hasMore,
        initial = true
    )
    val sortType by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::sortType,
        initial = initialSortType
    )

    onGlobalEvent<SearchThreadUiEvent.SwitchSortType> {
        viewModel.send(SearchThreadUiIntent.Refresh(keyword, it.sortType))
    }
    val shouldLoad = LocalShouldLoad.current
    LaunchedEffect(currentKeyword) {
        if (currentKeyword.isNotEmpty() && keyword != currentKeyword) {
            if (shouldLoad) {
                viewModel.send(SearchThreadUiIntent.Refresh(keyword, sortType))
            } else {
                viewModel.initialized = false
            }
        }
    }

    val pullToRefreshState = rememberPullToRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(SearchThreadUiIntent.Refresh(keyword, sortType)) }
    )
    val lazyListState = rememberLazyListState()

    val isEmpty by remember {
        derivedStateOf { data.isEmpty() }
    }

    onGlobalEvent<SearchUiEvent.KeywordChanged> {
        viewModel.send(SearchThreadUiIntent.Refresh(it.keyword, sortType))
    }

    StateScreen(
        modifier = Modifier.fillMaxSize(),
        isEmpty = isEmpty,
        isError = error != null,
        isLoading = isRefreshing,
        onReload = { viewModel.send(SearchThreadUiIntent.Refresh(keyword, sortType)) },
        errorScreen = {
            error?.let {
                val (e) = it
                ErrorScreen(error = e)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            LoadMoreLayout(
                isLoading = isLoadingMore,
                onLoadMore = {
                    viewModel.send(
                        SearchThreadUiIntent.LoadMore(keyword, currentPage, sortType)
                    )
                },
                loadEnd = !hasMore,
                lazyListState = lazyListState,
            ) {
                SearchThreadList(
                    data = data,
                    lazyListState = lazyListState,
                    onItemClick = {
                        navigator.navigate(
                            ThreadPageDestination(
                                threadId = it.tid.toLong()
                            )
                        )
                    },
                    onItemUserClick = {
                        navigator.navigate(UserProfilePageDestination(it.userId.toLong()))
                    },
                    onItemForumClick = {
                        navigator.navigate(
                            ForumPageDestination(
                                it.forumName
                            )
                        )
                    },
                    searchKeyword = keyword,
                )

                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = ExtendedTheme.colorScheme.pullRefreshIndicator,
                    contentColor = ExtendedTheme.colorScheme.primary,
                )
            }
        }
    }
}
