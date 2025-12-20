import librosa
import numpy as np

TARGET_FRAMES = 101     # MUST match model
N_MFCC = 40
SR = 16000
N_FFT = 512
HOP_LENGTH = 160

def extract_mfcc(audio):
    # Ensure fixed length audio (1 second)
    if len(audio) > SR:
        audio = audio[:SR]
    else:
        audio = np.pad(audio, (0, SR - len(audio)))

    mfcc = librosa.feature.mfcc(
        y=audio,
        sr=SR,
        n_mfcc=N_MFCC,
        n_fft=N_FFT,
        hop_length=HOP_LENGTH
    )

    delta = librosa.feature.delta(mfcc)
    delta2 = librosa.feature.delta(mfcc, order=2)

    features = np.vstack([mfcc, delta, delta2])

    # Enforce fixed frame count
    if features.shape[1] > TARGET_FRAMES:
        features = features[:, :TARGET_FRAMES]
    else:
        pad_width = TARGET_FRAMES - features.shape[1]
        features = np.pad(
            features,
            ((0, 0), (0, pad_width)),
            mode="constant"
        )

    # CMVN
    features -= np.mean(features, axis=1, keepdims=True)
    features /= (np.std(features, axis=1, keepdims=True) + 1e-6)

    return features
