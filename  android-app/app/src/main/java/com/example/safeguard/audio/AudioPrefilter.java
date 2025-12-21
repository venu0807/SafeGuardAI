package com.example.safeguard.audio;

public class AudioPrefilter {

    public static boolean isSuspicious(float[] audio) {
        float energy = 0f;
        int zcr = 0;

        for (int i = 0; i < audio.length; i++) {
            energy += audio[i] * audio[i];
            if (i > 0 && audio[i] * audio[i - 1] < 0) zcr++;
        }

        energy /= audio.length;
        float zcrRate = (float) zcr / audio.length;

        return energy > 0.01f || zcrRate > 0.08f;
    }
}
