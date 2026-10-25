/*

 This is the source code of exteraGram for Android.

 We do not and cannot prevent the use of our code,
 but be respectful and credit the original author.

 Copyright @immat0x1, 2023

*/

package com.exteragram.messenger.utils;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.TranscribeButton;

import java.io.File;
import java.util.Locale;

public class ChatUtils {
    private static boolean useFallback;

    public static String getDC(TLRPC.User user) {
        return getDC(user, null);
    }

    public static String getDC(TLRPC.Chat chat) {
        return getDC(null, chat);
    }

    public static String getDC(TLRPC.User user, TLRPC.Chat chat) {
        int DC = 0, myDC = getConnectionsManager().getCurrentDatacenterId();
        if (user != null) {
            if (UserObject.isUserSelf(user) && myDC != -1) {
                DC = myDC;
            } else {
                DC = user.photo != null ? user.photo.dc_id : -1;
            }
        } else if (chat != null) {
            DC = chat.photo != null ? chat.photo.dc_id : -1;
        }
        if (DC == -1 || DC == 0) {
            return getDCName(0);
        } else {
            return String.format(Locale.ROOT, "DC%d, %s", DC, getDCName(DC));
        }
    }

    public static String getDCName(int dc) {
        switch (dc) {
            case 1:
            case 3:
                return "Miami FL, USA";
            case 2:
            case 4:
                return "Amsterdam, NL";
            case 5:
                return "Singapore, SG";
            default:
                return null;
        }
    }

    public static boolean isSubscribedTo(long id) {
        TLRPC.Chat chat = getMessagesController().getChat(id);
        return chat != null && !chat.left && !chat.kicked;
    }

    public static String getName(long did) {
        int currentAccount = UserConfig.selectedAccount;
        String name = null;
        if (DialogObject.isEncryptedDialog(did)) {
            TLRPC.EncryptedChat encryptedChat = getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(did));
            if (encryptedChat != null) {
                TLRPC.User user = getMessagesController().getUser(encryptedChat.user_id);
                if (user != null)
                    name = ContactsController.formatName(user.first_name, user.last_name);
            }
        } else if (DialogObject.isUserDialog(did)) {
            TLRPC.User user = getMessagesController().getUser(did);
            if (user != null) name = ContactsController.formatName(user.first_name, user.last_name);
        } else {
            TLRPC.Chat chat = getMessagesController().getChat(-did);
            if (chat != null) name = chat.title;
        }
        return did == UserConfig.getInstance(currentAccount).getClientUserId() ? LocaleController.getString("SavedMessages", R.string.SavedMessages) : name;
    }

    public interface SearchCallback {
        void run(TLRPC.User user);
    }

    public static void searchById(Long userId, SearchCallback callback) {
        if (userId == 0) {
            return;
        }
        callback.run(getMessagesController().getUser(userId));
    }

    public static String getOwnerIds(long stickerSetId) {
        return "int32: " + (stickerSetId >> 32) + '\n' +
                "int64: " + (0x100000000L + (stickerSetId >> 32));
    }

    public static MessagesController getMessagesController() {
        return MessagesController.getInstance(UserConfig.selectedAccount);
    }

    public static MessagesStorage getMessageStorage() {
        return MessagesStorage.getInstance(UserConfig.selectedAccount);
    }

    public static ConnectionsManager getConnectionsManager() {
        return ConnectionsManager.getInstance(UserConfig.selectedAccount);
    }

    public static FileLoader getFileLoader() {
        return FileLoader.getInstance(UserConfig.selectedAccount);
    }

    public static void addMessageToClipboard(MessageObject selectedObject, Runnable callback) {
        String path = getPathToMessage(selectedObject);
        if (!TextUtils.isEmpty(path)) {
            SystemUtils.addFileToClipboard(new File(path), callback);
        }
    }

    public static String getPathToMessage(MessageObject messageObject) {
        String path = messageObject.messageOwner.attachPath;
        if (!TextUtils.isEmpty(path)) {
            File temp = new File(path);
            if (!temp.exists()) {
                path = null;
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = getFileLoader().getPathToMessage(messageObject.messageOwner).toString();
            File temp = new File(path);
            if (!temp.exists()) {
                path = null;
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = getFileLoader().getPathToAttach(messageObject.getDocument(), true).toString();
            File temp = new File(path);
            if (!temp.exists()) {
                return null;
            }
        }
        return path;
    }

    public static boolean hasArchivedChats() {
        return getMessagesController().dialogs_dict.get(DialogObject.makeFolderDialogId(1)) != null;
    }

    public static CharSequence getMessageText(MessageObject selectedObject, MessageObject.GroupedMessages selectedObjectGroup) {
        CharSequence messageTextToTranslate = null;
        if (selectedObject.type != MessageObject.TYPE_EMOJIS && selectedObject.type != MessageObject.TYPE_ANIMATED_STICKER && selectedObject.type != MessageObject.TYPE_STICKER) {
            messageTextToTranslate = getMessageCaption(selectedObject, selectedObjectGroup);
            if (messageTextToTranslate == null && selectedObject.isPoll()) {
                try {
                    TLRPC.Poll poll = ((TLRPC.TL_messageMediaPoll) selectedObject.messageOwner.media).poll;
                    StringBuilder pollText = new StringBuilder(poll.question).append("\n");
                    for (TLRPC.TL_pollAnswer answer : poll.answers)
                        pollText.append("\n\uD83D\uDD18 ").append(answer.text);
                    messageTextToTranslate = pollText.toString();
                } catch (Exception ignored) {
                }
            }
            if (messageTextToTranslate == null && MessageObject.isMediaEmpty(selectedObject.messageOwner)) {
                messageTextToTranslate = getMessageContent(selectedObject);
            }
            if (messageTextToTranslate != null && Emoji.fullyConsistsOfEmojis(messageTextToTranslate)) {
                messageTextToTranslate = null;
            }
        }
        if (selectedObject.translated || selectedObject.isRestrictedMessage) {
            messageTextToTranslate = null;
        }
        return messageTextToTranslate;
    }

    private static CharSequence getMessageCaption(MessageObject messageObject, MessageObject.GroupedMessages group) {
        String restrictionReason = MessagesController.getRestrictionReason(messageObject.messageOwner.restriction_reason);
        if (!TextUtils.isEmpty(restrictionReason)) {
            return restrictionReason;
        }
        if (messageObject.isVoiceTranscriptionOpen() && !TranscribeButton.isTranscribing(messageObject)) {
            return messageObject.getVoiceTranscription();
        }
        if (messageObject.caption != null) {
            return messageObject.caption;
        }
        if (group == null) {
            return null;
        }
        CharSequence caption = null;
        for (int a = 0, N = group.messages.size(); a < N; a++) {
            MessageObject message = group.messages.get(a);
            if (message.caption != null) {
                if (caption != null) {
                    return null;
                }
                caption = message.caption;
            }
        }
        return caption;
    }

    private static CharSequence getMessageContent(MessageObject messageObject) {
        SpannableStringBuilder str = new SpannableStringBuilder();
        String restrictionReason = MessagesController.getRestrictionReason(messageObject.messageOwner.restriction_reason);
        if (!TextUtils.isEmpty(restrictionReason)) {
            str.append(restrictionReason);
        } else if (messageObject.caption != null) {
            str.append(messageObject.caption);
        } else {
            str.append(messageObject.messageText);
        }
        return str.toString();
    }
}
