package snake.ranking;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RankingManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static RankingManager instance;
    private List<RankEntry> entries;
    private static final String FILE_NAME = "snake_ranking.dat";

    private RankingManager() {
        entries = new ArrayList<>();
        loadFromFile();
    }

    public static RankingManager getInstance() {
        if (instance == null) {
            instance = new RankingManager();
        }
        return instance;
    }

    public void addScore(String nickname, int score) {
        entries.add(new RankEntry(nickname, score));
        entries.sort((a, b) -> {
            if (a.getScore() != b.getScore()) {
                return Integer.compare(b.getScore(), a.getScore());
            }
            return Long.compare(a.getTimestamp(), b.getTimestamp());
        });
        if (entries.size() > 50) {
            entries = new ArrayList<>(entries.subList(0, 50));
        }
        saveToFile();
    }

    public List<RankEntry> getTopN(int n) {
        int size = Math.min(n, entries.size());
        return entries.subList(0, size);
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                entries = (List<RankEntry>) ois.readObject();
                entries.sort((a, b) -> {
                    if (a.getScore() != b.getScore()) {
                        return Integer.compare(b.getScore(), a.getScore());
                    }
                    return Long.compare(a.getTimestamp(), b.getTimestamp());
                });
            } catch (IOException | ClassNotFoundException e) {
                entries = new ArrayList<>();
            }
        } else {
            entries = new ArrayList<>();
        }
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(entries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}