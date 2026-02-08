package org.iwoss.coremmaw.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;

public class KnowledgeHandler {
    private static final String KNOWLEDGE_TAG = "known_players";

    //method for write meet
    public static void learnPlayer(ServerPlayer teacher, ServerPlayer student) {
        CompoundTag data = teacher.getPersistentData();
        ListTag list = data.getList(KNOWLEDGE_TAG, 8);

        String studentUUID = student.getUUID().toString();

        //inspect
        boolean alreadyKnown = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(studentUUID)) {
                alreadyKnown = true;
                break;

            }
        }

        if (!alreadyKnown) {
            list.add(StringTag.valueOf(studentUUID));
            data.put(KNOWLEDGE_TAG, list);
        }
    }
}