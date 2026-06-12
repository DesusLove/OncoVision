"""
Generate dummy ResNet-18 weights for the OncoVision pipeline.

Use this when you don't have the real BreakHis-trained weights and just want
the full stack (FastAPI -> Spring -> Vue) to work end-to-end with random
predictions. Real evaluation needs the trained models; this is a wiring test.

Usage (from the project root):
    cd ml
    uv run --python 3.12 \
      --with torch --with torchvision \
      python model/dummy_weights.py

Or with plain pip:
    pip install torch torchvision
    python ml/model/dummy_weights.py

Outputs:
    ml/model/resnet18_breakhis_200x.pt            (binary: 2 classes)
    ml/model/resnet18_multiclass_200x.pt          (multiclass: 8 classes)
Both files match the architecture consumed by ml/service/app.py.
"""

import torch
from torchvision.models import resnet18

# Match exactly the metadata in ml/model/metadata.json and
# ml/model/metadata_multiclass_200x.json — the FastAPI service reads these
# at startup to rebuild the model head, so the dummy weights have to declare
# the same class count.
N_BINARY = 2
N_MULTI = 8


def make_head(n_classes: int) -> torch.nn.Module:
    """Return a ResNet-18 with the final FC layer replaced for n_classes."""
    model = resnet18(weights=None)
    model.fc = torch.nn.Linear(model.fc.in_features, n_classes)
    return model


def main() -> None:
    bin_model = make_head(N_BINARY)
    multi_model = make_head(N_MULTI)
    torch.save(bin_model.state_dict(), "model/resnet18_breakhis_200x.pt")
    torch.save(multi_model.state_dict(), "model/resnet18_multiclass_200x.pt")
    print("Wrote model/resnet18_breakhis_200x.pt (random, 2 classes)")
    print("Wrote model/resnet18_multiclass_200x.pt (random, 8 classes)")
    print("Predictions will be effectively random — the pipeline still works.")


if __name__ == "__main__":
    main()
