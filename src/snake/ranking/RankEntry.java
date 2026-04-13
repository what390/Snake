package snake.ranking;

import java.io.Serializable;

public class RankEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nickname;
    private int score;
    private long timestamp;

    public RankEntry(String nickname, int score) {
        this.nickname = nickname;
        this.score = score;
        this.timestamp = System.currentTimeMillis();
    }

    public String getNickname() { return nickname; }
    public int getScore() { return score; }
    public long getTimestamp() { return timestamp; }
}