package com.focal.app.feature_renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.focal.app.core.model.ArticleDetailModel
import com.focal.app.core.model.ContentBlockModel

@Composable
fun NativeArticleRenderer(
    article: ArticleDetailModel,
    fontSizeDelta: Float,
    onImageClick: (String) -> Unit
) {
    SelectionContainer {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = (22 + fontSizeDelta).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (30 + fontSizeDelta).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!article.authorAvatar.isNullOrBlank()) {
                        AsyncImage(
                            model = article.authorAvatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = article.authorName ?: "原平台发布",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = article.publishTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(article.blocks) { block ->
                when (block) {
                    is ContentBlockModel.Paragraph -> {
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = (15 + fontSizeDelta).sp,
                                lineHeight = (26 + fontSizeDelta).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    is ContentBlockModel.Heading -> {
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = (17 + fontSizeDelta).sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    is ContentBlockModel.Image -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = block.proxyUrl,
                                contentDescription = block.caption,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onImageClick(block.proxyUrl) }
                            )
                            if (!block.caption.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = block.caption,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is ContentBlockModel.Video -> {
                        if (!block.videoTrackUrl.isNullOrBlank()) {
                            NativeDashPlayerView(
                                videoTrackUrl = block.videoTrackUrl,
                                audioTrackUrl = block.audioTrackUrl
                            )
                        } else {
                            NativeVideoBlockView(
                                bvid = block.bvid,
                                streamUrl = block.streamUrl,
                                coverUrl = block.coverUrl
                            )
                        }
                    }

                    is ContentBlockModel.Quote -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = block.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = (14 + fontSizeDelta).sp,
                                    lineHeight = (22 + fontSizeDelta).sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    is ContentBlockModel.Code -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = block.code,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    is ContentBlockModel.Divider -> {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
