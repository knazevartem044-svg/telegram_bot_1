package org.example;

public final class Response {
    private final long chatId;
    private final String text;
    private final boolean showStartMenu;

    public Response(long chatId, String text) {
        this(chatId, text, false);
    }

    public Response(long chatId, String text, boolean showStartMenu) {
        this.chatId = chatId;
        this.text = text;
        this.showStartMenu = showStartMenu;
    }

    public long getChatId() { return chatId; }
    public String getText() { return text; }
    public boolean isShowStartMenu() { return showStartMenu; }
}
