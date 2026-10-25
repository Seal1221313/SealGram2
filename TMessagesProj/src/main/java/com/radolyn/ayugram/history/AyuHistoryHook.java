package com.radolyn.ayugram.history;

import android.util.SparseArray;

import androidx.core.util.Pair;

import com.radolyn.ayugram.database.entities.DeletedMessageReaction;
import com.radolyn.ayugram.messages.AyuMessagesController;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class AyuHistoryHook {
    public static Pair<Integer, Integer> getMinAndMaxIds(ArrayList<MessageObject> messages) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        boolean hasPositive = false;
        for (var message : messages) {
            if (message.getId() > 0) {
                hasPositive = true;
                break;
            }
        }
        for (var message : messages) {
            int id = message.getId();
            if (hasPositive && id <= 0) {
                continue;
            }
            min = Math.min(min, id);
            max = Math.max(max, id);
        }
        if (min == Integer.MAX_VALUE) {
            return new Pair<>(0, 0);
        }
        return new Pair<>(min, max);
    }

    public static void doHook(int account, ArrayList<MessageObject> messages, SparseArray<MessageObject>[] messagesDict, int startId, int endId, long dialogId, int limit, int topicId, boolean secret) {
        try {
            long userId = UserConfig.getInstance(account).getClientUserId();
            var saved = AyuMessagesController.getInstance().getMessages(userId, dialogId, topicId, startId, endId, limit);
            if (saved == null || saved.isEmpty()) {
                return;
            }

            var existing = new HashSet<Integer>();
            for (var message : messages) {
                existing.add(message.getId());
            }

            boolean added = false;
            for (var full : saved) {
                var entity = full.message;
                if (entity == null || existing.contains(entity.messageId)) {
                    continue;
                }
                if (messagesDict != null && messagesDict.length > 0 && messagesDict[0] != null && messagesDict[0].indexOfKey(entity.messageId) >= 0) {
                    continue;
                }

                var msg = new TLRPC.TL_message();
                AyuMessageUtils.map(entity, msg, account);
                AyuMessageUtils.mapMedia(entity, msg);
                msg.ayuDeleted = true;
                msg.dialog_id = dialogId;
                applyReactions(msg, full.reactions);

                var object = new MessageObject(account, msg, true, false);
                messages.add(object);
                existing.add(entity.messageId);
                added = true;
            }

            if (added) {
                Collections.sort(messages, (a, b) -> {
                    if (secret) {
                        int byDate = Integer.compare(b.messageOwner.date, a.messageOwner.date);
                        if (byDate != 0) {
                            return byDate;
                        }
                    }
                    return Integer.compare(b.getId(), a.getId());
                });
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private static void applyReactions(TLRPC.Message msg, List<DeletedMessageReaction> reactions) {
        if (reactions == null || reactions.isEmpty()) {
            return;
        }
        var result = new TLRPC.TL_messageReactions();
        for (var saved : reactions) {
            var count = new TLRPC.TL_reactionCount();
            count.count = saved.count;
            count.chosen = saved.selfSelected;
            if (saved.isCustom) {
                var reaction = new TLRPC.TL_reactionCustomEmoji();
                reaction.document_id = saved.documentId;
                count.reaction = reaction;
            } else {
                var reaction = new TLRPC.TL_reactionEmoji();
                reaction.emoticon = saved.emoticon;
                count.reaction = reaction;
            }
            result.results.add(count);
        }
        msg.reactions = result;
    }
}
