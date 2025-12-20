import seaborn as sns
import matplotlib.pyplot as plt
import pandas as pd

df = pd.read_csv("results/score_distribution.csv")

plt.figure(figsize=(6,4))
sns.kdeplot(
    data=df,
    x="score",
    hue="label",
    fill=True,
    common_norm=False
)

plt.title("Risk Score Density Heatmap")
plt.xlabel("MFCC Distress Score")
plt.savefig("results/risk_heatmap.png")
plt.close()
