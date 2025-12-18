import tensorflow as tf
import numpy as np
import librosa

MODEL_PATH = "../ml/exports/mfcc_audio_model.tflite"
SR = 16000
N_MFCC = 40

interpreter = tf.lite.Interpreter(model_path=MODEL_PATH)
interpreter.allocate_tensors()

input_index = interpreter.get_input_details()[0]["index"]
output_index = interpreter.get_output_details()[0]["index"]

def mfcc_score(audio):
    mfcc = librosa.feature.mfcc(y=audio, sr=SR, n_mfcc=N_MFCC)
    mfcc = mfcc.T
    mfcc = mfcc[np.newaxis, ..., np.newaxis].astype(np.float32)

    interpreter.set_tensor(input_index, mfcc)
    interpreter.invoke()

    score = interpreter.get_tensor(output_index)[0][0]
    return float(score)

















# import tensorflow as tf
# import numpy as np
# import librosa

# N_MFCC = 40
# SR = 16000

# interpreter = tf.lite.Interpreter(
#     model_path="../ml/exports/mfcc_audio_model.tflite"
# )
# interpreter.allocate_tensors()

# input_idx = interpreter.get_input_details()[0]["index"]
# output_idx = interpreter.get_output_details()[0]["index"]

# def mfcc_score(audio):
#     mfcc = librosa.feature.mfcc(y=audio, sr=SR, n_mfcc=N_MFCC)
#     mfcc = mfcc.T[np.newaxis, ..., np.newaxis].astype(np.float32)
#     interpreter.set_tensor(input_idx, mfcc)
#     interpreter.invoke()
#     return float(interpreter.get_tensor(output_idx)[0][0])
