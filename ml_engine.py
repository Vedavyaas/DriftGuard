import pandas as pd
import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
import json
import os

SCRATCH_DIR = "/Users/chandhru/.gemini/antigravity-ide/scratch"

# Setup seed for reproducibility
torch.manual_seed(42)
np.random.seed(42)

# 1. Load Data
df = pd.read_csv('drift_event_labels.csv')

# Feature Engineering: Extract hour manually
df['hour_str'] = df['timestamp'].apply(lambda x: x.split('T')[1].split(':')[0])
df['hour_of_day'] = df['hour_str'].astype(float) / 23.0  # Normalize to [0, 1]

# Define categorical features to encode
cat_cols = ['change_source', 'approval_status', 'environment', 'severity']

# Store categories mapping for inference consistency
encoding_map = {}
encoded_features = []

for col in cat_cols:
    unique_vals = sorted(df[col].dropna().unique())
    encoding_map[col] = unique_vals
    # One-hot encoding manually for predictability
    for val in unique_vals:
        feature_name = f"{col}_{val}"
        df[feature_name] = (df[col] == val).astype(float)
        encoded_features.append(feature_name)

# Add numeric/boolean features
df['maintenance_window_val'] = df['maintenance_window'].astype(float)
encoded_features.append('maintenance_window_val')
encoded_features.append('hour_of_day')

# Save encoding map to scratch
with open(os.path.join(SCRATCH_DIR, 'encoding_map.json'), 'w') as f:
    json.dump(encoding_map, f, indent=4)

# Prepare PyTorch Tensors
X = df[encoded_features].values.astype(np.float32)
y = df['is_risky'].values.astype(np.float32).reshape(-1, 1)

# Split into Train and Test (80/20)
indices = np.random.permutation(len(X))
split_idx = int(0.8 * len(X))
train_indices = indices[:split_idx]
test_indices = indices[split_idx:]

X_train, y_train = X[train_indices], y[train_indices]
X_test, y_test = X[test_indices], y[test_indices]

# 2. PyTorch Dataset & DataLoader
class DriftDataset(Dataset):
    def __init__(self, features, labels):
        self.features = torch.tensor(features)
        self.labels = torch.tensor(labels)
        
    def __len__(self):
        return len(self.features)
        
    def __getitem__(self, idx):
        return self.features[idx], self.labels[idx]

train_dataset = DriftDataset(X_train, y_train)
train_loader = DataLoader(train_dataset, batch_size=32, shuffle=True)

# 3. Model Definition
class DriftClassifier(nn.Module):
    def __init__(self, input_dim):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(input_dim, 16),
            nn.ReLU(),
            nn.Linear(16, 8),
            nn.ReLU(),
            nn.Linear(8, 1),
            nn.Sigmoid()
        )
        
    def forward(self, x):
        return self.net(x)

input_dim = len(encoded_features)
model = DriftClassifier(input_dim)
criterion = nn.BCELoss()
optimizer = optim.Adam(model.parameters(), lr=0.01)

# 4. Training Loop
model.train()
print("Training PyTorch Drift Classifier...")
for epoch in range(15):
    epoch_loss = 0.0
    for batch_x, batch_y in train_loader:
        optimizer.zero_grad()
        predictions = model(batch_x)
        loss = criterion(predictions, batch_y)
        loss.backward()
        optimizer.step()
        epoch_loss += loss.item() * batch_x.size(0)
    print(f"Epoch {epoch+1}/15 - Loss: {epoch_loss / len(X_train):.4f}")

# Save Model Weights to scratch
torch.save(model.state_dict(), os.path.join(SCRATCH_DIR, 'drift_model.pth'))
print(f"Model saved to '{os.path.join(SCRATCH_DIR, 'drift_model.pth')}'")

# 5. Predictions & Evaluation
model.eval()
with torch.no_grad():
    all_X = torch.tensor(X)
    raw_preds = model(all_X).numpy()
    preds = (raw_preds >= 0.5).astype(bool).flatten()

# Save predictions back into the dataframe to evaluate
df['predicted_risky'] = preds

# Let's print metrics
y_true = df['is_risky'].astype(int)
y_pred = df['predicted_risky'].astype(int)

# Precision & Recall
tp = ((y_true == 1) & (y_pred == 1)).sum()
fp = ((y_true == 0) & (y_pred == 1)).sum()
fn = ((y_true == 1) & (y_pred == 0)).sum()
tn = ((y_true == 0) & (y_pred == 0)).sum()

precision = tp / (tp + fp) if (tp + fp) > 0 else 0
recall = tp / (tp + fn) if (tp + fn) > 0 else 0

# Critical recall (severity == CRITICAL)
critical_mask = df['severity'] == 'CRITICAL'
tp_critical = ((y_true[critical_mask] == 1) & (y_pred[critical_mask] == 1)).sum()
total_critical = critical_mask.sum()
critical_recall = tp_critical / total_critical if total_critical > 0 else 0

# Benign suppression rate: proportion of benign changes correctly classified as False
benign_mask = df['is_risky'] == False
tn_benign = ((y_true[benign_mask] == 0) & (y_pred[benign_mask] == 0)).sum()
total_benign = benign_mask.sum()
benign_suppression = tn_benign / total_benign if total_benign > 0 else 0

print("\n=== EVALUATION REPORT ===")
print(f"Precision: {precision:.2%}")
print(f"Recall: {recall:.2%}")
print(f"Critical Recall: {critical_recall:.2%}")
print(f"Benign Suppression Rate: {benign_suppression:.2%}")

# Update the CSV file and save to scratch
df_to_save = pd.read_csv('drift_event_labels.csv')
df_to_save['predicted_risky'] = preds
df_to_save.to_csv(os.path.join(SCRATCH_DIR, 'drift_event_labels.csv'), index=False)
print(f"Updated CSV saved to '{os.path.join(SCRATCH_DIR, 'drift_event_labels.csv')}'")
