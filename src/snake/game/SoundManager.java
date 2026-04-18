package snake.game;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    private static SoundManager instance;
    private Clip bgmClip;

    private SoundManager() {}

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    public void playBackgroundMusic() {
        playBackgroundMusic("res/bgm.wav");
    }

    public void playBackgroundMusic(String filePath) {
        try {
            stopBackgroundMusic();
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("❌ 音乐文件不存在: " + file.getAbsolutePath());
                return;
            }
            System.out.println("✅ 找到音乐文件: " + file.getAbsolutePath());
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("🎵 背景音乐播放中...");
        } catch (UnsupportedAudioFileException e) {
            System.err.println("❌ 不支持的音频格式，请转换为 WAV (PCM 16-bit)");
        } catch (IOException | LineUnavailableException e) {
            System.err.println("❌ 播放失败: " + e.getMessage());
        }
    }

    public void stopBackgroundMusic() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }

    public void playEatSound() {
        playSoundEffect("res/eat.wav");
    }

    public void playSoundEffect(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("❌ 音效文件不存在: " + file.getAbsolutePath());
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            System.err.println("❌ 音效播放失败: " + e.getMessage());
        }
    }
}