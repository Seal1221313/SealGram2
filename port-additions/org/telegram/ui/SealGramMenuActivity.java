package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.SealGramI18n;
import org.telegram.ui.ActionBar.BaseFragment;

public class SealGramMenuActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        SealGramHubActivity.setupBar(actionBar, "SealGram", this::finishFragment);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, AndroidUtilities.dp(12), 0, AndroidUtilities.dp(24));
        LinearLayout card = SealGramHubActivity.card(context, root);
        SealGramHubActivity.row(context, card, R.drawable.msg_voice_unmuted, SealGramI18n.t("HubVoiceToText"), null, () -> presentFragment(new SealGramHubActivity.Page(4)));
        SealGramHubActivity.row(context, card, R.drawable.msg_edit, SealGramI18n.t("HubAutoReply"), null, () -> presentFragment(new SealGramHubActivity.Page(5)));
        SealGramHubActivity.row(context, card, R.drawable.msg_voicechat, SealGramI18n.t("VoiceTitle"), null, () -> presentFragment(new SealGramVoiceActivity()));
        SealGramHubActivity.row(context, card, R.drawable.msg_palette, SealGramI18n.t("StyleTitle"), null, () -> presentFragment(new SealGramStyleActivity()));
        fragmentView = SealGramHubActivity.scroll(context, root);
        return fragmentView;
    }
}
