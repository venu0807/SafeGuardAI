import numpy as np
import librosa

def audio_prefilter(audio, sr=16000):
    frame_len = int(0.025 * sr)
    hop_len = int(0.010 * sr)

    energy = np.mean(librosa.feature.rms(
        y=audio,
        frame_length=frame_len,
        hop_length=hop_len
    ))

    S = np.abs(librosa.stft(audio, n_fft=512))
    psd = S / (np.sum(S, axis=0, keepdims=True) + 1e-9)
    entropy = -np.mean(np.sum(psd * np.log(psd + 1e-9), axis=0))

    zcr = np.mean(librosa.feature.zero_crossing_rate(audio))

    return energy > 0.01 or entropy > 3.5 or zcr > 0.08
