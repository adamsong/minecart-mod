package com.alc.moreminecarts.misc;


import com.alc.moreminecarts.MMItemReferences;
import com.alc.moreminecarts.MoreMinecartsMod;
import com.alc.moreminecarts.entities.HSMinecartEntities;
import com.alc.moreminecarts.items.CouplerItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = "moreminecarts")
public class MoreMinecartsEventReciever {

    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {

        Hand hand = event.getHand();
        PlayerEntity player = event.getPlayer();
        ItemStack using = player.getItemInHand(hand);

        Hand other_hand = hand == Hand.MAIN_HAND? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack using_secondary = player.getItemInHand(other_hand);

        Entity entity = event.getTarget();

        // We check both hands, but only use one, since this function gets called once for each hand.
        if (using.getItem() == MMItemReferences.coupler || using_secondary.getItem() == MMItemReferences.coupler) {
            event.setCanceled(true);
            if (event.getWorld().isClientSide()) return;

            if (using.getItem() == MMItemReferences.coupler) {
                if (entity instanceof AbstractMinecartEntity
                    || entity instanceof BoatEntity
                    || entity instanceof MobEntity
                    || entity instanceof EnderDragonEntity){
                    World world = event.getWorld();
                    player.playSound(SoundEvents.CHAIN_PLACE, 0.9F, 1.0F);
                    CouplerItem.hookIn(player, world, using, entity);
                }
                else {
                    CouplerItem.clearCoupler(using);
                }
            }
        }

        if (using.getItem() == MMItemReferences.high_speed_upgrade || using_secondary.getItem() == MMItemReferences.high_speed_upgrade) {
            event.setCanceled(true);
            if (event.getWorld().isClientSide()) return;

            if (using.getItem() == MMItemReferences.high_speed_upgrade && entity instanceof AbstractMinecartEntity
                && !(entity instanceof HSMinecartEntities.IHSCart)) {
                boolean success = HSMinecartEntities.upgradeMinecart((AbstractMinecartEntity) entity);
                if (!player.abilities.instabuild && success) using.getStack().shrink(1);
            }
        }

        // To prevent entering a high speed cart immediately after upgrading it.
        if ( (event.getTarget() instanceof HSMinecartEntities.HSMinecart || event.getTarget() instanceof HSMinecartEntities.HSPushcart)
                && event.getTarget().tickCount < 10) {
            event.setCanceled(true);
            return;
        }

    }

    @SubscribeEvent
    public static void onBiomeLoad(BiomeLoadingEvent event) {
        if (event.getCategory() == Biome.Category.DESERT) {
            event.getGeneration().addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, MoreMinecartsMod.GLASS_CACTUS_FEATURE);
        }
    }

}
