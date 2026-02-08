package org.iwoss.coremmaw.network;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class ClientKnowledgeStorage {
    //list UUID players
    private static final Set<UUID> KNOWN_PLAYERS = new HashSet<>();
    private static final Map<UUID, String> RP_NAMES = new HashMap<>();

    public static void addPlayer(UUID uuid) {
        KNOWN_PLAYERS.add(uuid);
    }

    public static boolean isKnown(UUID uuid) {
        return KNOWN_PLAYERS.contains(uuid);
    }

    public static void clear() {
        KNOWN_PLAYERS.clear();
    }

    public static void updateRPName(UUID uuid, String name) {
        RP_NAMES.put(uuid, name);
    }

    public static String getRPName(UUID uuid) {
        return RP_NAMES.getOrDefault(uuid, "");
    }

}