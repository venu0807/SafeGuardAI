import tensorflow as tf
import numpy as np
import librosa
import os

SR = 16000
N_MFCC = 40

MODEL_PATH = os.path.join(
    os.path.dirname(__file__),
    "../ml/exports/mfcc_audio_model.tflite"
)

interpreter = tf.lite.Interpreter(model_path=MODEL_PATH)
interpreter.allocate_tensors()

input_idx = interpreter.get_input_details()[0]["index"]
output_idx = interpreter.get_output_details()[0]["index"]

def mfcc_score(audio):
    mfcc = librosa.feature.mfcc(y=audio, sr=SR, n_mfcc=N_MFCC)
    mfcc = mfcc.T[np.newaxis, ..., np.newaxis].astype(np.float32)

    interpreter.set_tensor(input_idx, mfcc)
    interpreter.invoke()

    score = interpreter.get_tensor(output_idx)[0][0]
    return float(score)
