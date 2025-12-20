from collections import deque
import numpy as np

class TemporalGate:
    def __init__(self, window=5, threshold=0.6):
        self.buffer = deque(maxlen=window)
        self.threshold = threshold

    def update(self, score):
        self.buffer.append(score)

    def is_consistent(self):
        if len(self.buffer) < self.buffer.maxlen:
            return False
        return np.mean(self.buffer) >= self.threshold
