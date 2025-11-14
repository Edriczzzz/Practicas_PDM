import pool from "../config/db.js";

export class Task {
  static async getAll() {
    const [rows] = await pool.query("SELECT * FROM Task");
    return rows;
  }

  static async getById(id) {
    const [rows] = await pool.query("SELECT * FROM Task WHERE id = ?", [id]);
    return rows[0];
  }

  static async create(task) {
    const { name, status, deadline } = task;
    const [result] = await pool.query(
      "INSERT INTO Task (name, status, deadline) VALUES (?, ?, ?)",
      [name, status, deadline]
    );
    return { id: result.insertId, ...task };
  }

  static async update(id, task) {
    const { name, status, deadline } = task;
    await pool.query(
      "UPDATE Task SET name=?, status=?, deadline=? WHERE id=?",
      [name, status, deadline, id]
    );
  }

  static async delete(id) {
    await pool.query("DELETE FROM Task WHERE id=?", [id]);
  }
}
