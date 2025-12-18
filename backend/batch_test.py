import os
import csv
import numpy as np

from audio_loader import load_audio
from mfcc_inference import mfcc_score

# CONFIG
TEST_DIR = "../tests"
OUTPUT_CSV = "../docs/mfcc_results.csv"
SPIKE_THRESHOLD = 0.25
DISTRESS_THRESHOLD = 0.6

results = []

print("Starting batch testing...\n")

for file in os.listdir(TEST_DIR):
    if not file.endswith(".wav"):
        continue

    path = os.path.join(TEST_DIR, file)
    audio = load_audio(path)

    peak = float(np.max(np.abs(audio)))

    if peak < SPIKE_THRESHOLD:
        score = 0.0
        decision = "SKIPPED_LOW_VOLUME"
    else:
        score = mfcc_score(audio)
        decision = "DISTRESS" if score >= DISTRESS_THRESHOLD else "NORMAL"

    print(f"{file:25s} | Peak={peak:.2f} | Score={score:.2f} | {decision}")

    results.append([file, peak, score, decision])

# SAVE CSV
os.makedirs("../docs", exist_ok=True)

with open(OUTPUT_CSV, "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerow(["filename", "peak_amplitude", "mfcc_score", "decision"])
    writer.writerows(results)

print("\nBatch testing complete.")
print(f"Results saved to {OUTPUT_CSV}")
