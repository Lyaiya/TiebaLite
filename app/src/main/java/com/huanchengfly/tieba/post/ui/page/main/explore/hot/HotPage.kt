package com.huanchengfly.tieba.post.ui.page.main.explore.hot

import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.hasAgree
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.OrangeA700
import com.huanchengfly.tieba.post.ui.common.theme.compose.RedA700
import com.huanchengfly.tieba.post.ui.common.theme.compose.White
import com.huanchengfly.tieba.post.ui.common.theme.compose.Yellow
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.HotTopicListPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.utils.rememberPullToRefreshState
import com.huanchengfly.tieba.post.ui.widgets.compose.Container
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.ProvideContentColor
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalDivider
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalGrid
import com.huanchengfly.tieba.post.ui.widgets.compose.items
import com.huanchengfly.tieba.post.ui.widgets.compose.itemsIndexed
import com.huanchengfly.tieba.post.utils.StringUtil.getShortNumString
import com.ramcosta.composedestinations.annotation.Destination
import io.github.fornewid.placeholder.material3.placeholder

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun HotPage(
    viewModel: HotViewModel = pageViewModel()
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(HotUiIntent.Load)
        viewModel.initialized = true
    }
    onGlobalEvent<GlobalEvent.Refresh>(
        filter = { it.key == "hot" }
    ) {
        viewModel.send(HotUiIntent.Load)
    }
    val navigator = LocalNavigator.current
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::isRefreshing,
        initial = false
    )
    val topicList by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::topicList,
        initial = emptyList()
    )
    val threadList by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::threadList,
        initial = emptyList()
    )
    val tabList by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::tabList,
        initial = emptyList()
    )
    val currentTabCode by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::currentTabCode,
        initial = "all"
    )
    val isLoadingThreadList by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::isLoadingThreadList,
        initial = false
    )
    val pullToRefreshState = rememberPullToRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.send(HotUiIntent.Load) }
    )
    Box(
        modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        MyLazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            if (topicList.isNotEmpty()) {
                item(key = "TopicHeader") {
                    Container {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .padding(horizontal = 16.dp)
                        ) { ChipHeader(text = stringResource(id = R.string.hot_topic_rank)) }
                    }
                }
                item(key = "TopicList") {
                    Container {
                        VerticalGrid(
                            column = 2,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(
                                items = topicList,
                            ) { index, item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        color = when (index) {
                                            0 -> RedA700
                                            1 -> OrangeA700
                                            2 -> Yellow
                                            else -> MaterialTheme.colorScheme.onBackground.copy(
                                                MaterialTheme.colorScheme.onSurfaceVariant.alpha
                                            )
                                        },
                                        fontFamily = FontFamily(
                                            Typeface.createFromAsset(
                                                LocalContext.current.assets,
                                                "bebas.ttf"
                                            )
                                        ),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Text(
                                        text = item.get { topicName },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    when (item.get { tag }) {
                                        2 -> Text(
                                            text = stringResource(id = R.string.topic_tag_hot),
                                            fontSize = 10.sp,
                                            color = White,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(RedA700)
                                                .padding(vertical = 2.dp, horizontal = 4.dp)
                                        )

                                        1 -> Text(
                                            text = stringResource(id = R.string.topic_tag_new),
                                            fontSize = 10.sp,
                                            color = White,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(OrangeA700)
                                                .padding(vertical = 2.dp, horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                            item {
                                ProvideContentColor(color = ExtendedTheme.colorScheme.primary) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                navigator.navigate(HotTopicListPageDestination)
                                            }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.tip_more_topic),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item(key = "TopicDivider") {
                    Container {
                        VerticalDivider(
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 8.dp)
                                .padding(horizontal = 16.dp),
                            thickness = 2.dp
                        )
                    }
                }
            }
            if (threadList.isNotEmpty()) {
                if (tabList.isNotEmpty()) {
                    item(key = "ThreadTabs") {
                        Container {
                            VerticalGrid(
                                column = 5,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ExtendedTheme.colorScheme.background)
                                    .padding(vertical = 8.dp)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                item {
                                    ThreadListTab(
                                        text = stringResource(id = R.string.tab_all_hot_thread),
                                        selected = currentTabCode == "all",
                                        onSelected = {
                                            viewModel.send(
                                                HotUiIntent.RefreshThreadList(
                                                    "all"
                                                )
                                            )
                                        }
                                    )
                                }
                                items(tabList) {
                                    ThreadListTab(
                                        text = it.get { tabName },
                                        selected = currentTabCode == it.get { tabCode },
                                        onSelected = {
                                            viewModel.send(
                                                HotUiIntent.RefreshThreadList(
                                                    it.get { tabCode })
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item(key = "ThreadListTip") {
                    Container(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.hot_thread_rank_rule),
                            color = ExtendedTheme.colorScheme.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
                if (isLoadingThreadList) {
                    items(10) { ThreadListItemPlaceholder() }
                } else {
                    itemsIndexed(
                        items = threadList,
                        key = { _, item -> "Thread_${item.get { threadId }}" }
                    ) { index, item ->
                        Container {
                            FeedCard(
                                item = item,
                                onClick = {
                                    navigator.navigate(
                                        ThreadPageDestination(
                                            threadId = it.id,
                                            threadInfo = it
                                        )
                                    )
                                },
                                onClickReply = {
                                    navigator.navigate(
                                        ThreadPageDestination(
                                            threadId = it.id,
                                            scrollToReply = true
                                        )
                                    )
                                },
                                onAgree = {
                                    viewModel.send(
                                        HotUiIntent.Agree(
                                            threadId = it.threadId,
                                            postId = it.firstPostId,
                                            hasAgree = it.hasAgree
                                        )
                                    )
                                },
                                onClickForum = { navigator.navigate(ForumPageDestination(it.name)) },
                                onClickUser = { navigator.navigate(UserProfilePageDestination(it.id)) },
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val color = when (index) {
                                        0 -> RedA700
                                        1 -> OrangeA700
                                        2 -> Yellow
                                        else -> MaterialTheme.colorScheme.onBackground.copy(
                                            MaterialTheme.colorScheme.onSurfaceVariant.alpha
                                        )
                                    }
                                    Text(
                                        text = "${index + 1}",
                                        color = color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = stringResource(
                                            id = R.string.hot_num,
                                            item.get { hotNum }.getShortNumString()
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = color
                                    )
                                }
                            }
                        }
//                        ThreadListItem(
//                            index = index,
//                            itemHolder = item,
//                            onClick = {
//                                navigator.navigate(
//                                    ThreadPageDestination(
//                                        threadId = it.id,
//                                        threadInfo = it
//                                    )
//                                )
//                            }
//                        )
                    }
                }
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = ExtendedTheme.colorScheme.pullRefreshIndicator,
            contentColor = ExtendedTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ThreadListItemPlaceholder() {
    Row(modifier = Modifier.padding(all = 16.dp)) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "1",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = ExtendedTheme.colorScheme.background,
                modifier = Modifier
                    .padding(top = 3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .wrapContentSize()
                    .placeholder(visible = true)
                    .padding(vertical = 1.dp, horizontal = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(visible = true)
                )
                Text(
                    text = stringResource(id = R.string.hot_num, "666"),
                    style = MaterialTheme.typography.bodySmall,
                    color = ExtendedTheme.colorScheme.textSecondary,
                    modifier = Modifier.placeholder(visible = true)
                )
            }
        }
    }
}

@Composable
private fun ThreadListTab(
    text: String,
    selected: Boolean,
    onSelected: () -> Unit
) {
    val textColor by animateColorAsState(targetValue = if (selected) ExtendedTheme.colorScheme.onAccent else ExtendedTheme.colorScheme.onChip)
    val backgroundColor by animateColorAsState(targetValue = if (selected) ExtendedTheme.colorScheme.primary else ExtendedTheme.colorScheme.chip)
    Text(
        text = text,
        textAlign = TextAlign.Center,
        color = textColor,
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100))
            .background(backgroundColor)
            .clickable(onClick = onSelected)
            .padding(vertical = 4.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}


@Composable
private fun ChipHeader(
    modifier: Modifier = Modifier,
    text: String,
    invert: Boolean = false,
) {
    Text(
        color = if (invert) MaterialTheme.colorScheme.onSecondary else ExtendedTheme.colorScheme.onChip,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .then(modifier)
            .background(color = if (invert) MaterialTheme.colorScheme.secondary else ExtendedTheme.colorScheme.chip)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}