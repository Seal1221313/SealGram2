package com.radolyn.ayugram.history;

import android.text.TextUtils;

import com.radolyn.ayugram.AyuConstants;
import com.radolyn.ayugram.AyuUtils;
import com.radolyn.ayugram.database.entities.AyuMessageBase;
import com.radolyn.ayugram.messages.AyuMessagesController;
import com.radolyn.ayugram.messages.AyuSavePreferences;
import com.radolyn.ayugram.utils.AyuFileLocation;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.util.ArrayList;

public class AyuMessageUtils {
    private static final int VECTOR = 0x1cb5c415;

    public static void map(AyuSavePreferences prefs, AyuMessageBase entity) {
        var msg = prefs.getMessage();

        entity.userId = prefs.getUserId();
        entity.dialogId = prefs.getDialogId();
        entity.topicId = prefs.getTopicId();
        entity.messageId = prefs.getMessageId();
        entity.entityCreateDate = prefs.getRequestCatchTime();

        entity.groupedId = msg.grouped_id;
        entity.peerId = msg.peer_id != null ? DialogObject.getPeerDialogId(msg.peer_id) : prefs.getDialogId();
        entity.fromId = msg.from_id != null ? DialogObject.getPeerDialogId(msg.from_id) : 0;
        entity.date = msg.date;
        entity.flags = msg.flags;
        entity.editDate = msg.edit_date;
        entity.views = msg.views;

        if (msg.fwd_from != null) {
            entity.fwdFlags = msg.fwd_from.flags;
            entity.fwdFromId = msg.fwd_from.from_id != null ? DialogObject.getPeerDialogId(msg.fwd_from.from_id) : 0;
            entity.fwdName = msg.fwd_from.from_name;
            entity.fwdDate = msg.fwd_from.date;
            entity.fwdPostAuthor = msg.fwd_from.post_author;
        }

        if (msg.reply_to != null) {
            entity.replyFlags = msg.reply_to.flags;
            entity.replyMessageId = msg.reply_to.reply_to_msg_id;
            entity.replyPeerId = msg.reply_to.reply_to_peer_id != null ? DialogObject.getPeerDialogId(msg.reply_to.reply_to_peer_id) : 0;
            entity.replyTopId = msg.reply_to.reply_to_top_id;
            entity.replyForumTopic = msg.reply_to.forum_topic;
        }

        entity.text = msg.message;
        entity.textEntities = serializeEntities(msg.entities);
    }

    public static void mapMedia(AyuSavePreferences prefs, AyuMessageBase entity, boolean saveFile) {
        var msg = prefs.getMessage();

        entity.documentType = AyuConstants.DOCUMENT_TYPE_NONE;
        entity.mediaPath = null;
        entity.hqThumbPath = null;
        entity.documentSerialized = null;
        entity.mimeType = null;

        if (msg == null || msg.media == null) {
            return;
        }

        TLRPC.Document document = null;
        if (msg.media instanceof TLRPC.TL_messageMediaPhoto && msg.media.photo != null) {
            entity.documentType = AyuConstants.DOCUMENT_TYPE_PHOTO;
        } else if (msg.media instanceof TLRPC.TL_messageMediaDocument && msg.media.document != null) {
            document = msg.media.document;
            entity.mimeType = document.mime_type;
            entity.documentSerialized = serialize(document);
            if (MessageObject.isStickerDocument(document) || MessageObject.isAnimatedStickerDocument(document)) {
                entity.documentType = AyuConstants.DOCUMENT_TYPE_STICKER;
            } else {
                entity.documentType = AyuConstants.DOCUMENT_TYPE_FILE;
            }
        } else {
            return;
        }

        File source = null;
        try {
            source = FileLoader.getInstance(prefs.getAccountId()).getPathToMessage(msg);
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (source == null || !source.exists() || source.length() == 0) {
            return;
        }

        String path = source.getAbsolutePath();
        if (saveFile) {
            try {
                if (!AyuMessagesController.attachmentsPath.exists()) {
                    AyuMessagesController.attachmentsPath.mkdirs();
                }
                var target = new File(AyuMessagesController.attachmentsPath, AyuUtils.getFilename(msg, source));
                if (AndroidUtilities.copyFile(source, target)) {
                    path = target.getAbsolutePath();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        entity.mediaPath = path;
        if (entity.documentType == AyuConstants.DOCUMENT_TYPE_PHOTO || (entity.mimeType != null && entity.mimeType.startsWith("image/"))) {
            entity.hqThumbPath = path;
        }
    }

    public static void map(AyuMessageBase entity, TLRPC.Message msg, int account) {
        msg.id = entity.messageId;
        msg.dialog_id = entity.dialogId;
        msg.date = entity.date;
        msg.flags = entity.flags;
        msg.edit_date = entity.editDate;
        msg.views = entity.views;
        msg.grouped_id = entity.groupedId;
        msg.message = entity.text == null ? "" : entity.text;

        msg.out = (entity.flags & 2) != 0;
        msg.mentioned = (entity.flags & 16) != 0;
        msg.media_unread = (entity.flags & 32) != 0;
        msg.silent = (entity.flags & 8192) != 0;
        msg.post = (entity.flags & 16384) != 0;
        msg.from_scheduled = (entity.flags & 262144) != 0;
        msg.legacy = (entity.flags & 524288) != 0;
        msg.edit_hide = (entity.flags & 2097152) != 0;
        msg.pinned = (entity.flags & 16777216) != 0;

        var controller = MessagesController.getInstance(account);
        msg.peer_id = controller.getPeer(entity.peerId != 0 ? entity.peerId : entity.dialogId);
        if (entity.fromId != 0) {
            msg.from_id = controller.getPeer(entity.fromId);
            msg.flags |= 256;
        } else {
            msg.flags &= ~256;
        }

        var entities = deserializeEntities(entity.textEntities);
        if (!entities.isEmpty()) {
            msg.entities = entities;
            msg.flags |= 128;
        }

        if (entity.fwdFromId != 0 || !TextUtils.isEmpty(entity.fwdName) || !TextUtils.isEmpty(entity.fwdPostAuthor)) {
            var fwd = new TLRPC.TL_messageFwdHeader();
            int fwdFlags = entity.fwdFlags & 128;
            if (entity.fwdFromId != 0) {
                fwd.from_id = controller.getPeer(entity.fwdFromId);
                fwdFlags |= 1;
            }
            if (!TextUtils.isEmpty(entity.fwdName)) {
                fwd.from_name = entity.fwdName;
                fwdFlags |= 32;
            }
            if (!TextUtils.isEmpty(entity.fwdPostAuthor)) {
                fwd.post_author = entity.fwdPostAuthor;
                fwdFlags |= 8;
            }
            fwd.flags = fwdFlags;
            fwd.imported = (fwdFlags & 128) != 0;
            fwd.date = entity.fwdDate;
            msg.fwd_from = fwd;
            msg.flags |= 4;
        }

        if (entity.replyMessageId != 0) {
            var reply = new TLRPC.TL_messageReplyHeader();
            reply.reply_to_msg_id = entity.replyMessageId;
            reply.reply_to_scheduled = (entity.replyFlags & 4) != 0;
            reply.forum_topic = entity.replyForumTopic;
            int replyFlags = 0;
            if (entity.replyPeerId != 0) {
                reply.reply_to_peer_id = controller.getPeer(entity.replyPeerId);
                replyFlags |= 1;
            }
            if (entity.replyTopId != 0) {
                reply.reply_to_top_id = entity.replyTopId;
                replyFlags |= 2;
            }
            if (reply.reply_to_scheduled) {
                replyFlags |= 4;
            }
            if (reply.forum_topic) {
                replyFlags |= 8;
            }
            reply.flags = replyFlags;
            msg.reply_to = reply;
            msg.flags |= 8;
        }
    }

    public static void mapMedia(AyuMessageBase entity, TLRPC.Message msg) {
        if (entity.documentType == AyuConstants.DOCUMENT_TYPE_NONE) {
            return;
        }

        File file = TextUtils.isEmpty(entity.mediaPath) ? null : new File(entity.mediaPath);
        boolean fileExists = file != null && file.exists();

        if (entity.documentType == AyuConstants.DOCUMENT_TYPE_PHOTO) {
            if (!fileExists) {
                return;
            }
            int w = 0;
            int h = 0;
            var size = AyuUtils.extractImageSizeFromName(file.getName());
            if (size == null) {
                size = AyuUtils.extractImageSizeFromFile(file.getAbsolutePath());
            }
            if (size != null && size.first != null && size.second != null) {
                w = size.first;
                h = size.second;
            }
            if (w <= 0 || h <= 0) {
                w = 800;
                h = 800;
            }

            var photoSize = new TLRPC.TL_photoSize();
            photoSize.type = "x";
            photoSize.w = w;
            photoSize.h = h;
            photoSize.size = (int) file.length();
            photoSize.location = new AyuFileLocation(file.getAbsolutePath());

            var photo = new TLRPC.TL_photo();
            photo.id = ((long) entity.messageId << 32) ^ (entity.dialogId * 31) ^ file.getAbsolutePath().hashCode();
            photo.date = entity.date;
            photo.sizes = new ArrayList<>();
            photo.sizes.add(photoSize);

            var media = new TLRPC.TL_messageMediaPhoto();
            media.flags = 1;
            media.photo = photo;
            msg.media = media;
            msg.flags |= 512;
            if (fileExists) {
                msg.attachPath = file.getAbsolutePath();
            }
        } else {
            var document = deserializeDocument(entity.documentSerialized);
            if (document == null) {
                return;
            }
            var media = new TLRPC.TL_messageMediaDocument();
            media.flags = 1;
            media.document = document;
            msg.media = media;
            msg.flags |= 512;
            if (fileExists) {
                msg.attachPath = file.getAbsolutePath();
            }
        }
    }

    public static byte[] serialize(TLObject object) {
        try {
            var data = new SerializedData();
            object.serializeToStream(data);
            var bytes = data.toByteArray();
            data.cleanup();
            return bytes;
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    private static TLRPC.Document deserializeDocument(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return null;
        }
        try {
            var data = new SerializedData(bytes);
            var document = TLRPC.Document.TLdeserialize(data, data.readInt32(false), false);
            data.cleanup();
            return document;
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    private static byte[] serializeEntities(ArrayList<TLRPC.MessageEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return null;
        }
        try {
            var data = new SerializedData();
            data.writeInt32(VECTOR);
            data.writeInt32(entities.size());
            for (var entity : entities) {
                entity.serializeToStream(data);
            }
            var bytes = data.toByteArray();
            data.cleanup();
            return bytes;
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    private static ArrayList<TLRPC.MessageEntity> deserializeEntities(byte[] bytes) {
        var result = new ArrayList<TLRPC.MessageEntity>();
        if (bytes == null || bytes.length < 8) {
            return result;
        }
        try {
            var data = new SerializedData(bytes);
            if (data.readInt32(false) == VECTOR) {
                int count = data.readInt32(false);
                for (int i = 0; i < count; i++) {
                    var entity = TLRPC.MessageEntity.TLdeserialize(data, data.readInt32(false), false);
                    if (entity == null) {
                        break;
                    }
                    result.add(entity);
                }
            }
            data.cleanup();
        } catch (Exception e) {
            FileLog.e(e);
        }
        return result;
    }
}
