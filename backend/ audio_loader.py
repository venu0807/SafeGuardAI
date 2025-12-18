import librosa
import numpy as np

SR = 16000
SAMPLES = SR  # 1 second

def load_audio(path):
    audio, _ = librosa.load(path, sr=SR, mono=True)
    if len(audio) > SAMPLES:
        audio = audio[:SAMPLES]
    else:
        audio = np.pad(audio, (0, SAMPLES - len(audio)))
    return audio