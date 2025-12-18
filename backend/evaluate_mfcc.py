import os
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

from audio_loader import load_audio
from mfcc_inference import mfcc_score
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    confusion_matrix
)

# CONFIG
TEST_DIR = "../tests"
SPIKE_THRESHOLD = 0.25
DISTRESS_THRESHOLD = 0.6
OUTPUT_DIR = "../docs"
os.makedirs(OUTPUT_DIR, exist_ok=True)

y_true = []
y_pred = []
scores = []
files = []

print("Running evaluation...\n")

for label_dir, true_label in [("normal", 0), ("distress", 1)]:
    folder = os.path.join(TEST_DIR, label_dir)

    for file in os.listdir(folder):
        if not file.endswith(".wav"):
            continue

        path = os.path.join(folder, file)
        audio = load_audio(path)
        peak = np.max(np.abs(audio))

        if peak < SPIKE_THRESHOLD:
            pred = 0
            score = 0.0
        else:
            score = mfcc_score(audio)
            pred = 1 if score >= DISTRESS_THRESHOLD else 0

        y_true.append(true_label)
        y_pred.append(pred)
        scores.append(score)
        files.append(file)

        print(f"{file:20s} | GT={true_label} | Score={score:.2f} | Pred={pred}")

# METRICS
acc = accuracy_score(y_true, y_pred)
prec = precision_score(y_true, y_pred)
rec = recall_score(y_true, y_pred)
f1 = f1_score(y_true, y_pred)

print("\n=== METRICS ===")
print(f"Accuracy : {acc:.2f}")
print(f"Precision: {prec:.2f}")
print(f"Recall   : {rec:.2f}")
print(f"F1-score : {f1:.2f}")

# SAVE METRICS
metrics_df = pd.DataFrame([{
    "Accuracy": acc,
    "Precision": prec,
    "Recall": rec,
    "F1-score": f1
}])
metrics_df.to_csv(f"{OUTPUT_DIR}/metrics.csv", index=False)

# CONFUSION MATRIX
cm = confusion_matrix(y_true, y_pred)

plt.figure(figsize=(5,4))
sns.heatmap(
    cm,
    annot=True,
    fmt="d",
    cmap="Blues",
    xticklabels=["Normal", "Distress"],
    yticklabels=["Normal", "Distress"]
)
plt.xlabel("Predicted")
plt.ylabel("Actual")
plt.title("Confusion Matrix")
plt.tight_layout()
plt.savefig(f"{OUTPUT_DIR}/confusion_matrix.png")
plt.close()

# SCORE HEATMAP (DISTRIBUTION)
score_df = pd.DataFrame({
    "file": files,
    "true_label": y_true,
    "score": scores
})

plt.figure(figsize=(6,4))
sns.histplot(
    data=score_df,
    x="score",
    hue="true_label",
    bins=10,
    kde=True,
    palette=["green", "red"]
)
plt.xlabel("MFCC Distress Score")
plt.title("Score Distribution Heatmap")
plt.tight_layout()
plt.savefig(f"{OUTPUT_DIR}/score_distribution.png")
plt.close()

print("\nEvaluation complete.")
print("Outputs saved to /docs/")
