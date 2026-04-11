const express = require("express");
const app = express();
app.get("/heartrate", (_, res) =>
  res.json({ bpm: Math.floor(60 + Math.random() * 40) }),
);
app.get("/spo2", (_, res) =>
  res.json({ percentage: Math.floor(95 + Math.random() * 5) }),
);
app.get("/temperature", (_, res) =>
  res.json({ celsius: +(36.1 + Math.random() * 1.4).toFixed(1) }),
);
app.listen(3000, () => console.log("Mock API on port 3000"));
