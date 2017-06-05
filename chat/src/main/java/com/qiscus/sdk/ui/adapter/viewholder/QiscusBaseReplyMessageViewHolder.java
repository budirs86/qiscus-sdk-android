/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.ui.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.adapter.ReplyItemClickListener;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusImageUtil;

import java.io.File;


/**
 * Created on : June 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseReplyMessageViewHolder extends QiscusBaseTextMessageViewHolder {
    @NonNull protected ViewGroup originMessageView;
    @NonNull protected TextView originSenderTextView;
    @NonNull protected TextView originMessageTextView;
    @Nullable protected View barView;
    @Nullable protected ImageView originIconView;
    @Nullable protected ImageView originImageView;

    protected int barColor;
    protected int originSenderColor;
    protected int originMessageColor;

    private ReplyItemClickListener replyItemClickListener;

    public QiscusBaseReplyMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                            OnLongItemClickListener longItemClickListener,
                                            ReplyItemClickListener replyItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        this.replyItemClickListener = replyItemClickListener;
        originMessageView = getOriginMessageView(itemView);
        originSenderTextView = getOriginSenderTextView(itemView);
        originMessageTextView = getOriginMessageTextView(itemView);
        barView = getBarView(itemView);
        originIconView = getOriginIconView(itemView);
        originImageView = getOriginImageView(itemView);
    }

    @NonNull
    protected abstract ViewGroup getOriginMessageView(View itemView);

    @NonNull
    protected abstract TextView getOriginSenderTextView(View itemView);

    @NonNull
    protected abstract TextView getOriginMessageTextView(View itemView);

    @Nullable
    protected abstract View getBarView(View itemView);

    @Nullable
    protected abstract ImageView getOriginIconView(View itemView);

    @Nullable
    protected abstract ImageView getOriginImageView(View itemView);

    @Override
    protected void loadChatConfig() {
        super.loadChatConfig();
        barColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getReplyBarColor());
        originSenderColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getReplySenderColor());
        originMessageColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getReplyMessageColor());
    }

    @Override
    protected void setUpColor() {
        super.setUpColor();
        if (barView != null) {
            barView.setBackgroundColor(barColor);
        }
        originSenderTextView.setTextColor(originSenderColor);
        originMessageTextView.setTextColor(originMessageColor);
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        super.showMessage(qiscusComment);
        originMessageView.setOnClickListener(v -> {
            if (replyItemClickListener != null) {
                replyItemClickListener.onReplyItemClick(qiscusComment);
            }
        });

        QiscusComment originComment = qiscusComment.getReplyTo();
        originSenderTextView.setText(originComment.getSender());
        switch (originComment.getType()) {
            case IMAGE:
                if (originImageView != null) {
                    originImageView.setVisibility(View.VISIBLE);
                    File localPath = Qiscus.getDataStore().getLocalPath(originComment.getId());
                    if (localPath == null) {
                        showBlurryImage(originComment);
                    } else {
                        showImage(localPath);
                    }
                }
                if (originIconView != null) {
                    originIconView.setVisibility(View.GONE);
                }
                originMessageTextView.setText(originComment.getAttachmentName());
                break;
            case AUDIO:
                if (originImageView != null) {
                    originImageView.setVisibility(View.GONE);
                }
                if (originIconView != null) {
                    originIconView.setVisibility(View.VISIBLE);
                    originIconView.setImageResource(R.drawable.ic_qiscus_add_audio);
                }
                originMessageTextView.setText(QiscusAndroidUtil.getString(R.string.qiscus_voice_message));
                break;
            case FILE:
                if (originImageView != null) {
                    originImageView.setVisibility(View.GONE);
                }
                if (originIconView != null) {
                    originIconView.setVisibility(View.VISIBLE);
                    originIconView.setImageResource(R.drawable.ic_qiscus_file);
                }
                originMessageTextView.setText(originComment.getAttachmentName());
                break;
            default:
                if (originImageView != null) {
                    originImageView.setVisibility(View.GONE);
                }
                if (originIconView != null) {
                    originIconView.setVisibility(View.GONE);
                }
                originMessageTextView.setText(originComment.getMessage());
                break;

        }
    }

    private void showImage(File file) {
        if (originImageView != null) {
            Glide.with(originImageView.getContext())
                    .load(file)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.qiscus_image_placeholder)
                    .into(originImageView);
        }
    }

    private void showBlurryImage(QiscusComment qiscusComment) {
        if (originImageView != null) {
            Glide.with(originImageView.getContext())
                    .load(QiscusImageUtil.generateBlurryThumbnailUrl(qiscusComment.getAttachmentUri().toString()))
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .placeholder(R.drawable.qiscus_image_placeholder)
                    .error(R.drawable.qiscus_image_placeholder)
                    .into(originImageView);
        }
    }
}
