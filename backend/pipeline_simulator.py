from audio_loader import load_audio
from mfcc_inference import mfcc_score
import numpy as np

AUDIO_PATH = "../tests/test_scream.wav"

audio = load_audio(AUDIO_PATH)

peak = float(np.max(np.abs(audio)))
print(f"Peak amplitude: {peak:.3f}")

if peak < 0.25:
    print("No significant sound detected.")
else:
    score = mfcc_score(audio)
    print(f"MFCC Distress Score: {score:.3f}")

    if score >= 0.6:
        print("⚠️  DISTRESS DETECTED")
    else:
        print("Normal audio")
