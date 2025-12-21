import os
import json
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.metrics import (
    confusion_matrix,
    classification_report,
    precision_recall_fscore_support
)

from backend.utils.audio_loader import load_audio
from backend.inference.mfcc_gate_inference import MFCCGate
from backend.config import MFCC_MODEL_PATH

NORMAL_DIR = "tests/normal"
DISTRESS_DIR = "tests/distress"
RESULTS_DIR = "results"

os.makedirs(RESULTS_DIR, exist_ok=True)

y_true = []
y_scores = []

gate = MFCCGate(MFCC_MODEL_PATH)

def process_folder(folder, label):
    for f in os.listdir(folder):
        if f.endswith(".wav"):
            audio = load_audio(os.path.join(folder, f))
            score = gate.predict(audio)
            y_true.append(label)
            y_scores.append(score)

process_folder(NORMAL_DIR, 0)
process_folder(DISTRESS_DIR, 1)

y_scores = np.array(y_scores)
y_pred = (y_scores >= 0.5).astype(int)

# Metrics
precision, recall, f1, _ = precision_recall_fscore_support(
    y_true, y_pred, average="binary"
)

metrics = {
    "precision": float(precision),
    "recall": float(recall),
    "f1_score": float(f1)
}

with open(f"{RESULTS_DIR}/metrics.json", "w") as f:
    json.dump(metrics, f, indent=2)

# Classification report
report = classification_report(y_true, y_pred)
with open(f"{RESULTS_DIR}/classification_report.txt", "w") as f:
    f.write(report)

# Confusion matrix
cm = confusion_matrix(y_true, y_pred)
pd.DataFrame(cm, index=["Normal", "Distress"],
             columns=["Pred_Normal", "Pred_Distress"]) \
  .to_csv(f"{RESULTS_DIR}/confusion_matrix.csv")

# Confusion matrix heatmap
plt.figure(figsize=(5,4))
plt.imshow(cm, cmap="Blues")
plt.title("Confusion Matrix")
plt.colorbar()
plt.xticks([0,1], ["Normal", "Distress"])
plt.yticks([0,1], ["Normal", "Distress"])
plt.xlabel("Predicted")
plt.ylabel("Actual")

for i in range(2):
    for j in range(2):
        plt.text(j, i, cm[i,j], ha="center", va="center")

plt.tight_layout()
plt.savefig(f"{RESULTS_DIR}/confusion_matrix.png")
plt.close()

# Score distribution
pd.DataFrame({
    "score": y_scores,
    "label": y_true
}).to_csv(f"{RESULTS_DIR}/score_distribution.csv", index=False)

plt.figure()
plt.hist(y_scores[np.array(y_true)==0], bins=30, alpha=0.6, label="Normal")
plt.hist(y_scores[np.array(y_true)==1], bins=30, alpha=0.6, label="Distress")
plt.legend()
plt.title("MFCC Score Distribution")
plt.xlabel("Score")
plt.ylabel("Count")
plt.savefig(f"{RESULTS_DIR}/score_distribution.png")
plt.close()

print("✅ Evaluation complete. Results saved in /results")
