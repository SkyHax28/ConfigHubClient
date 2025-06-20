package com.dew.system.event;

import com.dew.system.event.events.*;

public interface EventListener {
    default void onPreUpdate(PreUpdateEvent event) {
    }

    default void onPostUpdate(PostUpdateEvent event) {
    }

    default void onKeyboard(KeyboardEvent event) {
    }

    default void onRender2D(Render2DEvent event) {
    }

    default void onPreMotion(PreMotionEvent event) {
    }

    default void onPostMotion(PostMotionEvent event) {
    }

    default void onSendPacket(SendPacketEvent event) {
    }

    default void onReceivedPacket(ReceivedPacketEvent event) {
    }

    default void onWorld(WorldEvent event) {
    }

    default void onMove(MoveEvent event) {
    }

    default void onRender3D(Render3DEvent event) {
    }

    default void onTick(TickEvent event) {
    }

    default void onAttack(AttackEvent event) {
    }

    default void onStrafe(StrafeEvent event) {
    }

    default void onLivingUpdate(LivingUpdateEvent event) {
    }

    default void onMoveInput(MoveInputEvent event){
    }

    default void onMoveForward(MoveForwardEvent event){
    }

    default void onPostSprint(PostSprintEvent event){
    }

    default void onPreSprint(PreSprintEvent event){
    }
}
