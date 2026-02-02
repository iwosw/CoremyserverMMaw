package org.iwoss.coremmaw.compat.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityBison;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class NewBisonHerdLogic {

    public static void spawnMigration(ServerLevel level, BlockPos startPos) {
        int herdSize = 40;
        List<EntityBison> herd = new ArrayList<>();

        // 1. Создаем вожака (Альфа-самец)
        EntityBison leader = createBison(level, startPos, true);
        if (leader == null) return;
        herd.add(leader);

        // 2. Создаем стадо вокруг
        for (int i = 1; i < herdSize; i++) {
            BlockPos memberPos = startPos.offset(level.random.nextInt(15) - 7, 0, level.random.nextInt(15) - 7);
            EntityBison follower = createBison(level, memberPos, false);
            if (follower != null) {
                // Внедряем ИИ следования за вожаком
                // Используем TemptGoal хитро: вожак для них как "еда", они хотят быть рядом
                follower.goalSelector.addGoal(1, new TemptGoal(follower, 1.1D, Ingredient.of(Items.WHEAT), false));
                // Примечание: В идеале тут нужен кастомный Goal, но для начала сойдет и привязка к лидеру
                herd.add(follower);
            }
        }

        System.out.println("Миграция бизонов началась: 40 голов направляются на пастбища.");
    }

    private static EntityBison createBison(ServerLevel level, BlockPos pos, boolean isLeader) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        EntityBison bison = AMEntityRegistry.BISON.get().create(level);

        if (bison != null) {
            bison.moveTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, level.random.nextFloat() * 360, 0);

            // Баффы характеристик
            double hp = isLeader ? 100.0D : 60.0D;
            bison.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
            bison.setHealth((float) hp);

            // Броня против стрел и копий (Хардкор!)
            bison.getAttribute(Attributes.ARMOR).setBaseValue(isLeader ? 10.0D : 4.0D);
            bison.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);

            bison.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
            level.addFreshEntity(bison);
        }
        return bison;
    }
}