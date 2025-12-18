import librosa
import numpy as np
import soundfile as sf

SR = 16000
SAMPLES = SR

def load_audio(path):
    try:
        audio, _ = sf.read(path)
        if audio.ndim > 1:
            audio = audio.mean(axis=1)
        audio = librosa.resample(audio, orig_sr=44100, target_sr=SR)
    except Exception:
        audio, _ = librosa.load(path, sr=SR, mono=True)

    if len(audio) > SAMPLES:
        audio = audio[:SAMPLES]
    else:
        audio = np.pad(audio, (0, SAMPLES - len(audio)))

    return audio
