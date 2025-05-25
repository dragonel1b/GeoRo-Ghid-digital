package com.example.myapplication.Joc1;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCManager {
    private static NPCManager instance;
    private Map<String, NPC> npcs = new HashMap<>();
    private Context context;
    
    private NPCManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static synchronized NPCManager getInstance(Context context) {
        if (instance == null) {
            instance = new NPCManager(context);
        }
        return instance;
    }
    
    public void initializeNPCs() {
        // Clear existing NPCs
        npcs.clear();
        
        // This would be called once at game initialization
        // Usually from your main activity or game initialization code
        Resources res = context.getResources();
        
        // Example: Create pre-defined NPCs for cities
        createNPCsForCity("Sibiu", res);
        createNPCsForCity("Cluj", res);
        createNPCsForCity("Brașov", res);
        createNPCsForCity("București", res);
        createNPCsForCity("Iași", res);
        createNPCsForCity("Timișoara", res);
    }
    
    private void createNPCsForCity(String cityName, Resources res) {
        // In a real implementation, you would have specific NPCs for each city
        // with their own dialogues and quests from resource files
        
        // This is a placeholder implementation
        String npcId = cityName + "_guide";
        
        // Get NPC drawable - assuming you have drawables named appropriately
        // For example, R.drawable.npc_guide
        int drawableResId = context.getResources().getIdentifier(
                "npc_guide", "drawable", context.getPackageName());
        
        if (drawableResId == 0) {
            // Fallback to a default drawable if specific one not found
            drawableResId = android.R.drawable.ic_dialog_info;
        }
        
        Drawable npcDrawable = ContextCompat.getDrawable(context, drawableResId);
        
        // Create the NPC at a specific position (this would be based on your city layout)
        NPC npc = new NPC(100, 100, cityName + " Guide", npcDrawable, 0xFF0000FF);
        
        // Add some basic dialogues
        npc.addDialogue("Welcome to " + cityName + "!");
        npc.addDialogue("I can help you discover the attractions of " + cityName + ".");
        npc.addDialogue("Have you tried our local cuisine?");
        
        // Add a simple quest
        Mission cityQuest = new Mission(
                "Explore " + cityName + " historical sites", 
                3, // Collect 3 items
                50 // 50 reward points
        );
        npc.setNpcQuest(cityQuest);
        
        // Store the NPC
        npcs.put(npcId, npc);
    }
    
    /**
     * Original method maintained for backward compatibility
     */
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
    
    public void addNPC(String npcId, NPC npc) {
        npcs.put(npcId, npc);
    }
    
    public NPC getNPC(String npcId) {
        return npcs.get(npcId);
    }
    
    public List<NPC> getAllNPCsForCity(String cityName) {
        List<NPC> cityNPCs = new ArrayList<>();
        for (Map.Entry<String, NPC> entry : npcs.entrySet()) {
            if (entry.getKey().startsWith(cityName.toLowerCase())) {
                cityNPCs.add(entry.getValue());
            }
        }
        return cityNPCs;
    }
    
    public void resetNPCInteractions() {
        for (NPC npc : npcs.values()) {
            npc.setInteracted(false);
        }
    }
    
    public void updateNPCQuests(Context context) {
        // Update quests based on player progress
        // For example, mark quests as completed if they're saved in SharedPreferences
        
        for (Map.Entry<String, NPC> entry : npcs.entrySet()) {
            String npcId = entry.getKey();
            NPC npc = entry.getValue();
            
            if (npc.getNpcQuest() != null) {
                String questId = npcId + "_quest";
                if (SharedPrefsHelper.isQuestCompleted(context, questId)) {
                    // Mark the quest as completed if it's saved in preferences
                    npc.getNpcQuest().setCompleted(true);
                }
            }
        }
    }
    
    public void completeQuest(Context context, String npcId) {
        NPC npc = npcs.get(npcId);
        if (npc != null && npc.getNpcQuest() != null && !npc.getNpcQuest().isCompleted()) {
            // Mark the quest as completed
            npc.getNpcQuest().setCompleted(true);
            
            // Save to preferences
            String questId = npcId + "_quest";
            SharedPrefsHelper.markQuestCompleted(context, questId);
            
            // Give rewards to player
            int rewardPoints = npc.getNpcQuest().getRewardPoints();
            SharedPrefsHelper.addToBalance(context, rewardPoints);
            SharedPrefsHelper.addExperience(context, rewardPoints / 2);
        }
    }
}
