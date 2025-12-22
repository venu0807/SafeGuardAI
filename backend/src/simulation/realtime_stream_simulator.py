import time
import librosa
import numpy as np
import os

from backend.src.inference.mfcc_gate_inference import MFCCGate
from backend.src.fusion.temporal_gate import TemporalGate
from backend.src.fusion.fusion_engine import FusionEngine
from backend.src.fusion.decision_engine import DecisionEngine
from backend.config import MFCC_MODEL_PATH

# ---------------------------------------------------
# CONFIG (MUST MATCH ANDROID)
# ---------------------------------------------------
SAMPLE_RATE = 16000
WINDOW_SIZE = 16000        # 1 second
HOP_SIZE = 8000            # 0.5 second overlap
SLEEP_TIME = 0.5           # simulate live stream

TEST_AUDIO_PATH = os.path.abspath(
    os.path.join(os.path.dirname(__file__), "..", "..", "tests", "test_scream.wav")
)



# ---------------------------------------------------
# LOAD TEST AUDIO
# ---------------------------------------------------
audio, sr = librosa.load(TEST_AUDIO_PATH, sr=SAMPLE_RATE, mono=True)

print("Audio loaded")
print("Total duration:", round(len(audio) / sr, 2), "seconds")

# ---------------------------------------------------
# INITIALIZE PIPELINE
# ---------------------------------------------------
mfcc_gate = MFCCGate(MFCC_MODEL_PATH)
temporal_gate = TemporalGate(window=5, threshold=0.6)
fusion_engine = FusionEngine()
decision_engine = DecisionEngine(threshold=0.85)

latencies = []

print("\nStarting real-time simulation...\n")

# ---------------------------------------------------
# REAL-TIME LOOP
# ---------------------------------------------------
for idx in range(0, len(audio) - WINDOW_SIZE, HOP_SIZE):
    chunk = audio[idx: idx + WINDOW_SIZE]

    start_time = time.time()

    # MFCC gate inference
    score = mfcc_gate.predict(chunk)

    latency_ms = (time.time() - start_time) * 1000
    latencies.append(latency_ms)

    temporal_gate.update(score)

    print(
        f"Window {idx//HOP_SIZE:02d} | "
        f"MFCC score = {score:.3f} | "
        f"Latency = {latency_ms:.1f} ms"
    )

    # Temporal consistency check
    if temporal_gate.is_consistent():
        fusion_engine.update_audio(score)
        risk = fusion_engine.compute_risk()

        print(f"   → Consistent audio | Risk score = {risk:.3f}")

        if decision_engine.should_trigger_sos(risk):
            print("\n🚨🚨🚨 SOS WOULD TRIGGER (REAL-TIME) 🚨🚨🚨\n")
            break

    time.sleep(SLEEP_TIME)

# ---------------------------------------------------
# LATENCY SUMMARY
# ---------------------------------------------------
if latencies:
    print("\nLatency Summary:")
    print("  Mean latency :", round(np.mean(latencies), 2), "ms")
    print("  Max latency  :", round(np.max(latencies), 2), "ms")
    print("  Min latency  :", round(np.min(latencies), 2), "ms")

