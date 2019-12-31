package ru.gadjini.reminder.domain;

public class FriendSearchResult {

    private TgUser friend;

    private String matchWord;

    public FriendSearchResult(TgUser friend, String matchWord) {
        this.friend = friend;
        this.matchWord = matchWord;
    }

    public boolean isNotFound() {
        return friend == null;
    }

    public TgUser getFriend() {
        return friend;
    }

    public String getMatchWord() {
        return matchWord;
    }
}
