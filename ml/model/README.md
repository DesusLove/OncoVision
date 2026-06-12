# Trained model weights

The trained `.pt` files (`resnet18_breakhis_200x.pt` and
`resnet18_multiclass_200x.pt`) are **not** committed — they are in
`.gitignore` at the project root (`*.pt`).

## If you have the real trained weights

Place them here:

| File                              | What it is                          |
|-----------------------------------|-------------------------------------|
| `resnet18_breakhis_200x.pt`       | Binary (benign vs malignant) model  |
| `metadata.json`                   | Class list, threshold, image size   |
| `resnet18_multiclass_200x.pt`     | 8-class subtype model               |
| `metadata_multiclass_200x.json`   | Subtype class list, image size      |

`metadata.json` and `metadata_multiclass_200x.json` are committed; only the
binary `.pt` blobs are excluded.

## If you don't have them — generate dummy weights

Run the helper from the project root:

```bash
cd ml
uv run --python 3.12 --with torch --with torchvision python model/dummy_weights.py
```

This creates ResNet-18 architectures with **randomly initialised weights**.
The pipeline (FastAPI → Spring → Vue) works end-to-end; predictions are
meaningless, but the request/response shapes, persistence, and UI all
function. Useful for wiring tests and CI.

## Training from scratch

The training notebooks live in `ml/notebooks/`:

```
ml/notebooks/breast_cancer_resnet18_200x.ipynb            # binary
ml/notebooks/breast_cancer_resnet18_multiclass_200x.ipynb # multiclass
```

Expected data layout (also git-ignored):

```
ml/data/classificacao_binaria/<benign|malignant>/*.png
ml/data/classificacao_multiclasse/<subtype>/*.png
```

Source dataset: [BreakHis](https://web.inf.ufpr.br/vri/databases/breast-cancer-histopathological-database-breakhis/).
Notebooks were tested on Kaggle with a single GPU.
