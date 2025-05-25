package com.example.myapplication.Joc1;

import android.content.res.Resources;

public class NPCManager {
    public static void addNPCWithDialogues(NPC npc, Resources res, int dialoguesArrayId, int questDescriptionId, int requiredItems, int rewardPoints) {
        String[] dialogues = res.getStringArray(dialoguesArrayId);
        for (String dialogue : dialogues) {
            npc.addDialogue(dialogue);
        }
        npc.setNpcQuest(new Mission(
                res.getString(questDescriptionId),
                requiredItems,
                rewardPoints
        ));
    }
}
