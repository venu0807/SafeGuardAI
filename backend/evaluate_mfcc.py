import os
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

from audio_loader import load_audio
from mfcc_inference import mfcc_score

from sklearn.metrics import (
    confusion_matrix,
    accuracy_score,
    precision_score,
    recall_score,
    f1_score
)

# ---------------- CONFIG ----------------
TEST_DIR = "../tests"
OUTPUT_DIR = "../docs"

SPIKE_THRESHOLD = 0.25
DISTRESS_THRESHOLD = 0.6

os.makedirs(OUTPUT_DIR, exist_ok=True)

# ---------------------------------------

y_true = []
y_pred = []
scores = []
filenames = []

print("\nRunning MFCC evaluation...\n")

# LOOP OVER LABELED DATA
for label_name, true_label in [("normal", 0), ("distress", 1)]:
    folder = os.path.join(TEST_DIR, label_name)

    for file in os.listdir(folder):
        if not file.endswith(".wav"):
            continue

        path = os.path.join(folder, file)
        audio = load_audio(path)

        peak = np.max(np.abs(audio))

        if peak < SPIKE_THRESHOLD:
            score = 0.0
            pred = 0
        else:
            score = mfcc_score(audio)
            pred = 1 if score >= DISTRESS_THRESHOLD else 0

        y_true.append(true_label)
        y_pred.append(pred)
        scores.append(score)
        filenames.append(file)

        print(
            f"{file:20s} | GT={true_label} | "
            f"Score={score:.2f} | Pred={pred}"
        )

# ---------------- METRICS ----------------
acc = accuracy_score(y_true, y_pred)
prec = precision_score(y_true, y_pred)
rec = recall_score(y_true, y_pred)
f1 = f1_score(y_true, y_pred)

print("\n=== METRICS ===")
print(f"Accuracy : {acc:.3f}")
print(f"Precision: {prec:.3f}")
print(f"Recall   : {rec:.3f}")
print(f"F1-score : {f1:.3f}")

metrics_df = pd.DataFrame([{
    "Accuracy": acc,
    "Precision": prec,
    "Recall": rec,
    "F1-score": f1
}])

metrics_df.to_csv(
    os.path.join(OUTPUT_DIR, "metrics.csv"),
    index=False
)

# -------- CONFUSION MATRIX (TABLE) -------
cm = confusion_matrix(y_true, y_pred)

cm_df = pd.DataFrame(
    cm,
    index=["Actual_Normal", "Actual_Distress"],
    columns=["Pred_Normal", "Pred_Distress"]
)

cm_df.to_csv(
    os.path.join(OUTPUT_DIR, "confusion_matrix.csv")
)

# -------- CONFUSION MATRIX (HEATMAP) -----
plt.figure(figsize=(5, 4))
sns.heatmap(
    cm,
    annot=True,
    fmt="d",
    cmap="Blues",
    xticklabels=["Normal", "Distress"],
    yticklabels=["Normal", "Distress"]
)
plt.xlabel("Predicted Label")
plt.ylabel("True Label")
plt.title("Confusion Matrix")
plt.tight_layout()
plt.savefig(
    os.path.join(OUTPUT_DIR, "confusion_matrix.png")
)
plt.close()

# -------- SCORE DISTRIBUTION HEATMAP -----
score_df = pd.DataFrame({
    "Score": scores,
    "True Label": y_true
})

plt.figure(figsize=(6, 4))
sns.histplot(
    data=score_df,
    x="Score",
    hue="True Label",
    bins=10,
    kde=True,
    palette={0: "green", 1: "red"}
)
plt.xlabel("MFCC Distress Score")
plt.title("Score Distribution Heatmap")
plt.tight_layout()
plt.savefig(
    os.path.join(OUTPUT_DIR, "score_distribution.png")
)
plt.close()

print("\nEvaluation complete.")
print("Outputs saved in /docs/")
