package com.birdhouse.tracker;

import java.awt.Color;
import java.awt.TrayIcon;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.FlashNotification;
import net.runelite.client.config.Notification;
import net.runelite.client.config.NotificationSound;
import net.runelite.client.config.RequestFocusType;
import net.runelite.client.config.RuneLiteConfig;

/** READY delivery channels, shared by live events and founder-only smoke tests. */
final class BirdhouseNotificationDelivery
{
    static final Color CHAT_RED = new Color(255, 0, 0);
    static final String TIME_TRACKING_DISABLED_TEXT =
        "Birdhouse Timer: Time Tracking is disabled. Enable it in RuneLite Configuration > search \"Time Tracking\" > enable.";
    // Explicitly desktop-only to avoid the second, default-highlight chat message
    // that RuneLite's generic notifier can otherwise generate.
    private final ChatMessageManager chatMessageManager;
    private final Notifier notifier;
    private final RuneLiteConfig runeLiteConfig;

    @Inject
    BirdhouseNotificationDelivery(ChatMessageManager chatMessageManager, Notifier notifier,
        RuneLiteConfig runeLiteConfig)
    {
        this.chatMessageManager = chatMessageManager;
        this.notifier = notifier;
        this.runeLiteConfig = runeLiteConfig;
    }

    void sendReady(boolean redChatEnabled, boolean desktopEnabled)
    {
        dispatchReady(redChatEnabled, desktopEnabled, this::sendRedChat, this::sendDesktop);
    }

    /** Independent per-player READY choices; production and tests share this branch. */
    static void dispatchReady(boolean redChatEnabled, boolean desktopEnabled,
        Runnable redChat, Runnable desktop)
    {
        if (redChatEnabled)
        {
            try
            {
                redChat.run();
            }
            finally
            {
                // A chat failure must not prevent an enabled desktop attempt.
                if (desktopEnabled)
                {
                    desktop.run();
                }
            }
        }
        else if (desktopEnabled)
        {
            desktop.run();
        }
    }

    void sendRedChat()
    {
        String formatted = new ChatMessageBuilder()
            .append(CHAT_RED, BirdhouseWarningMessage.READY_TEXT)
            .build();
        chatMessageManager.queue(QueuedMessage.builder()
            .type(ChatMessageType.CONSOLE)
            .runeLiteFormattedMessage(formatted)
            .build());
    }

    void sendDesktop()
    {
        // Notifier only applies RuneLite's global focus rule when this per-notification
        // flag is false and the client is focused. Read the global preference at the
        // moment of delivery so user changes take effect without restarting the plugin.
        notifier.notify(desktopNotification(runeLiteConfig.sendNotificationsWhenFocused()),
            BirdhouseWarningMessage.READY_TEXT);
    }

    /** Desktop-only notification that follows RuneLite's global focus preference. */
    static Notification desktopNotification(boolean sendWhenFocused)
    {
        return new Notification(
            true, true, true, true, TrayIcon.MessageType.INFO,
            RequestFocusType.OFF, NotificationSound.OFF, null, 0, 0,
            false, FlashNotification.DISABLED, CHAT_RED, sendWhenFocused);
    }

    /** A dependency warning is not a READY alert and is independent of READY preferences. */
    void sendTimeTrackingDisabledWarning()
    {
        String formatted = new ChatMessageBuilder()
            .append(CHAT_RED, TIME_TRACKING_DISABLED_TEXT)
            .build();
        chatMessageManager.queue(QueuedMessage.builder()
            .type(ChatMessageType.CONSOLE)
            .runeLiteFormattedMessage(formatted)
            .build());
    }
}
