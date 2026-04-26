#!/usr/bin/env python3
"""Train a health-state classifier and convert it to TensorFlow Lite.

Required packages:
- tensorflow==2.16.1
- pandas
- numpy
- scikit-learn
"""

from __future__ import annotations

from pathlib import Path
from tempfile import TemporaryDirectory

import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.metrics import classification_report
from sklearn.model_selection import train_test_split


LABEL_TO_ID = {
    "NORMAL": 0,
    "ELEVATED": 1,
    "CRITICAL": 2,
}

ID_TO_LABEL = {v: k for k, v in LABEL_TO_ID.items()}


def load_dataset(csv_path: Path) -> tuple[np.ndarray, np.ndarray]:
    df = pd.read_csv(csv_path)
    expected_columns = ["hr", "spo2", "temp", "label"]
    if list(df.columns) != expected_columns:
        raise ValueError(f"Unexpected columns: {list(df.columns)}. Expected: {expected_columns}")

    unknown_labels = set(df["label"].unique()) - set(LABEL_TO_ID)
    if unknown_labels:
        raise ValueError(f"Unknown labels found: {sorted(unknown_labels)}")

    x = df[["hr", "spo2", "temp"]].astype(np.float32).to_numpy()
    y = df["label"].map(LABEL_TO_ID).astype(np.int32).to_numpy()

    return x, y


def build_model(normalizer: tf.keras.layers.Normalization) -> tf.keras.Model:
    model = tf.keras.Sequential(
        [
            tf.keras.layers.Input(shape=(3,), name="health_input"),
            normalizer,
            tf.keras.layers.Dense(32, activation="relu"),
            tf.keras.layers.Dense(16, activation="relu"),
            tf.keras.layers.Dense(3, activation="softmax", name="class_probs"),
        ],
        name="health_classifier",
    )
    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=1e-3),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"],
    )
    return model


def main() -> None:
    np.random.seed(42)
    tf.random.set_seed(42)

    script_dir = Path(__file__).resolve().parent
    csv_path = script_dir / "health_data.csv"
    model_path = script_dir / "health_model.keras"
    tflite_path = script_dir / "model.tflite"

    if not csv_path.exists():
        raise FileNotFoundError(f"Dataset not found: {csv_path}")

    print(f"TensorFlow version: {tf.__version__}")
    if tf.__version__ != "2.16.1":
        print("WARNING: Expected tensorflow==2.16.1")

    x, y = load_dataset(csv_path)

    x_train, x_test, y_train, y_test = train_test_split(
        x,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y,
    )

    normalizer = tf.keras.layers.Normalization(axis=-1)
    normalizer.adapt(x_train)

    model = build_model(normalizer)
    callbacks = [
        tf.keras.callbacks.EarlyStopping(
            monitor="val_loss",
            patience=10,
            restore_best_weights=True,
        )
    ]

    model.fit(
        x_train,
        y_train,
        validation_split=0.2,
        epochs=80,
        batch_size=32,
        callbacks=callbacks,
        verbose=2,
    )

    test_loss, test_acc = model.evaluate(x_test, y_test, verbose=0)
    print(f"Test loss: {test_loss:.4f}")
    print(f"Test accuracy: {test_acc:.4f}")

    y_pred = np.argmax(model.predict(x_test, verbose=0), axis=1)
    print("\nClassification report:")
    print(classification_report(y_test, y_pred, target_names=[ID_TO_LABEL[i] for i in range(3)]))

    model.save(model_path)
    print(f"Saved Keras model to: {model_path}")

    with TemporaryDirectory() as saved_model_dir:
        model.export(saved_model_dir)
        converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir)
        tflite_model = converter.convert()
    tflite_path.write_bytes(tflite_model)
    print(f"Saved TFLite model to: {tflite_path}")


if __name__ == "__main__":
    main()
