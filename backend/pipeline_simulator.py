import numpy as np
from audio_loader import load_audio
from mfcc_inference import mfcc_score

AUDIO_PATH = "../tests/test_scream.wav"

DISTRESS_THRESHOLD = 0.6

audio = load_audio(AUDIO_PATH)

peak = np.max(np.abs(audio))
print("Peak amplitude:", peak)

if peak < 0.25:
    print("No significant sound detected.")
else:
    score = mfcc_score(audio)
    print("MFCC distress score:", score)

    if score >= DISTRESS_THRESHOLD:
        print("🚨 DISTRESS DETECTED → SOS WOULD TRIGGER")
    else:
        print("Normal sound detected.")










# from audio_loader import load_audio
# from mfcc_inference import mfcc_score
# from wav2vec2_inference import wav2vec2_score
# from fusion_engine import compute_risk
# from sos_simulator import trigger_sos
# from encryption import encrypt
# import numpy as np

# THRESHOLD = 0.85

# audio = load_audio("../tests/test_scream.wav")

# peak = np.max(np.abs(audio))
# if peak > 0.25:
#     m_score = mfcc_score(audio)
#     if m_score > 0.6:
#         w_score = wav2vec2_score(audio)
#         risk = compute_risk(w_score)
#         if risk >= THRESHOLD:
#             encrypted = encrypt(audio.tobytes())
#             trigger_sos(risk)
#             print(f"SOS triggered with risk score: {risk}")     