import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import taskRoutes from "./routes/tasks.js";
import authRoutes from "./routes/auth.js";


dotenv.config();

const app = express();

// Middlewares
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE'],
  credentials: true
}));
app.use(express.json());

// Ruta de prueba
app.get("/", (req, res) => {
  res.json({ 
    message: "✅ API funcionando correctamente",
    version: "1.0.0",
    endpoints: {
      auth: "/api/auth/login",
      tasks: "/api/tasks"
    }
  });
});

// Rutas de la API
app.use("/api/auth", authRoutes);
app.use("/api/tasks", taskRoutes);

// Manejo de rutas no encontradas
app.use((req, res) => {
  res.status(404).json({ 
    error: "Ruta no encontrada",
    path: req.path 
  });
});

// Para desarrollo local
const PORT = process.env.PORT || 3000;
if (process.env.NODE_ENV !== 'production') {
  app.listen(PORT, () => {
    console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
  });
}

// Exportar para Vercel
export default app;