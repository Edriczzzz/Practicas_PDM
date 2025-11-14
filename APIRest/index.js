import dotenv from "dotenv";
import express from "express";
import cors from "cors";
import taskRoutes from "./routes/tasks.js";
import authRoutes from "./routes/auth.js";
import pool from "./config/db.js";  // ✅ Importar pool

dotenv.config();
const app = express();

app.use(cors());
app.use(express.json());
app.use("/api/tasks", taskRoutes);
app.use("/api/auth", authRoutes);

app.get("/", (req, res) => res.send("API Tasks funcionando ✅"));

// ✅ AGREGAR ENDPOINT DE PRUEBA
app.get("/test-db", async (req, res) => {
  try {
    const [rows] = await pool.query("SELECT 1 + 1 AS result");
    res.json({ 
      message: "Conexión a MySQL exitosa ✅", 
      test: rows[0] 
    });
  } catch (error) {
    res.status(500).json({ 
      message: "Error conectando a MySQL ❌", 
      error: error.message 
    });
  }
});

// Para desarrollo local
if (process.env.NODE_ENV !== "production") {
  const PORT = process.env.PORT || 3000;
  app.listen(PORT, () => {
    console.log(`Servidor corriendo en puerto ${PORT}`);
  });
}

// Exportar para Vercel
export default app;

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`🚀 Servidor corriendo en puerto ${PORT}`);
  console.log(`🔗 http://localhost:${PORT}`);
});