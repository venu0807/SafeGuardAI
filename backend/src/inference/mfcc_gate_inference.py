import numpy as np
import tensorflow as tf
from backend.preprocessing.audio_prefilter import audio_prefilter
from backend.preprocessing.mfcc_features import extract_mfcc

class MFCCGate:
    def __init__(self, model_path):
        self.interpreter = tf.lite.Interpreter(model_path=model_path)
        self.interpreter.allocate_tensors()
        self.input_details = self.interpreter.get_input_details()
        self.output_details = self.interpreter.get_output_details()

    def predict(self, audio):
        if not audio_prefilter(audio):
            return 0.0

        mfcc = extract_mfcc(audio)
        mfcc = mfcc[np.newaxis, ..., np.newaxis].astype(np.float32)

        self.interpreter.set_tensor(
            self.input_details[0]['index'], mfcc
        )
        self.interpreter.invoke()

        score = self.interpreter.get_tensor(
            self.output_details[0]['index']
        )[0][0]

        return float(score)
