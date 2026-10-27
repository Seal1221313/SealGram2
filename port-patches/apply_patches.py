import sys

root = sys.argv[1]
java = root + "/TMessagesProj/src/main/java/org/telegram/"


def patch(path, old, new, label):
    with open(path, encoding="utf-8") as f:
        s = f.read()
    if new in s:
        print("Already patched: " + label)
        return
    if s.count(old) != 1:
        sys.exit("Anchor for '%s' found %d times in %s" % (label, s.count(old), path))
    with open(path, "w", encoding="utf-8") as f:
        f.write(s.replace(old, new))
    print("Patched: " + label)


patch(java + "messenger/MessagesController.java",
      "    public MessagesController(int num) {\n        super(num);\n",
      "    public MessagesController(int num) {\n        super(num);\n        SealAutoReply.attach(num);\n",
      "MessagesController auto-reply attach")

la = java + "ui/LaunchActivity.java"
patch(la,
      "        super.onUserInteraction();\n        voipLaunchedInBackground = false;\n",
      "        super.onUserInteraction();\n        org.telegram.messenger.SealAutoReply.touch();\n        voipLaunchedInBackground = false;\n",
      "LaunchActivity.onUserInteraction")
patch(la,
      "        super.onPause();\n        isResumed = false;\n        pipActivityHandler.onPause();\n",
      "        super.onPause();\n        org.telegram.messenger.SealAutoReply.touchNow();\n        isResumed = false;\n        pipActivityHandler.onPause();\n",
      "LaunchActivity.onPause")
patch(la,
      "        super.onResume();\n        isResumed = true;\n        pipActivityHandler.onResume();\n",
      "        super.onResume();\n        org.telegram.messenger.SealAutoReply.touchNow();\n        isResumed = true;\n        pipActivityHandler.onResume();\n",
      "LaunchActivity.onResume")

mc = java + "messenger/MediaController.java"
patch(mc,
      "    public int sampleRate = 48000;\n",
      "    public int sampleRate = 48000;\n    private SealVoice.Processor sealVoice;\n",
      "MediaController sealVoice field")
patch(mc,
      "                if (len > 0) {\n                    buffer.limit(len);\n                    double sum = 0;\n",
      "                if (len > 0) {\n                    buffer.limit(len);\n"
      "                    if (SealVoice.forMessages()) {\n"
      "                        try {\n"
      "                            if (sealVoice == null) sealVoice = SealVoice.create(sampleRate);\n"
      "                            sealVoice.process(buffer, len);\n"
      "                        } catch (Throwable ignore) {\n"
      "                        }\n"
      "                    }\n"
      "                    double sum = 0;\n",
      "MediaController legacy recorder voice hook")
patch(mc,
      "    private boolean shouldUseAudioRecorderV2(int account) {\n",
      "    private boolean shouldUseAudioRecorderV2(int account) {\n        if (SealVoice.forMessages()) {\n            return false;\n        }\n",
      "MediaController V2 recorder bypass")

icv = java + "ui/Components/InstantCameraView.java"
patch(icv,
      "                AudioTimestamp audioTimestamp = new AudioTimestamp();\n                boolean shouldUseTimestamp = true;\n",
      "                AudioTimestamp audioTimestamp = new AudioTimestamp();\n                org.telegram.messenger.SealVoice.Processor sealVoice = null;\n                boolean shouldUseTimestamp = true;\n",
      "InstantCameraView sealVoice decl")
patch(icv,
      "                        long timestamp;\n                        if (shouldUseTimestamp) {\n",
      "                        if (org.telegram.messenger.SealVoice.forRound()) {\n"
      "                            try {\n"
      "                                if (sealVoice == null) sealVoice = org.telegram.messenger.SealVoice.create(audioSampleRate);\n"
      "                                sealVoice.process(byteBuffer, readResult);\n"
      "                            } catch (Throwable ignore) {\n"
      "                            }\n"
      "                        }\n"
      "                        long timestamp;\n                        if (shouldUseTimestamp) {\n",
      "InstantCameraView round video voice hook")

wr = root + "/TMessagesProj/src/main/java/org/webrtc/voiceengine/WebRtcAudioRecord.java"
patch(wr,
      "      long lastTime = System.nanoTime();\n      while (keepAlive) {\n",
      "      long lastTime = System.nanoTime();\n      org.telegram.messenger.SealVoice.Processor sealVoice = org.telegram.messenger.SealVoice.create(requestedSampleRate > 0 ? requestedSampleRate : 48000);\n      while (keepAlive) {\n",
      "WebRtcAudioRecord sealVoice decl")
patch(wr,
      "          // It's possible we've been shut down during the read, and stopRecording() tried and\n",
      "          if (!microphoneMute && org.telegram.messenger.SealVoice.forCalls()) {\n"
      "            try {\n"
      "              sealVoice.process(byteBuffer, bytesRead);\n"
      "            } catch (Throwable ignore) {\n"
      "            }\n"
      "          }\n"
      "          // It's possible we've been shut down during the read, and stopRecording() tried and\n",
      "WebRtcAudioRecord call voice hook")

patch(java + "ui/Components/TranscribeButton.java",
      "        if (processClick) {\n            if (!premium && toOpen) {\n                if (canTranscribeTrial(",
      "        if (processClick) {\n"
      "            MessageObject localVoice = parent.getMessageObject();\n"
      "            if (toOpen && localVoice != null && localVoice.isVoice() && org.telegram.messenger.SealGram.pref(\"stt_inline_button\", true)) {\n"
      "                setLoading(true, true);\n"
      "                if (org.telegram.messenger.SealStt.recognizeInline(parent.getContext(), localVoice, () -> setLoading(false, true))) {\n"
      "                    return;\n"
      "                }\n"
      "                setLoading(false, true);\n"
      "            }\n"
      "            if (!premium && toOpen) {\n                if (canTranscribeTrial(",
      "TranscribeButton inline recognition")

ca = java + "ui/ChatActivity.java"
patch(ca,
      "            if (options.isEmpty() && optionsView == null) {\n                return false;\n            }\n",
      "            if (selectedObject != null && selectedObject.isVoice() && org.telegram.messenger.SealGram.pref(\"stt_enabled\", true) && !options.contains(9101)) {\n"
      "                items.add(\"Розпізнати текст (офлайн)\");\n"
      "                options.add(9101);\n"
      "                icons.add(R.drawable.msg_copy);\n"
      "            }\n"
      "            if (options.isEmpty() && optionsView == null) {\n                return false;\n            }\n",
      "ChatActivity offline recognition menu item")
patch(ca,
      "            case OPTION_COPY: {\n                final TL_iv.RichMessage copyRichMessage",
      "            case 9101: {\n                org.telegram.messenger.SealStt.recognize(this, selectedObject);\n                break;\n            }\n"
      "            case OPTION_COPY: {\n                final TL_iv.RichMessage copyRichMessage",
      "ChatActivity offline recognition handler")

sa = java + "ui/SettingsActivity.java"
patch(sa,
      "\n\n        items.add(UItem.asShadow(null));\n        if (!walletAbove && getMessagesController().config.walletAvailable.get()) {",
      "\n        items.add(SettingCell.Factory.of(9001, IconBackgroundColors.GREEN.top, IconBackgroundColors.GREEN.bottom, R.drawable.settings_features, \"SealGram\"));\n\n        items.add(UItem.asShadow(null));\n        if (!walletAbove && getMessagesController().config.walletAvailable.get()) {",
      "SettingsActivity SealGram row")
patch(sa,
      "            case 10:\n                presentSettingFragment(new LanguageSelectActivity());\n                break;\n",
      "            case 10:\n                presentSettingFragment(new LanguageSelectActivity());\n                break;\n            case 9001:\n                presentSettingFragment(new SealGramMenuActivity());\n                break;\n",
      "SettingsActivity SealGram click")
