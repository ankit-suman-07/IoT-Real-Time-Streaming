import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score
import joblib

# Load data
df = pd.read_csv('data/hour.csv')

# Features and target
features = ['season','hr','holiday','weekday','workingday',
            'weathersit','temp','atemp','hum','windspeed']
X = df[features]
y = df['cnt']

# Split
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# Train
model = RandomForestRegressor(n_estimators=30, random_state=42)
model.fit(X_train, y_train)

# Evaluate
preds = model.predict(X_test)
print(f"MAE:  {mean_absolute_error(y_test, preds):.2f}")
print(f"R2:   {r2_score(y_test, preds):.2f}")

# Save model
joblib.dump(model, 'model/bike_model.pkl')
print("Model saved to model/bike_model.pkl")