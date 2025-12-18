import os
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

from audio_loader import load_audio
from mfcc_inference import mfcc_score
from sklearn.metrics import confusion_matrix, accuracy_score, precision_score, recall_score, f1_score

# CONFIG
TEST_DIR = "../tests"
OUTPUT_DIR = "../docs"
SPIKE_THRESHOLD = 0.25
DISTRESS_THRESHOLD = 0.6

os.makedirs(OUTPUT_DIR, exist_ok=True)

y_true = []
y_pred = []

print("Evaluating MFCC + CNN model...\n")

# NORMAL = 0, DISTRESS = 1
for label_name, true_label in [("normal", 0), ("distress", 1)]:
    folder = os.path.join(TEST_DIR, label_name)

    for file in os.listdir(folder):
        if not file.endswith(".wav"):
            continue

        path = os.path.join(folder, file)
        audio = load_audio(path)

        peak = np.max(np.abs(audio))

        if peak < SPIKE_THRESHOLD:
            pred_label = 0
            score = 0.0
        else:
            score = mfcc_score(audio)
            pred_label = 1 if score >= DISTRESS_THRESHOLD else 0

        y_true.append(true_label)
        y_pred.append(pred_label)

        print(f"{file:20s} | GT={true_label} | Pred={pred_label} | Score={score:.2f}")

# -----------------------------
# METRICS
# -----------------------------
acc = accuracy_score(y_true, y_pred)
prec = precision_score(y_true, y_pred)
rec = recall_score(y_true, y_pred)
f1 = f1_score(y_true, y_pred)

metrics_df = pd.DataFrame([{
    "Accuracy": acc,
    "Precision": prec,
    "Recall": rec,
    "F1-Score": f1
}])

metrics_df.to_csv(f"{OUTPUT_DIR}/metrics.csv", index=False)

print("\n=== METRICS ===")
print(metrics_df)

# -----------------------------
# CONFUSION MATRIX
# -----------------------------
cm = confusion_matrix(y_true, y_pred)

cm_df = pd.DataFrame(
    cm,
    index=["Actual Normal", "Actual Distress"],
    columns=["Predicted Normal", "Predicted Distress"]
)

cm_df.to_csv(f"{OUTPUT_DIR}/confusion_matrix.csv")

# HEATMAP
plt.figure(figsize=(5, 4))
sns.heatmap(
    cm_df,
    annot=True,
    fmt="d",
    cmap="Blues"
)
plt.title("Confusion Matrix – MFCC + CNN")
plt.tight_layout()
plt.savefig(f"{OUTPUT_DIR}/confusion_matrix.png")
plt.close()

print("\nConfusion matrix saved to docs/")
