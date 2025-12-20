import numpy as np
import pandas as pd
from sklearn.metrics import f1_score

thresholds = np.linspace(0.1, 0.9, 17)
rows = []

for t in thresholds:
    preds = (y_scores >= t).astype(int)
    f1 = f1_score(y_true, preds)
    rows.append({"threshold": t, "f1": f1})

df = pd.DataFrame(rows)
df.to_csv("results/threshold_curve.csv", index=False)
