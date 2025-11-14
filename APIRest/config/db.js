import mysql from "mysql2/promise";
import dotenv from "dotenv";
dotenv.config();

const pool = mysql.createPool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
});

pool.getConnection()
  .then(connection => {
    console.log("✅ Conexión exitosa a MySQL");
    console.log(`📦 Base de datos: ${process.env.DB_NAME}`);
    connection.release();
  })
  .catch(err => {
    console.error("❌ Error conectando a MySQL:", err.message);
  });

export default pool;