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

/** READY delivery channels, shared by live events and founder-only smoke tests. */
final class BirdhouseNotificationDelivery
{
    static final Color CHAT_RED = new Color(255, 0, 0);
    // Explicitly desktop-only to avoid the second, default-highlight chat message
    // that RuneLite's generic notifier can otherwise generate.
    private static final Notification DESKTOP_ONLY = new Notification(
        true, true, true, true, TrayIcon.MessageType.INFO,
        RequestFocusType.OFF, NotificationSound.OFF, null, 0, 0,
        false, FlashNotification.DISABLED, CHAT_RED, true);

    private final ChatMessageManager chatMessageManager;
    private final Notifier notifier;

    @Inject
    BirdhouseNotificationDelivery(ChatMessageManager chatMessageManager, Notifier notifier)
    {
        this.chatMessageManager = chatMessageManager;
        this.notifier = notifier;
    }

    void sendReady()
    {
        // Both calls use the same literal text. The two paths are deliberately
        // independent and can be smoke-tested separately.
        try
        {
            sendRedChat();
        }
        finally
        {
            // Even if chat queuing fails, still attempt desktop delivery.
            sendDesktop();
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
        notifier.notify(DESKTOP_ONLY, BirdhouseWarningMessage.READY_TEXT);
    }
}
